package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PuntoHistoricoRecurso(
        Long reporteId,
        Long productoId,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        FrecuenciaReporte frecuencia,
        BigDecimal utilizacion,
        BigDecimal horasRequeridas,
        BigDecimal horasDisponibles,
        BigDecimal deficitHoras,
        BigDecimal impacto,
        ClasificacionRestriccion clasificacion
) {}
