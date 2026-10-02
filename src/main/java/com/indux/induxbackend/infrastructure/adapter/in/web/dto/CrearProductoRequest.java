package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearProductoRequest(
        @NotNull Long empresaId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotBlank String unidadMedida) {
}
