package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalisisRecursoReporte(
        Long id,
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
        Integer prioridad,
        String diagnostico,
        Instant calculadoEn) {

    public AnalisisRecursoReporte conId(Long nuevoId) {
        return new AnalisisRecursoReporte(nuevoId, reporteRecursoId, recursoId, codigo, nombre,
                aporteHoras, horasRequeridas, horasDisponibles, capacidad, utilizacion, holgura,
                deficitHoras, impacto, deficitCapacidad, clasificacion, prioridad, diagnostico, calculadoEn);
    }
}
