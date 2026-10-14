package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReporteTest {

    private Reporte reporte(LocalDate inicio, LocalDate fin, String demanda) {
        return new Reporte(null, 1L, 1L, "Planta 1", FrecuenciaReporte.MENSUAL, inicio, fin,
                new BigDecimal(demanda), new BigDecimal("700"), null);
    }

    @Test
    void sinEstadoQuedaEnBorrador() {
        Reporte r = reporte(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "1000");

        assertThat(r.estado()).isEqualTo(EstadoReporte.BORRADOR);
        assertThat(r.conEstado(EstadoReporte.VALIDADO).estado()).isEqualTo(EstadoReporte.VALIDADO);
    }

    @Test
    void rechazaPeriodoInvertido() {
        assertThatThrownBy(() -> reporte(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), "1000"))
                .isInstanceOf(DominioException.class);
    }

    @Test
    void rechazaDemandaNegativa() {
        assertThatThrownBy(() -> reporte(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "-1"))
                .isInstanceOf(DominioException.class);
    }

    @Test
    void reporteRecursoRechazaHorasPorTurnoMayoresA24() {
        assertThatThrownBy(() -> new ReporteRecurso(null, null, 1L, new BigDecimal("0.5"), "horas",
                new BigDecimal("480"), null, 2, new BigDecimal("25"), 20))
                .isInstanceOf(DominioException.class);
    }
}