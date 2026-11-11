package com.indux.induxbackend.domain.model;

import java.util.List;

public record DetalleAnalisisReporte(
        Long reporteId,
        EstadoReporte estado,
        List<ValidacionResultado> validaciones,
        List<AnalisisRecursoReporte> recursos) {
}
