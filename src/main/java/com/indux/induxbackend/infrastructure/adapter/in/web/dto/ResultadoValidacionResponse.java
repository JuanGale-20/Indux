package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.validacion.EstadoValidacion;
import com.indux.induxbackend.domain.validacion.ResultadoValidacion;

import java.util.List;

public record ResultadoValidacionResponse(EstadoValidacion estado, boolean puedeAnalizarse,
                                          List<HallazgoResponse> hallazgos) {

    public static ResultadoValidacionResponse desde(ResultadoValidacion r) {
        return new ResultadoValidacionResponse(r.estado(), r.puedeAnalizarse(),
                r.hallazgos().stream().map(HallazgoResponse::desde).toList());
    }
}
