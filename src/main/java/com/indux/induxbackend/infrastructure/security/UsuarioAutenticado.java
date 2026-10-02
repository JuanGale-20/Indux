package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.domain.model.Rol;

public record UsuarioAutenticado(Long id, Long empresaId, String email, Rol rol) {
}
