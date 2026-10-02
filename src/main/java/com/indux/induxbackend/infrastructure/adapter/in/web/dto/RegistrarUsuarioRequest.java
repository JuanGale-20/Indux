package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarUsuarioRequest(
        Long empresaId,
        @NotBlank String nombre,
        @NotBlank String email,
        @NotBlank String password,
        @NotNull Rol rol) {

    @Override
    public String toString() {
        return "RegistrarUsuarioRequest[email=" + email + ", rol=" + rol + "]";
    }
}