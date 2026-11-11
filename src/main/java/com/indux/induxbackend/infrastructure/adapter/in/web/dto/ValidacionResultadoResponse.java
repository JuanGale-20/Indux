package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.ValidacionResultado;

public record ValidacionResultadoResponse(String grupo, String variable, String severidad, String mensaje) {

    public static ValidacionResultadoResponse desde(ValidacionResultado v) {
        return new ValidacionResultadoResponse(
                v.grupo(),
                v.variable(),
                v.severidad().name(),
                v.mensaje());
    }
}
