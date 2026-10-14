package com.indux.induxbackend.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClasificacionRestriccionTest {

    private static ClasificacionRestriccion c(String utilizacion) {
        return ClasificacionRestriccion.desdeUtilizacion(new BigDecimal(utilizacion));
    }

    @Test
    void respetaLosLimitesDeLasFranjas() {
        assertEquals(ClasificacionRestriccion.BAJA_UTILIZACION, c("79.99"));
        assertEquals(ClasificacionRestriccion.UTILIZACION_ADECUADA, c("80"));
        assertEquals(ClasificacionRestriccion.UTILIZACION_ADECUADA, c("90"));
        assertEquals(ClasificacionRestriccion.CAPACIDAD_AJUSTADA, c("90.01"));
        assertEquals(ClasificacionRestriccion.CAPACIDAD_AJUSTADA, c("100"));
        assertEquals(ClasificacionRestriccion.SOBRECARGA, c("100.01"));
    }
}
