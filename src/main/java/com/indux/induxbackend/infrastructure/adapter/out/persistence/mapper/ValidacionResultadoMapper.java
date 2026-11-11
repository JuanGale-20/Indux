package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.SeveridadValidacion;
import com.indux.induxbackend.domain.model.ValidacionResultado;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ValidacionResultadoJpaEntity;

public final class ValidacionResultadoMapper {

    private ValidacionResultadoMapper() {}

    public static ValidacionResultado toDomain(ValidacionResultadoJpaEntity e) {
        if (e == null) {
            return null;
        }
        return new ValidacionResultado(
                e.getId(),
                e.getReporteId(),
                e.getGrupo(),
                e.getVariable(),
                SeveridadValidacion.valueOf(e.getSeveridad()),
                e.getMensaje());
    }

    public static ValidacionResultadoJpaEntity toEntity(ValidacionResultado d) {
        if (d == null) {
            return null;
        }
        return new ValidacionResultadoJpaEntity(
                d.id(),
                d.reporteId(),
                d.grupo(),
                d.variable(),
                d.severidad().name(),
                d.mensaje());
    }
}
