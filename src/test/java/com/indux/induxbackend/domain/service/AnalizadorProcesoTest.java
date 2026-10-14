package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.Alerta;
import com.indux.induxbackend.domain.model.EntradaRecurso;
import com.indux.induxbackend.domain.model.ResultadoRecurso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalizadorProcesoTest {

    private final AnalizadorProceso analizador =
            new AnalizadorProceso(new CalculadoraIndicadores(), new BigDecimal("0.10"));

    private EntradaRecurso recurso(long id, String nombre, String tiempo) {
        return new EntradaRecurso(id, "R" + id, nombre, 1,
                new BigDecimal(tiempo), 2, new BigDecimal("8"), 26);
    }

    @Test
    void generaAlertasYPriorizaSoloLosRecursosConDeficit() {
        List<ResultadoRecurso> r = analizador.analizar(
                new BigDecimal("10000"),
                List.of(recurso(1, "Empaque", "0.02"),
                        recurso(2, "Corte", "0.04"),
                        recurso(3, "Ensamblaje", "0.05")));

        ResultadoRecurso empaque = r.get(0);
        ResultadoRecurso corte = r.get(1);
        ResultadoRecurso ensamblaje = r.get(2);

        // Ensamblaje: 120,19 % de utilización, sobrecarga. 500 h requeridas vs 416 disponibles
        assertThat(ensamblaje.prioridad()).isEqualTo(1);
        assertThat(ensamblaje.deficitCapacidad()).isEqualByComparingTo("1680");
        assertThat(ensamblaje.indicadores().deficitHoras()).isEqualByComparingTo("84");
        assertThat(ensamblaje.indicadores().impacto()).isEqualByComparingTo("16.80");
        assertThat(ensamblaje.alertas())
                .containsExactly(Alerta.CAPACIDAD_INSUFICIENTE, Alerta.DEFICIT_CAPACIDAD);

        // Corte: 96,15 %, capacidad ajustada con holgura limitada. No tiene déficit, no se prioriza
        assertThat(corte.prioridad()).isNull();
        assertThat(corte.alertas())
                .containsExactly(Alerta.ALTA_UTILIZACION, Alerta.CAPACIDAD_LIMITADA);

        // Empaque: 48,08 %, baja utilización y sin prioridad
        assertThat(empaque.prioridad()).isNull();
        assertThat(empaque.alertas()).containsExactly(Alerta.CAPACIDAD_DISPONIBLE);
    }

    @Test
    void entreRecursosConDeficitPriorizaElDeMayorImpacto() {
        List<ResultadoRecurso> r = analizador.analizar(
                new BigDecimal("10000"),
                List.of(recurso(4, "A", "0.05"),    // 500 h requeridas: déficit 84, impacto 16,80 %
                        recurso(5, "B", "0.06")));  // 600 h requeridas: déficit 184, impacto 30,67 %

        assertThat(r.get(0).prioridad()).isEqualTo(2);
        assertThat(r.get(1).prioridad()).isEqualTo(1);
    }
}