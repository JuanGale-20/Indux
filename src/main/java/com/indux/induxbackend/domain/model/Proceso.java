package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

public record Proceso(Long id, Long productoId, Long liderId, String codigo,
                      String nombre, int secuencia, String unidadProcesada) {
    public Proceso {
        Reglas.idObligatorio(productoId, "El producto del proceso");
        codigo = Reglas.textoObligatorio(codigo, "El código del proceso");
        nombre = Reglas.textoObligatorio(nombre, "El nombre del proceso");
        unidadProcesada = Reglas.textoObligatorio(unidadProcesada, "La unidad procesada");
        if (secuencia <= 0) {
            throw new DominioException("La secuencia del proceso debe ser mayor que 0");
        }
    }
}
