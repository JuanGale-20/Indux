package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.CausaDeficitRecurso;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.FactorCausa;
import com.indux.induxbackend.domain.model.ReporteRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorCausasTest {

    private final DetectorCausas detector = new DetectorCausas();

    private ReporteRecurso crearLinea(BigDecimal tiempoProc, BigDecimal tiempoNoDisp) {
        return new ReporteRecurso(
                1L, 100L, 10L, tiempoProc, "kg",
                new BigDecimal("160"), tiempoNoDisp, 2, new BigDecimal("8"), 20
        );
    }

    private AnalisisRecursoReporte crearAnalisis(
            BigDecimal aporte, BigDecimal totales, BigDecimal disponibles, BigDecimal deficit, Integer prioridad) {
        return new AnalisisRecursoReporte(
                1L, 100L, 10L, "REC-1", "Torno",
                aporte, totales, disponibles,
                new BigDecimal("1600"), new BigDecimal("120"),
                disponibles.subtract(totales), deficit,
                new BigDecimal("15.00"), new BigDecimal("100"),
                ClasificacionRestriccion.SOBRECARGA, prioridad,
                "Alerta crítica", Instant.now()
        );
    }

    @Test
    @DisplayName("Sin déficit devuelve null")
    void sinDeficit() {
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("200"), new BigDecimal("200"), new BigDecimal("320"),
                BigDecimal.ZERO, null);
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), BigDecimal.ZERO);

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("200"));
        assertThat(causa).isNull();
    }

    @Test
    @DisplayName("Solo demanda cuando no hay merma, ni compartido, ni tiempo excesivo ni no disponible")
    void soloDemanda() {
        // deficit = 40 (requeridas 360, disponibles 320, aporte 360)
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("360"), new BigDecimal("360"), new BigDecimal("320"),
                new BigDecimal("40"), 1);
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), BigDecimal.ZERO);

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("360"));

        assertThat(causa).isNotNull();
        assertThat(causa.causaPrincipal()).isEqualTo(FactorCausa.DEMANDA);
        assertThat(causa.factores()).hasSize(1);
        assertThat(causa.factores().getFirst().factor()).isEqualTo(FactorCausa.DEMANDA);
        assertThat(causa.factores().getFirst().horas()).isEqualByComparingTo("40");
        assertThat(causa.factores().getFirst().porcentajeDelDeficit()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Recurso compartido genera factor con sus horas y porcentaje")
    void recursoCompartido() {
        // totales 360, aporte 200 -> compartido = 160. deficit = 40.
        // Como compartido (160) supera el deficit (40), el tope del porcentaje es 100 %.
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("200"), new BigDecimal("360"), new BigDecimal("320"),
                new BigDecimal("40"), 1);
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), BigDecimal.ZERO);

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("200"));

        assertThat(causa).isNotNull();
        assertThat(causa.causaPrincipal()).isEqualTo(FactorCausa.RECURSO_COMPARTIDO);
        assertThat(causa.factores().getFirst().horas()).isEqualByComparingTo("160");
        assertThat(causa.factores().getFirst().porcentajeDelDeficit()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("Tiempo de procesamiento: supera umbral de 10 % genera factor; bajo umbral no genera")
    void tiempoProcesamientoUmbral() {
        // ref = 0.20. t = 0.21 -> supera en 5 % (bajo umbral del 10 %)
        ReporteRecurso lineaBajo = crearLinea(new BigDecimal("0.21"), BigDecimal.ZERO);
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("360"), new BigDecimal("360"), new BigDecimal("320"),
                new BigDecimal("40"), 1);

        CausaDeficitRecurso causaBajo = detector.detectar(r, lineaBajo, new BigDecimal("0.20"), new BigDecimal("360"));
        assertThat(causaBajo.factores()).noneMatch(f -> f.factor() == FactorCausa.TIEMPO_PROCESAMIENTO);

        // ref = 0.20. t = 0.25 -> supera en 25 % (supera umbral del 10 %)
        // exceso = 360 * (0.25 - 0.20) / 0.25 = 360 * 0.05 / 0.25 = 72
        ReporteRecurso lineaSobre = crearLinea(new BigDecimal("0.25"), BigDecimal.ZERO);
        CausaDeficitRecurso causaSobre = detector.detectar(r, lineaSobre, new BigDecimal("0.20"), new BigDecimal("360"));

        assertThat(causaSobre.factores()).anyMatch(f -> f.factor() == FactorCausa.TIEMPO_PROCESAMIENTO);
        assertThat(causaSobre.factores().stream().filter(f -> f.factor() == FactorCausa.TIEMPO_PROCESAMIENTO).findFirst().get().horas())
                .isEqualByComparingTo("72");
    }

    @Test
    @DisplayName("Merma genera factor por diferencia con merma cero")
    void merma() {
        // aporte = 200, aporteSinMerma = 180 -> merma = 20
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("200"), new BigDecimal("200"), new BigDecimal("170"),
                new BigDecimal("30"), 1);
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), BigDecimal.ZERO);

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("180"));

        assertThat(causa.factores()).anyMatch(f -> f.factor() == FactorCausa.MERMA);
        assertThat(causa.factores().stream().filter(f -> f.factor() == FactorCausa.MERMA).findFirst().get().horas())
                .isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("Tiempo no disponible genera factor")
    void tiempoNoDisponible() {
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), new BigDecimal("25"));
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("300"), new BigDecimal("300"), new BigDecimal("250"),
                new BigDecimal("50"), 1);

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("300"));

        assertThat(causa.factores()).anyMatch(f -> f.factor() == FactorCausa.TIEMPO_NO_DISPONIBLE);
        assertThat(causa.factores().stream().filter(f -> f.factor() == FactorCausa.TIEMPO_NO_DISPONIBLE).findFirst().get().horas())
                .isEqualByComparingTo("25");
    }

    @Test
    @DisplayName("Factores quedan ordenados por horas de mayor a menor y el porcentaje se topa en 100")
    void ordenYTopePorcentaje() {
        // deficit = 50
        // compartido = 80 (pct 100 %)
        // tiempo no disp = 20 (pct 40 %)
        // merma = 10 (pct 20 %)
        AnalisisRecursoReporte r = crearAnalisis(
                new BigDecimal("200"), new BigDecimal("280"), new BigDecimal("230"),
                new BigDecimal("50"), 1);
        ReporteRecurso linea = crearLinea(new BigDecimal("0.2"), new BigDecimal("20"));

        CausaDeficitRecurso causa = detector.detectar(r, linea, new BigDecimal("0.2"), new BigDecimal("190"));

        assertThat(causa.causaPrincipal()).isEqualTo(FactorCausa.RECURSO_COMPARTIDO);
        assertThat(causa.factores()).hasSize(3);
        assertThat(causa.factores().get(0).factor()).isEqualTo(FactorCausa.RECURSO_COMPARTIDO);
        assertThat(causa.factores().get(0).porcentajeDelDeficit()).isEqualByComparingTo("100.00");
        assertThat(causa.factores().get(1).factor()).isEqualTo(FactorCausa.TIEMPO_NO_DISPONIBLE);
        assertThat(causa.factores().get(2).factor()).isEqualTo(FactorCausa.MERMA);
    }
}
