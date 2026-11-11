package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.DireccionTendencia;
import com.indux.induxbackend.domain.model.TendenciaRecurso;

import java.math.BigDecimal;

public record TendenciaResponse(
        DireccionTendencia direccion,
        BigDecimal variacion,
        int periodos,
        boolean sostenida,
        String mensaje
) {
    public static TendenciaResponse desde(TendenciaRecurso t) {
        return new TendenciaResponse(
                t.direccion(),
                t.variacion(),
                t.periodos(),
                t.sostenida(),
                t.mensaje()
        );
    }
}
