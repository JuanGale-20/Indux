package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;

public record IndicadoresRecurso(
        BigDecimal horasRequeridas,
        BigDecimal horasDisponibles,
        BigDecimal capacidad,
        BigDecimal utilizacion,
        BigDecimal holgura,
        BigDecimal deficitHoras,
        BigDecimal impacto,
        ClasificacionRestriccion clasificacion) {
}