package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActualizarUsuarioRequest(
        Long empresaId,
        @NotBlank String nombre,
        @NotBlank String email,
        String password,
        @NotNull Rol rol) {
}