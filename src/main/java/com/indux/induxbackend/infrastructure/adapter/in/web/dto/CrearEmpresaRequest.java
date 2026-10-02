package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearEmpresaRequest(
        @NotBlank String nombre,
        String sector) {
}
