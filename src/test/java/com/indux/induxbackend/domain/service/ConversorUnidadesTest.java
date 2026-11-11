package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.EquivalenciaUnidad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversorUnidadesTest {

    @Test
    @DisplayName("Convierte unidades iguales sin importar mayúsculas o espacios")
    void unidadesIguales() {
        ConversorUnidades conversor = new ConversorUnidades(List.of());
        BigDecimal resultado = conversor.convertir(new BigDecimal("123.45"), " KG ", "kg");
        assertThat(resultado).isEqualByComparingTo("123.45");
    }

    @Test
    @DisplayName("Convierte ton a kg y kg a ton con factor inverso calculado")
    void tonAKgYViceversa() {
        EquivalenciaUnidad eq = new EquivalenciaUnidad(1L, null, null, "ton", "kg", new BigDecimal("1000"));
        ConversorUnidades conversor = new ConversorUnidades(List.of(eq));

        BigDecimal kg = conversor.convertir(new BigDecimal("2.5"), "ton", "kg");
        assertThat(kg).isEqualByComparingTo("2500.000000");

        BigDecimal ton = conversor.convertir(new BigDecimal("5000"), "kg", "ton");
        assertThat(ton).isEqualByComparingTo("5.000000");
    }

    @Test
    @DisplayName("Convierte horas a segundos encadenando horas->minutos y minutos->segundos")
    void horasASegundosEncadenado() {
        List<EquivalenciaUnidad> eqs = List.of(
                new EquivalenciaUnidad(1L, null, null, "horas", "minutos", new BigDecimal("60")),
                new EquivalenciaUnidad(2L, null, null, "minutos", "segundos", new BigDecimal("60"))
        );
        ConversorUnidades conversor = new ConversorUnidades(eqs);

        BigDecimal segundos = conversor.convertir(new BigDecimal("2"), "horas", "segundos");
        assertThat(segundos).isEqualByComparingTo("7200.000000");
    }

    @Test
    @DisplayName("Lanza DominioException cuando no existe camino de equivalencia")
    void sinEquivalenciaLanzaError() {
        ConversorUnidades conversor = new ConversorUnidades(List.of());
        assertThatThrownBy(() -> conversor.convertir(BigDecimal.TEN, "metros", "kg"))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("No existe una equivalencia entre 'metros' y 'kg'");
    }

    @Test
    @DisplayName("La equivalencia específica de producto gana a la global")
    void equivalenciaDeProductoGanaAGlobal() {
        List<EquivalenciaUnidad> eqs = List.of(
                new EquivalenciaUnidad(1L, null, null, "lote", "unidades", new BigDecimal("100")),
                new EquivalenciaUnidad(2L, 10L, 99L, "lote", "unidades", new BigDecimal("500"))
        );
        ConversorUnidades conversor = new ConversorUnidades(eqs);

        BigDecimal unidades = conversor.convertir(new BigDecimal("2"), "lote", "unidades");
        assertThat(unidades).isEqualByComparingTo("1000.000000");
    }
}
