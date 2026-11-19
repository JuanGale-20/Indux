package com.indux.induxbackend.infrastructure.adapter.out.llm;

import com.indux.induxbackend.application.port.out.LlmClientPort.MensajeLlm;
import com.indux.induxbackend.application.port.out.LlmClientPort.PeticionLlm;
import com.indux.induxbackend.application.port.out.LlmClientPort.ResultadoLlm;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiLlmAdapterTest {

    @Test
    void adapterSinApiKeyNoEstaConfigurado() {
        GeminiLlmAdapter adapter = new GeminiLlmAdapter("", "gemini-3.8-flash", "https://generativelanguage.googleapis.com/v1beta/openai", 15);
        assertThat(adapter.estaConfigurado()).isFalse();
        assertThat(adapter.obtenerModeloPredeterminado()).isEqualTo("gemini-3.8-flash");

        ResultadoLlm res = adapter.generarRespuesta(new PeticionLlm("gemini-3.8-flash", List.of(), 0.2, 500));
        assertThat(res.exito()).isFalse();
        assertThat(res.detalleError()).contains("no configurada");
    }

    @Test
    void adapterConPlaceholderNoEstaConfigurado() {
        GeminiLlmAdapter adapter = new GeminiLlmAdapter("tu_api_key_aqui", "gemini-3.8-flash", "https://generativelanguage.googleapis.com/v1beta/openai", 15);
        assertThat(adapter.estaConfigurado()).isFalse();
    }

    @Test
    void adapterConClaveValidaEstaConfigurado() {
        GeminiLlmAdapter adapter = new GeminiLlmAdapter("AIzaSyBValidApiKey12345", "gemini-3.8-flash", "https://generativelanguage.googleapis.com/v1beta/openai", 15);
        assertThat(adapter.estaConfigurado()).isTrue();
        assertThat(adapter.obtenerModeloPredeterminado()).isEqualTo("gemini-3.8-flash");
    }

    @Test
    void adapterTomaModeloConfigurado() {
        GeminiLlmAdapter adapter = new GeminiLlmAdapter("AIzaSyBValidApiKey12345", "gemini-custom-model", "https://generativelanguage.googleapis.com/v1beta/openai", 20);
        assertThat(adapter.obtenerModeloPredeterminado()).isEqualTo("gemini-custom-model");
    }

    @Test
    void claveGeminiViajaEnEncabezadosYNuncaEnUrlNiEnParametros() {
        String testKey = "AIzaSySuperSecretKey999";
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GeminiLlmAdapter adapter = new GeminiLlmAdapter(testKey, "gemini-3.8-flash", "https://generativelanguage.googleapis.com/v1beta/openai", 15, builder);

        String mockResponseBody = "{\"id\":\"chat-test\",\"model\":\"gemini-3.8-flash\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Respuesta Segura\"},\"finish_reason\":\"stop\"}]}";

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", testKey))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + testKey))
                .andExpect(request -> {
                    String uri = request.getURI().toString();
                    assertThat(uri).doesNotContain(testKey);
                    assertThat(uri).doesNotContain("key=");
                })
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        ResultadoLlm res = adapter.generarRespuesta(new PeticionLlm("gemini-3.8-flash", List.of(new MensajeLlm("user", "Hola")), 0.2, 500));

        server.verify();
        assertThat(res.exito()).isTrue();
        assertThat(res.contenido()).isEqualTo("Respuesta Segura");
    }
}