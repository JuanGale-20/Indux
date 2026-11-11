package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.DetalleFactorCausa;
import com.indux.induxbackend.domain.model.FactorCausa;

import java.math.BigDecimal;

public record DetalleFactorResponse(
        FactorCausa factor,
        BigDecimal horas,
        BigDecimal porcentajeDelDeficit,
        String descripcion
) {
    public static DetalleFactorResponse desde(DetalleFactorCausa d) {
        return new DetalleFactorResponse(d.factor(), d.horas(), d.porcentajeDelDeficit(), d.descripcion());
    }
}
