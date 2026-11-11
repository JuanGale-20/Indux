package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.NivelRecomendacion;
import com.indux.induxbackend.domain.model.Recomendacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorRecomendacionesTest {

    private final GeneradorRecomendaciones generador = new GeneradorRecomendaciones();

    @Test
    @DisplayName("Sobrecarga con prioridad 1 genera recomendación CRÍTICA y acción prioritaria")
    void sobrecargaPrioridadUno() {
        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                1L, 100L, 10L, "REC-1", "Torno",
                new BigDecimal("200"), new BigDecimal("360"), new BigDecimal("320"),
                new BigDecimal("1600"), new BigDecimal("112.50"), new BigDecimal("-40"),
                new BigDecimal("40"), new BigDecimal("11.11"), new BigDecimal("200"),
                ClasificacionRestriccion.SOBRECARGA, 1,
                "Alerta crítica: sobrecarga. Recurso compartido con 1 producto más",
                Instant.now()
        );

        List<Recomendacion> recs = generador.generar(List.of(r));

        assertThat(recs).hasSize(1);
        Recomendacion rec = recs.getFirst();
        assertThat(rec.nivel()).isEqualTo(NivelRecomendacion.CRITICA);
        assertThat(rec.prioridad()).isEqualTo(1);
        assertThat(rec.resumen()).contains("Sobrecarga: faltan 40 h (impacto 11.11 %)");
        assertThat(rec.acciones()).anyMatch(a -> a.contains("restricción principal"));
        assertThat(rec.acciones()).anyMatch(a -> a.contains("comparten este recurso"));
    }

    @Test
    @DisplayName("Sobrecarga con prioridad 2 genera recomendación ALTA sin acción de restricción principal")
    void sobrecargaPrioridadDos() {
        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                2L, 200L, 20L, "REC-2", "Fresadora",
                new BigDecimal("150"), new BigDecimal("200"), new BigDecimal("180"),
                new BigDecimal("900"), new BigDecimal("111.11"), new BigDecimal("-20"),
                new BigDecimal("20"), new BigDecimal("10.00"), new BigDecimal("100"),
                ClasificacionRestriccion.SOBRECARGA, 2,
                "Alerta crítica: sobrecarga",
                Instant.now()
        );

        List<Recomendacion> recs = generador.generar(List.of(r));

        assertThat(recs).hasSize(1);
        Recomendacion rec = recs.getFirst();
        assertThat(rec.nivel()).isEqualTo(NivelRecomendacion.ALTA);
        assertThat(rec.prioridad()).isEqualTo(2);
        assertThat(rec.acciones()).noneMatch(a -> a.contains("restricción principal"));
    }

    @Test
    @DisplayName("Capacidad ajustada genera recomendación MEDIA con holgura informada")
    void capacidadAjustada() {
        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                3L, 300L, 30L, "REC-3", "Prensa",
                new BigDecimal("300"), new BigDecimal("300"), new BigDecimal("320"),
                new BigDecimal("1000"), new BigDecimal("93.75"), new BigDecimal("20"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                ClasificacionRestriccion.CAPACIDAD_AJUSTADA, null,
                "Advertencia: capacidad muy ajustada",
                Instant.now()
        );

        List<Recomendacion> recs = generador.generar(List.of(r));

        assertThat(recs).hasSize(1);
        Recomendacion rec = recs.getFirst();
        assertThat(rec.nivel()).isEqualTo(NivelRecomendacion.MEDIA);
        assertThat(rec.prioridad()).isNull();
        assertThat(rec.resumen()).contains("holgura de 20 h");
        assertThat(rec.acciones()).anyMatch(a -> a.contains("plan de contingencia"));
    }

    @Test
    @DisplayName("Utilización adecuada genera recomendación INFORMATIVA")
    void utilizacionAdecuada() {
        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                4L, 400L, 40L, "REC-4", "Soldadora",
                new BigDecimal("270"), new BigDecimal("270"), new BigDecimal("320"),
                new BigDecimal("1000"), new BigDecimal("84.38"), new BigDecimal("50"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                ClasificacionRestriccion.UTILIZACION_ADECUADA, null,
                "Utilización adecuada",
                Instant.now()
        );

        List<Recomendacion> recs = generador.generar(List.of(r));

        assertThat(recs).hasSize(1);
        Recomendacion rec = recs.getFirst();
        assertThat(rec.nivel()).isEqualTo(NivelRecomendacion.INFORMATIVA);
        assertThat(rec.resumen()).contains("Utilización adecuada (84.38 %)");
    }

    @Test
    @DisplayName("Baja utilización genera recomendación BAJA con horas ociosas")
    void bajaUtilizacion() {
        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                5L, 500L, 50L, "REC-5", "Pintura",
                new BigDecimal("200"), new BigDecimal("200"), new BigDecimal("320"),
                new BigDecimal("1600"), new BigDecimal("62.50"), new BigDecimal("120"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                ClasificacionRestriccion.BAJA_UTILIZACION, null,
                "Baja utilización",
                Instant.now()
        );

        List<Recomendacion> recs = generador.generar(List.of(r));

        assertThat(recs).hasSize(1);
        Recomendacion rec = recs.getFirst();
        assertThat(rec.nivel()).isEqualTo(NivelRecomendacion.BAJA);
        assertThat(rec.resumen()).contains("120 h sin usar");
        assertThat(rec.acciones()).anyMatch(a -> a.contains("aprovechar la capacidad ociosa"));
    }
}
