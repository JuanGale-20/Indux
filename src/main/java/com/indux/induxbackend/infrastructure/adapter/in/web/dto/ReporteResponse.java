package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.application.port.in.ReporteUseCase.DetalleReporte;
import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.Reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(Long id, Long productoId, Long creadoPor, String plantaArea,
                              FrecuenciaReporte frecuencia, LocalDate periodoInicio,
                              LocalDate periodoFin, BigDecimal demanda, BigDecimal produccionReal,
                              EstadoReporte estado, List<LineaReporteResponse> lineas) {

    public static ReporteResponse desde(DetalleReporte d) {
        Reporte r = d.reporte();
        return new ReporteResponse(r.id(), r.productoId(), r.creadoPor(), r.plantaArea(),
                r.frecuencia(), r.periodoInicio(), r.periodoFin(), r.demanda(), r.produccionReal(),
                r.estado(), d.lineas().stream().map(LineaReporteResponse::desde).toList());
    }
}
