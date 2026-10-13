package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarProductoRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotBlank String unidadMedida) {
}