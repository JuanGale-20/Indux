package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Recurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.RecursoJpaEntity;

public final class RecursoMapper {

    private RecursoMapper() {
    }

    public static Recurso toDomain(RecursoJpaEntity e) {
        return new Recurso(e.getId(), e.getEmpresaId(), e.getCodigo(), e.getNombre(), e.getTipo(),
                e.getCantidadDisponible(), e.getEstado());
    }

    public static RecursoJpaEntity toEntity(Recurso d) {
        RecursoJpaEntity e = new RecursoJpaEntity();
        e.setId(d.id());
        e.setEmpresaId(d.empresaId());
        e.setCodigo(d.codigo());
        e.setNombre(d.nombre());
        e.setTipo(d.tipo());
        e.setCantidadDisponible(d.cantidadDisponible());
        e.setEstado(d.estado());
        return e;
    }
}