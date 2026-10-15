package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActualizarRecursoRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,
        String tipo,
        @NotNull @Positive Integer cantidadDisponible,
        String estado) {
}