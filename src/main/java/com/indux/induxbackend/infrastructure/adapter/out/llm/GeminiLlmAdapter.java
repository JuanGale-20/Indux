package com.indux.induxbackend.infrastructure.adapter.out.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.induxbackend.application.port.out.LlmClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Adaptador de infraestructura para conectar Indux con Google AI Studio (Gemini).
 * Utiliza el endpoint oficial compatible con chat completions de Google Gemini:
 * https://generativelanguage.googleapis.com/v1beta/openai/chat/completions
 *
 * La clave de API se transmite estrictamente en encabezados HTTP seguros
 * (x-goog-api-key y Authorization: Bearer). Nunca se incluye en parámetros de URL
 * ni se escribe en archivos de log.
 */
@Slf4j
@Component
public class GeminiLlmAdapter implements LlmClientPort {

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutSegundos;
    private final RestClient restClient;

    @Autowired
    public GeminiLlmAdapter(
            @Value("${indux.ia.gemini.api-key:${GEMINI_API_KEY:${INDUX_LLM_API_KEY:}}}") String apiKey,
            @Value("${indux.ia.gemini.model:${GEMINI_MODEL:${INDUX_LLM_MODEL:gemini-3.8-flash}}}") String model,
            @Value("${indux.ia.gemini.base-url:${GEMINI_BASE_URL:${INDUX_LLM_BASE_URL:https://generativelanguage.googleapis.com/v1beta/openai}}}") String baseUrl,
            @Value("${indux.ia.gemini.timeout-segundos:${GEMINI_TIMEOUT:${INDUX_LLM_TIMEOUT:15}}}") int timeoutSegundos
    ) {
        this(apiKey, model, baseUrl, timeoutSegundos, null);
    }

    public GeminiLlmAdapter(
            String apiKey,
            String model,
            String baseUrl,
            int timeoutSegundos,
            RestClient.Builder restClientBuilder
    ) {
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null && !model.isBlank()) ? model.trim() : "gemini-3.8-flash";
        String rawBaseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.trim() : "https://generativelanguage.googleapis.com/v1beta/openai";
        this.baseUrl = rawBaseUrl.endsWith("/") ? rawBaseUrl.substring(0, rawBaseUrl.length() - 1) : rawBaseUrl;
        this.timeoutSegundos = timeoutSegundos > 0 ? timeoutSegundos : 15;

        RestClient.Builder builder;
        if (restClientBuilder != null) {
            builder = restClientBuilder;
        } else {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofSeconds(5));
            requestFactory.setReadTimeout(Duration.ofSeconds(this.timeoutSegundos));
            builder = RestClient.builder().requestFactory(requestFactory);
        }

        this.restClient = builder
                .baseUrl(this.baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public boolean estaConfigurado() {
        return !apiKey.isBlank()
                && !apiKey.equalsIgnoreCase("tu_api_key_aqui")
                && !apiKey.startsWith("reemplaza_")
                && apiKey.length() >= 10;
    }

    @Override
    public String obtenerModeloPredeterminado() {
        return model;
    }

    @Override
    public ResultadoLlm generarRespuesta(PeticionLlm peticion) {
        if (!estaConfigurado()) {
            return new ResultadoLlm(null, model, false, "API Key de Google AI Studio (Gemini) no configurada");
        }

        String modeloUsado = (peticion.model() != null && !peticion.model().isBlank())
                ? peticion.model()
                : this.model;

        List<GeminiMessageDto> messages = peticion.messages().stream()
                .map(m -> new GeminiMessageDto(m.role(), m.content()))
                .toList();

        GeminiChatRequest request = new GeminiChatRequest(
                modeloUsado,
                messages,
                peticion.temperature(),
                peticion.maxTokens() > 0 ? peticion.maxTokens() : 1500
        );

        // Máximo 1 reintento ante fallos transitorios (total: 2 intentos)
        int maxIntentos = 2;
        Exception ultimoError = null;

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                // Seguridad: La clave viaja EXCLUSIVAMENTE en encabezados HTTP. Nunca en query params.
                GeminiChatResponse response = restClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .header("x-goog-api-key", apiKey)
                        .body(request)
                        .retrieve()
                        .body(GeminiChatResponse.class);

                if (response == null || response.choices() == null || response.choices().isEmpty()) {
                    return new ResultadoLlm(null, modeloUsado, false, "Respuesta vacía del proveedor Google Gemini");
                }

                String contenido = response.choices().get(0).message().content();
                return new ResultadoLlm(contenido, modeloUsado, true, null);

            } catch (Exception e) {
                ultimoError = e;
                // Seguridad: los logs del servidor jamás imprimen la clave API
                log.warn("Intento {} de {} falló al invocar Gemini ({}): {}", 
                        intento, maxIntentos, modeloUsado, e.getMessage());

                if (intento < maxIntentos) {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Falla definitiva al comunicarse con la API de Google Gemini (AI Studio): {}",
                ultimoError != null ? ultimoError.getMessage() : "Error desconocido");
        return new ResultadoLlm(null, modeloUsado, false, ultimoError != null ? ultimoError.getMessage() : "Falla de comunicación con Gemini");
    }

    // DTOs para serialización con el endpoint compatible de Gemini en Google AI Studio
    public record GeminiChatRequest(
            String model,
            List<GeminiMessageDto> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiMessageDto(
            String role,
            String content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiChatResponse(
            String id,
            String model,
            List<GeminiChoiceDto> choices
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiChoiceDto(
            Integer index,
            GeminiMessageDto message,
            @JsonProperty("finish_reason") String finishReason
    ) {}
}