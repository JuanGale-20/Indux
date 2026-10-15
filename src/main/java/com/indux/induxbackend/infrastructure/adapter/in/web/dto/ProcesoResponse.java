package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Proceso;

import java.math.BigDecimal;

public record ProcesoResponse(Long id, Long productoId, Long liderId, String codigo,
                              String nombre, int secuencia, String unidadProcesada,
                              BigDecimal merma, String unidadEntrada) {

    public static ProcesoResponse desde(Proceso p) {
        return new ProcesoResponse(p.id(), p.productoId(), p.liderId(), p.codigo(),
                p.nombre(), p.secuencia(), p.unidadProcesada(), p.merma(), p.unidadEntrada());
    }
}
