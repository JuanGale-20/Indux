package com.indux.induxbackend.domain.model;


import java.math.BigDecimal;

public enum ClasificacionRestriccion {
    SIN_RESTRICCION,
    POSIBLE_RESTRICCION,
    RESTRICCION_CAPACIDAD;

    private static final BigDecimal LIMITE_ALTA_UTILIZACION = new BigDecimal("80");
    private static final BigDecimal LIMITE_CAPACIDAD = new BigDecimal("100");

    public static ClasificacionRestriccion desdeUtilizacion(BigDecimal utilizacion) {
        if (utilizacion.compareTo(LIMITE_CAPACIDAD) > 0) {
            return RESTRICCION_CAPACIDAD;
        }
        if (utilizacion.compareTo(LIMITE_ALTA_UTILIZACION) >= 0) {
            return POSIBLE_RESTRICCION;
        }
        return SIN_RESTRICCION;
    }
}
