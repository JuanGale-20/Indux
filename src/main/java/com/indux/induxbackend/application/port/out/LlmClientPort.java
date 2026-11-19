package com.indux.induxbackend.application.port.out;

import java.util.List;

public interface LlmClientPort {

    record MensajeLlm(String role, String content) {}

    record PeticionLlm(
            String model,
            List<MensajeLlm> messages,
            double temperature,
            int maxTokens
    ) {
        public PeticionLlm {
            if (messages == null) {
                messages = List.of();
            }
        }
    }

    record ResultadoLlm(
            String contenido,
            String modelo,
            boolean exito,
            String detalleError
    ) {}

    ResultadoLlm generarRespuesta(PeticionLlm peticion);

    boolean estaConfigurado();

    String obtenerModeloPredeterminado();
}
