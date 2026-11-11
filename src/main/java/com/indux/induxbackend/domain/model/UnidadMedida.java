package com.indux.induxbackend.domain.model;

public record UnidadMedida(String codigo, String nombre, String magnitud) {

    public UnidadMedida {
        codigo = Reglas.textoObligatorio(codigo, "El código de la unidad");
        nombre = Reglas.textoObligatorio(nombre, "El nombre de la unidad");
        magnitud = Reglas.textoObligatorio(magnitud, "La magnitud de la unidad");
    }
}
