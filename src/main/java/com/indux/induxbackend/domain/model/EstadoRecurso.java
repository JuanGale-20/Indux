package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

public enum EstadoRecurso {
    DISPONIBLE,
    NO_DISPONIBLE;

    /** Convierte un texto al estado. Nulo o vacío devuelve null; un valor inválido lanza DominioException. */
    public static EstadoRecurso desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DominioException("Estado de recurso inválido: " + valor
                    + ". Valores permitidos: DISPONIBLE, NO_DISPONIBLE");
        }
    }
}