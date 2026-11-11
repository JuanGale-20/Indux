package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;

public record TendenciaRecurso(
        DireccionTendencia direccion,
        BigDecimal variacion,
        int periodos,
        boolean sostenida,
        String mensaje
) {}
