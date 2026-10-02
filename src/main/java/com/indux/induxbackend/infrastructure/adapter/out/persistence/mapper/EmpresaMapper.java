package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;

public final class EmpresaMapper {

    private EmpresaMapper() {
    }

    public static Empresa toDomain(EmpresaJpaEntity e) {
        return new Empresa(e.getId(), e.getNombre(), e.getSector(), e.isActiva());
    }

    public static EmpresaJpaEntity toEntity(Empresa d) {
        EmpresaJpaEntity e = new EmpresaJpaEntity();
        e.setId(d.id());
        e.setNombre(d.nombre());
        e.setSector(d.sector());
        e.setActiva(d.activa());
        return e;
    }
}
