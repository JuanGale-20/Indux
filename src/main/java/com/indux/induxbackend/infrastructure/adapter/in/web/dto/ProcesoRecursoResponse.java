package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.ProcesoRecurso;

import java.math.BigDecimal;

public record ProcesoRecursoResponse(Long procesoId, Long recursoId, BigDecimal tiempoReferencia) {

    public static ProcesoRecursoResponse desde(ProcesoRecurso v) {
        return new ProcesoRecursoResponse(v.procesoId(), v.recursoId(), v.tiempoReferencia());
    }
}