package com.indux.induxbackend.domain.model;

import java.util.List;

public record ResultadoValidacionReporte(
        EstadoReporte estado,
        int errores,
        int advertencias,
        List<ValidacionResultado> resultados) {
}
