package com.indux.induxbackend.domain.model;

import java.util.List;

public record ResultadoImportacion(
        boolean valido,
        boolean guardado,
        int filasLeidas,
        List<ReporteImportadoResumen> reportes,
        List<ErrorImportacion> errores) {
}
