package com.indux.induxbackend.domain.model.asistente;

import java.util.List;

public record RespuestaAsistente(
        String respuesta,
        String modeloUtilizado,
        boolean proveedorConfigurado,
        List<String> fuentesContexto,
        String motor
) {
    public RespuestaAsistente {
        if (fuentesContexto == null) {
            fuentesContexto = List.of();
        }
        if (motor == null || motor.isBlank()) {
            motor = proveedorConfigurado ? "GEMINI" : "LOCAL";
        }
    }

    public RespuestaAsistente(String respuesta, String modeloUtilizado, boolean proveedorConfigurado, List<String> fuentesContexto) {
        this(respuesta, modeloUtilizado, proveedorConfigurado, fuentesContexto, proveedorConfigurado ? "GEMINI" : "LOCAL");
    }
}