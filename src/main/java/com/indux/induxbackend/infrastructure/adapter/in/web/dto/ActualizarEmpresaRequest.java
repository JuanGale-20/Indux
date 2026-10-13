package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEmpresaRequest(
        @NotBlank String nombre,
        String sector) {
}