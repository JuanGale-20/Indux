package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.AnalisisRecursoJpaEntity;

public final class AnalisisRecursoMapper {

    private AnalisisRecursoMapper() {}

    public static AnalisisRecursoReporte toDomain(AnalisisRecursoJpaEntity e, Long recursoId, String codigo, String nombre) {
        if (e == null) {
            return null;
        }
        return new AnalisisRecursoReporte(
                e.getId(),
                e.getReporteRecursoId(),
                recursoId,
                codigo,
                nombre,
                e.getAporteHoras(),
                e.getHorasRequeridas(),
                e.getHorasDisponibles(),
                e.getCapacidad(),
                e.getUtilizacion(),
                e.getHolgura(),
                e.getDeficitHoras(),
                e.getImpacto(),
                e.getDeficitCapacidad(),
                ClasificacionRestriccion.valueOf(e.getClasificacion()),
                e.getPrioridad(),
                e.getDiagnostico(),
                e.getCalculadoEn());
    }

    public static AnalisisRecursoJpaEntity toEntity(AnalisisRecursoReporte d) {
        if (d == null) {
            return null;
        }
        return new AnalisisRecursoJpaEntity(
                d.id(),
                d.reporteRecursoId(),
                d.horasRequeridas(),
                d.horasDisponibles(),
                d.capacidad(),
                d.utilizacion(),
                d.holgura(),
                d.deficitCapacidad(),
                d.clasificacion().name(),
                d.prioridad(),
                d.diagnostico(),
                d.calculadoEn(),
                d.aporteHoras(),
                d.deficitHoras(),
                d.impacto());
    }
}
