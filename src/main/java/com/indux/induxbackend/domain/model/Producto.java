package com.indux.induxbackend.domain.model;

public record Producto(Long id, Long empresaId, String codigo, String nombre, String unidadMedida) {
    public Producto {
        Reglas.idObligatorio(empresaId, "La empresa del producto");
        codigo = Reglas.textoObligatorio(codigo, "El código del producto");
        nombre = Reglas.textoObligatorio(nombre, "El nombre del producto");
        unidadMedida = Reglas.textoObligatorio(unidadMedida, "La unidad de medida");
    }
}
