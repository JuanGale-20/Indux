package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.PuntoHistoricoRecurso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PuntoHistoricoResponse(
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
) {
    public static PuntoHistoricoResponse desde(PuntoHistoricoRecurso p) {
        return new PuntoHistoricoResponse(
                p.reporteId(),
                p.productoId(),
                p.periodoInicio(),
                p.periodoFin(),
                p.frecuencia(),
                p.utilizacion(),
                p.horasRequeridas(),
                p.horasDisponibles(),
                p.deficitHoras(),
                p.impacto(),
                p.clasificacion()
        );
    }
}
