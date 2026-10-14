package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.math.BigDecimal;

public record ProcesoRecurso(Long procesoId, Long recursoId, BigDecimal tiempoReferencia) {
    public ProcesoRecurso {
        Reglas.idObligatorio(procesoId, "El proceso");
        Reglas.idObligatorio(recursoId, "El recurso");
        if (tiempoReferencia != null && tiempoReferencia.signum() <= 0) {
            throw new DominioException("El tiempo de referencia debe ser mayor que 0");
        }
    }
}