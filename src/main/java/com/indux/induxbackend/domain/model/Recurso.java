package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

public record Recurso(Long id, Long empresaId, String codigo, String nombre, String tipo,
                      int cantidadDisponible, EstadoRecurso estado) {
    public Recurso {
        Reglas.idObligatorio(empresaId, "La empresa del recurso");
        codigo = Reglas.textoObligatorio(codigo, "El código del recurso");
        nombre = Reglas.textoObligatorio(nombre, "El nombre del recurso");
        tipo = (tipo == null || tipo.isBlank()) ? null : tipo.trim();
        if (cantidadDisponible <= 0) {
            throw new DominioException("La cantidad disponible del recurso debe ser mayor que 0");
        }
        estado = (estado == null) ? EstadoRecurso.DISPONIBLE : estado;
    }
}