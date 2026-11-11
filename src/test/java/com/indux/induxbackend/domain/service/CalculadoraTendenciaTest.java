package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraTendenciaTest {

    private final CalculadoraTendencia calculadora = new CalculadoraTendencia();

    private PuntoHistoricoRecurso crearPunto(Long reporteId, LocalDate inicio, LocalDate fin, BigDecimal utilizacion) {
        return new PuntoHistoricoRecurso(
                reporteId, 1L, inicio, fin, FrecuenciaReporte.MENSUAL,
                utilizacion, new BigDecimal("200"), new BigDecimal("320"),
                BigDecimal.ZERO, BigDecimal.ZERO, ClasificacionRestriccion.UTILIZACION_ADECUADA
        );
    }

    @Test
    @DisplayName("Menos de 2 períodos devuelve SIN_DATOS_SUFICIENTES")
    void sinDatosSuficientes() {
        PuntoHistoricoRecurso p1 = crearPunto(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("70.00"));

        HistoricoRecurso h = calculadora.calcular(10L, "REC-1", "Torno", List.of(p1));

        assertThat(h.puntos()).hasSize(1);
        assertThat(h.tendencia().direccion()).isEqualTo(DireccionTendencia.SIN_DATOS_SUFICIENTES);
        assertThat(h.tendencia().variacion()).isNull();
        assertThat(h.tendencia().periodos()).isEqualTo(1);
        assertThat(h.tendencia().sostenida()).isFalse();
        assertThat(h.tendencia().mensaje()).contains("al menos 2 períodos");
    }

    @Test
    @DisplayName("Dos períodos con subida >= 5 puntos es CRECIENTE no sostenida")
    void dosPeriodosCreciente() {
        PuntoHistoricoRecurso p1 = crearPunto(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("70.00"));
        PuntoHistoricoRecurso p2 = crearPunto(2L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), new BigDecimal("76.50"));

        HistoricoRecurso h = calculadora.calcular(10L, "REC-1", "Torno", List.of(p1, p2));

        assertThat(h.tendencia().direccion()).isEqualTo(DireccionTendencia.CRECIENTE);
        assertThat(h.tendencia().variacion()).isEqualByComparingTo("6.50");
        assertThat(h.tendencia().periodos()).isEqualTo(2);
        assertThat(h.tendencia().sostenida()).isFalse();
        assertThat(h.tendencia().mensaje()).contains("subió 6.5 puntos");
        assertThat(h.tendencia().mensaje()).doesNotContain("sostenida");
    }

    @Test
    @DisplayName("Tres períodos con subidas consecutivas >= 5 es CRECIENTE sostenida")
    void tresPeriodosCrecienteSostenida() {
        PuntoHistoricoRecurso p1 = crearPunto(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("70.00"));
        PuntoHistoricoRecurso p2 = crearPunto(2L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), new BigDecimal("76.00"));
        PuntoHistoricoRecurso p3 = crearPunto(3L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), new BigDecimal("82.00"));

        HistoricoRecurso h = calculadora.calcular(10L, "REC-1", "Torno", List.of(p1, p2, p3));

        assertThat(h.tendencia().direccion()).isEqualTo(DireccionTendencia.CRECIENTE);
        assertThat(h.tendencia().variacion()).isEqualByComparingTo("6.00");
        assertThat(h.tendencia().periodos()).isEqualTo(3);
        assertThat(h.tendencia().sostenida()).isTrue();
        assertThat(h.tendencia().mensaje()).contains("tendencia creciente sostenida en los últimos 3 períodos");
    }

    @Test
    @DisplayName("Dos períodos con variación < 5 es ESTABLE")
    void estable() {
        PuntoHistoricoRecurso p1 = crearPunto(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("80.00"));
        PuntoHistoricoRecurso p2 = crearPunto(2L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), new BigDecimal("82.00"));

        HistoricoRecurso h = calculadora.calcular(10L, "REC-1", "Torno", List.of(p1, p2));

        assertThat(h.tendencia().direccion()).isEqualTo(DireccionTendencia.ESTABLE);
        assertThat(h.tendencia().variacion()).isEqualByComparingTo("2.00");
        assertThat(h.tendencia().mensaje()).contains("se mantiene estable");
    }

    @Test
    @DisplayName("Depuración de períodos repetidos: conserva el de mayor reporteId")
    void depuracionPeriodosRepetidos() {
        PuntoHistoricoRecurso p1Viejo = crearPunto(5L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("60.00"));
        PuntoHistoricoRecurso p1Nuevo = crearPunto(9L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), new BigDecimal("75.00"));
        PuntoHistoricoRecurso p2 = crearPunto(12L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), new BigDecimal("85.00"));

        HistoricoRecurso h = calculadora.calcular(10L, "REC-1", "Torno", List.of(p1Viejo, p1Nuevo, p2));

        assertThat(h.puntos()).hasSize(2);
        assertThat(h.puntos().get(0).reporteId()).isEqualTo(9L);
        assertThat(h.puntos().get(0).utilizacion()).isEqualByComparingTo("75.00");
        assertThat(h.tendencia().variacion()).isEqualByComparingTo("10.00");
    }
}
