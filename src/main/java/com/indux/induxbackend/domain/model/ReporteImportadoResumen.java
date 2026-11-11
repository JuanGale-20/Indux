package com.indux.induxbackend.domain.model;

import java.time.LocalDate;

public record ReporteImportadoResumen(
        String productoCodigo,
        String plantaArea,
        FrecuenciaReporte frecuencia,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        int recursos,
        Long reporteId) {
}
