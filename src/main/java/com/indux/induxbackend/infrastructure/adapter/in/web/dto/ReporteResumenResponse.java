package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.Reporte;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReporteResumenResponse(Long id, Long productoId, Long creadoPor, String plantaArea,
                                     FrecuenciaReporte frecuencia, LocalDate periodoInicio,
                                     LocalDate periodoFin, BigDecimal demanda,
                                     BigDecimal produccionReal, EstadoReporte estado) {

    public static ReporteResumenResponse desde(Reporte r) {
        return new ReporteResumenResponse(r.id(), r.productoId(), r.creadoPor(), r.plantaArea(),
                r.frecuencia(), r.periodoInicio(), r.periodoFin(), r.demanda(),
                r.produccionReal(), r.estado());
    }
}
