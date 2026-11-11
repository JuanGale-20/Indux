package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.DetalleAnalisisReporte;
import com.indux.induxbackend.domain.model.EstadoReporte;

import java.util.List;

public record AnalisisReporteResponse(
        Long reporteId,
        EstadoReporte estado,
        List<ValidacionResultadoResponse> validaciones,
        List<AnalisisRecursoResponse> recursos) {

    public static AnalisisReporteResponse desde(DetalleAnalisisReporte d) {
        return new AnalisisReporteResponse(
                d.reporteId(),
                d.estado(),
                d.validaciones() != null ? d.validaciones().stream().map(ValidacionResultadoResponse::desde).toList() : List.of(),
                d.recursos() != null ? d.recursos().stream().map(AnalisisRecursoResponse::desde).toList() : List.of());
    }
}
