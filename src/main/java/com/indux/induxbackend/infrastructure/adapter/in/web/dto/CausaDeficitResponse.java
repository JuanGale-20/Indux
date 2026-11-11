package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.CausaDeficitRecurso;
import com.indux.induxbackend.domain.model.FactorCausa;

import java.math.BigDecimal;
import java.util.List;

public record CausaDeficitResponse(
        Long recursoId,
        String codigo,
        String nombre,
        Integer prioridad,
        BigDecimal deficitHoras,
        FactorCausa causaPrincipal,
        List<DetalleFactorResponse> factores
) {
    public static CausaDeficitResponse desde(CausaDeficitRecurso c) {
        List<DetalleFactorResponse> factores = c.factores() != null
                ? c.factores().stream().map(DetalleFactorResponse::desde).toList()
                : List.of();
        return new CausaDeficitResponse(
                c.recursoId(),
                c.codigo(),
                c.nombre(),
                c.prioridad(),
                c.deficitHoras(),
                c.causaPrincipal(),
                factores
        );
    }
}
