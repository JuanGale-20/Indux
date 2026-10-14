package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.IndicadoresRecurso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraIndicadoresTest {

    private final CalculadoraIndicadores calculadora = new CalculadoraIndicadores();

    @Test
    void demandaMayorQueLaCapacidadEsSobrecarga() {
        IndicadoresRecurso r = calculadora.calcular(
                new BigDecimal("10000"), new BigDecimal("0.05"), 2, new BigDecimal("8"), 26, 1);

        assertThat(r.horasRequeridas()).isEqualByComparingTo("500");
        assertThat(r.horasDisponibles()).isEqualByComparingTo("416");
        assertThat(r.capacidad()).isEqualByComparingTo("8320");
        assertThat(r.utilizacion()).isEqualByComparingTo("120.19");
        assertThat(r.holgura()).isEqualByComparingTo("-84");
        assertThat(r.deficitHoras()).isEqualByComparingTo("84");
        assertThat(r.impacto()).isEqualByComparingTo("16.80");
        assertThat(r.clasificacion()).isEqualTo(ClasificacionRestriccion.SOBRECARGA);
    }

    @Test
    void recursoConHolguraTieneBajaUtilizacion() {
        IndicadoresRecurso r = calculadora.calcular(
                new BigDecimal("5000"), new BigDecimal("0.05"), 2, new BigDecimal("8"), 26, 1);

        assertThat(r.utilizacion()).isEqualByComparingTo("60.10");
        assertThat(r.holgura()).isEqualByComparingTo("166");
        assertThat(r.deficitHoras()).isEqualByComparingTo("0");
        assertThat(r.impacto()).isEqualByComparingTo("0");
        assertThat(r.clasificacion()).isEqualTo(ClasificacionRestriccion.BAJA_UTILIZACION);
    }

    @Test
    void losLimitesDeLaClasificacionSeRespetan() {
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("79.99")))
                .isEqualTo(ClasificacionRestriccion.BAJA_UTILIZACION);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("80")))
                .isEqualTo(ClasificacionRestriccion.UTILIZACION_ADECUADA);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("90")))
                .isEqualTo(ClasificacionRestriccion.UTILIZACION_ADECUADA);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("90.01")))
                .isEqualTo(ClasificacionRestriccion.CAPACIDAD_AJUSTADA);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("100")))
                .isEqualTo(ClasificacionRestriccion.CAPACIDAD_AJUSTADA);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("100.01")))
                .isEqualTo(ClasificacionRestriccion.SOBRECARGA);
    }
}