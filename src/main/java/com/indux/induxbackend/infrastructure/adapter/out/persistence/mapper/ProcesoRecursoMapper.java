package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProcesoRecursoJpaEntity;

public final class ProcesoRecursoMapper {

    private ProcesoRecursoMapper() {
    }

    public static ProcesoRecurso toDomain(ProcesoRecursoJpaEntity e) {
        return new ProcesoRecurso(e.getProcesoId(), e.getRecursoId(), e.getTiempoReferencia());
    }

    public static ProcesoRecursoJpaEntity toEntity(ProcesoRecurso d) {
        ProcesoRecursoJpaEntity e = new ProcesoRecursoJpaEntity();
        e.setProcesoId(d.procesoId());
        e.setRecursoId(d.recursoId());
        e.setTiempoReferencia(d.tiempoReferencia());
        return e;
    }
}