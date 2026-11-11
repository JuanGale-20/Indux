package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;

import java.math.BigDecimal;

public record AnalisisRecursoResponse(
        Integer prioridad,
        Long reporteRecursoId,
        Long recursoId,
        String codigo,
        String nombre,
        BigDecimal aporteHoras,
        BigDecimal horasRequeridas,
        BigDecimal horasDisponibles,
        BigDecimal capacidad,
        BigDecimal utilizacion,
        BigDecimal holgura,
        BigDecimal deficitHoras,
        BigDecimal impacto,
        BigDecimal deficitCapacidad,
        ClasificacionRestriccion clasificacion,
        String diagnostico) {

    public static AnalisisRecursoResponse desde(AnalisisRecursoReporte r) {
        return new AnalisisRecursoResponse(
                r.prioridad(),
                r.reporteRecursoId(),
                r.recursoId(),
                r.codigo(),
                r.nombre(),
                r.aporteHoras(),
                r.horasRequeridas(),
                r.horasDisponibles(),
                r.capacidad(),
                r.utilizacion(),
                r.holgura(),
                r.deficitHoras(),
                r.impacto(),
                r.deficitCapacidad(),
                r.clasificacion(),
                r.diagnostico());
    }
}
