package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.IndicadoresRecurso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraIndicadoresTest {

    private final CalculadoraIndicadores calculadora = new CalculadoraIndicadores();

    @Test
    void demandaMayorQueLaCapacidadEsRestriccion() {
        IndicadoresRecurso r = calculadora.calcular(
                new BigDecimal("10000"), new BigDecimal("0.05"), 2, new BigDecimal("8"), 26, 1);

        assertThat(r.horasRequeridas()).isEqualByComparingTo("500");
        assertThat(r.horasDisponibles()).isEqualByComparingTo("416");
        assertThat(r.capacidad()).isEqualByComparingTo("8320");
        assertThat(r.utilizacion()).isEqualByComparingTo("120.19");
        assertThat(r.holgura()).isEqualByComparingTo("-84");
        assertThat(r.clasificacion()).isEqualTo(ClasificacionRestriccion.RESTRICCION_CAPACIDAD);
    }

    @Test
    void recursoConHolguraNoTieneRestriccion() {
        IndicadoresRecurso r = calculadora.calcular(
                new BigDecimal("5000"), new BigDecimal("0.05"), 2, new BigDecimal("8"), 26, 1);

        assertThat(r.utilizacion()).isEqualByComparingTo("60.10");
        assertThat(r.holgura()).isEqualByComparingTo("166");
        assertThat(r.clasificacion()).isEqualTo(ClasificacionRestriccion.SIN_RESTRICCION);
    }

    @Test
    void loslimitesDeLaClasificacionSeRespetan() {
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("79.99")))
                .isEqualTo(ClasificacionRestriccion.SIN_RESTRICCION);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("80")))
                .isEqualTo(ClasificacionRestriccion.POSIBLE_RESTRICCION);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("100")))
                .isEqualTo(ClasificacionRestriccion.POSIBLE_RESTRICCION);
        assertThat(ClasificacionRestriccion.desdeUtilizacion(new BigDecimal("100.01")))
                .isEqualTo(ClasificacionRestriccion.RESTRICCION_CAPACIDAD);
    }
}