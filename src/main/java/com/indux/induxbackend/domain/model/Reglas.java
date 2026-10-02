package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

final class Reglas {
    private Reglas() {
    }

    static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DominioException(campo + " no puede estar vacío");
        }
        return valor.trim();
    }

    static Long idObligatorio(Long valor, String campo) {
        if (valor == null) {
            throw new DominioException(campo + " es obligatorio");
        }
        return valor;
    }
}
