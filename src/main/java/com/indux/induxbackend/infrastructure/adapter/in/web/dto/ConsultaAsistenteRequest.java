package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConsultaAsistenteRequest(
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 2500, message = "El mensaje no puede superar 2500 caracteres")
        String mensaje,

        Long reporteId,

        List<MensajeHistorialDto> historial
) {
    public record MensajeHistorialDto(
            String rol,
            String contenido
    ) {}
}
