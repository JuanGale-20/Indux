package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.EstadoRecurso;
import com.indux.induxbackend.domain.model.Recurso;

public record RecursoResponse(Long id, Long empresaId, String codigo, String nombre, String tipo,
                              int cantidadDisponible, EstadoRecurso estado) {

    public static RecursoResponse desde(Recurso r) {
        return new RecursoResponse(r.id(), r.empresaId(), r.codigo(), r.nombre(), r.tipo(),
                r.cantidadDisponible(), r.estado());
    }
}
