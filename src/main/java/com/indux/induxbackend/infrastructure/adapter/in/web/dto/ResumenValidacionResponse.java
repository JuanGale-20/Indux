package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.ResultadoValidacionReporte;

import java.util.List;

public record ResumenValidacionResponse(
        EstadoReporte estado,
        int errores,
        int advertencias,
        List<ValidacionResultadoResponse> resultados) {

    public static ResumenValidacionResponse desde(ResultadoValidacionReporte r) {
        return new ResumenValidacionResponse(
                r.estado(),
                r.errores(),
                r.advertencias(),
                r.resultados().stream().map(ValidacionResultadoResponse::desde).toList());
    }
}
