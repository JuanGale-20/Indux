package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Producto;

public record ProductoResponse(Long id, Long empresaId, String codigo, String nombre, String unidadMedida) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.id(), p.empresaId(), p.codigo(), p.nombre(), p.unidadMedida());
    }
}
