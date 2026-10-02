package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Usuario;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.UsuarioJpaEntity;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toDomain(UsuarioJpaEntity e) {
        return new Usuario(e.getId(), e.getEmpresaId(), e.getNombre(), e.getEmail(),
                e.getPasswordHash(), e.getRol(), e.isActivo());
    }

    public static UsuarioJpaEntity toEntity(Usuario d) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.setId(d.id());
        e.setEmpresaId(d.empresaId());
        e.setNombre(d.nombre());
        e.setEmail(d.email());
        e.setPasswordHash(d.passwordHash());
        e.setRol(d.rol());
        e.setActivo(d.activo());
        return e;
    }
}