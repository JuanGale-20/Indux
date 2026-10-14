package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.ReporteRecurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ReporteRecursoJpaEntity;

public final class ReporteRecursoMapper {

    private ReporteRecursoMapper() {
    }

    public static ReporteRecurso toDomain(ReporteRecursoJpaEntity e) {
        return new ReporteRecurso(e.getId(), e.getReporteId(), e.getRecursoId(),
                e.getTiempoProcesamiento(), e.getUnidadReferencia(), e.getTiempoCalendario(),
                e.getTiempoNoDisponible(), e.getNumeroTurnos(), e.getHorasPorTurno(),
                e.getDiasOperacion());
    }

    public static ReporteRecursoJpaEntity toEntity(ReporteRecurso d) {
        ReporteRecursoJpaEntity e = new ReporteRecursoJpaEntity();
        e.setId(d.id());
        e.setReporteId(d.reporteId());
        e.setRecursoId(d.recursoId());
        e.setTiempoProcesamiento(d.tiempoProcesamiento());
        e.setUnidadReferencia(d.unidadReferencia());
        e.setTiempoCalendario(d.tiempoCalendario());
        e.setTiempoNoDisponible(d.tiempoNoDisponible());
        e.setNumeroTurnos(d.numeroTurnos());
        e.setHorasPorTurno(d.horasPorTurno());
        e.setDiasOperacion(d.diasOperacion());
        return e;
    }
}