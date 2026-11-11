package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record CausaDeficitRecurso(
        Long recursoId,
        String codigo,
        String nombre,
        Integer prioridad,
        BigDecimal deficitHoras,
        FactorCausa causaPrincipal,
        List<DetalleFactorCausa> factores
) {}
