package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.ReporteRecurso;

import java.math.BigDecimal;

public record LineaReporteResponse(Long id, Long recursoId, BigDecimal tiempoProcesamiento,
                                   String unidadReferencia, BigDecimal tiempoCalendario,
                                   BigDecimal tiempoNoDisponible, int numeroTurnos,
                                   BigDecimal horasPorTurno, int diasOperacion) {

    public static LineaReporteResponse desde(ReporteRecurso l) {
        return new LineaReporteResponse(l.id(), l.recursoId(), l.tiempoProcesamiento(),
                l.unidadReferencia(), l.tiempoCalendario(), l.tiempoNoDisponible(),
                l.numeroTurnos(), l.horasPorTurno(), l.diasOperacion());
    }
}
