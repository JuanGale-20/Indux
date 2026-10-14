package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;

public enum ClasificacionRestriccion {
    BAJA_UTILIZACION,       // < 80 %
    UTILIZACION_ADECUADA,   // 80 % a 90 %
    CAPACIDAD_AJUSTADA,     // > 90 % a 100 %
    SOBRECARGA;             // > 100 %

    private static final BigDecimal LIMITE_BAJA = new BigDecimal("80");
    private static final BigDecimal LIMITE_ADECUADA = new BigDecimal("90");
    private static final BigDecimal LIMITE_CAPACIDAD = new BigDecimal("100");

    public static ClasificacionRestriccion desdeUtilizacion(BigDecimal utilizacion) {
        if (utilizacion.compareTo(LIMITE_CAPACIDAD) > 0) {
            return SOBRECARGA;
        }
        if (utilizacion.compareTo(LIMITE_ADECUADA) > 0) {
            return CAPACIDAD_AJUSTADA;
        }
        if (utilizacion.compareTo(LIMITE_BAJA) >= 0) {
            return UTILIZACION_ADECUADA;
        }
        return BAJA_UTILIZACION;
    }
}