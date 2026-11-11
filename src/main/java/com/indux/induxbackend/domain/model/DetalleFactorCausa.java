package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;

public record DetalleFactorCausa(
        FactorCausa factor,
        BigDecimal horas,
        BigDecimal porcentajeDelDeficit,
        String descripcion
) {}
