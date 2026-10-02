package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;

public record UsuarioResponse(Long id, Long empresaId, String nombre, String email, Rol rol, boolean activo) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.id(), u.empresaId(), u.nombre(), u.email(), u.rol(), u.activo());
    }
}
