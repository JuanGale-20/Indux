package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActualizarProcesoRequest(
        Long liderId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotNull @Positive Integer secuencia,
        @NotBlank String unidadProcesada) {
}