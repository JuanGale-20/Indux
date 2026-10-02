package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.validacion.Hallazgo;
import com.indux.induxbackend.domain.validacion.Severidad;

public record HallazgoResponse(String grupo, String variable, Severidad severidad, String mensaje) {

    public static HallazgoResponse desde(Hallazgo h) {
        return new HallazgoResponse(h.grupo(), h.variable(), h.severidad(), h.mensaje());
    }
}
