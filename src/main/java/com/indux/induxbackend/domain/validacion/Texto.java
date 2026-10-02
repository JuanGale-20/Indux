package com.indux.induxbackend.domain.validacion;

final class Texto {

    private Texto() {
    }

    static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
