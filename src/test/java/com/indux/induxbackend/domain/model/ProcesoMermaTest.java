package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcesoMermaTest {

    @Test
    void elConstructorAnteriorDejaMermaEnCeroYSinUnidadDeEntrada() {
        Proceso p = new Proceso(null, 1L, null, "COR", "Corte", 1, "unidades");

        assertThat(p.merma()).isEqualByComparingTo("0");
        assertThat(p.unidadEntrada()).isNull();
    }

    @Test
    void aceptaMermaValidaYLimpiaLaUnidadDeEntrada() {
        Proceso p = new Proceso(null, 1L, null, "COR", "Corte", 1, "unidades",
                new BigDecimal("5"), " kg ");

        assertThat(p.merma()).isEqualByComparingTo("5");
        assertThat(p.unidadEntrada()).isEqualTo("kg");
    }

    @Test
    void rechazaMermaDeCienPorCientoONegativa() {
        assertThatThrownBy(() -> new Proceso(null, 1L, null, "COR", "Corte", 1, "unidades",
                new BigDecimal("100"), null)).isInstanceOf(DominioException.class);
        assertThatThrownBy(() -> new Proceso(null, 1L, null, "COR", "Corte", 1, "unidades",
                new BigDecimal("-1"), null)).isInstanceOf(DominioException.class);
    }
}