package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorReporteTest {

    private final ValidadorReporte validador = new ValidadorReporte();

    private Reporte reporteBase(LocalDate inicio, LocalDate fin, FrecuenciaReporte frecuencia) {
        return new Reporte(1L, 10L, 100L, "Planta 1", frecuencia, inicio, fin,
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.BORRADOR);
    }

    private Producto productoBase() {
        return new Producto(10L, 1L, "PROD-1", "Producto 1", "kg");
    }

    private Proceso procesoBase(Long id, int secuencia) {
        return new Proceso(id, 10L, null, "PROC-" + id, "Proceso " + id, secuencia, "kg", BigDecimal.ZERO, null);
    }

    private Recurso recursoBase(Long id) {
        return new Recurso(id, 1L, "REC-" + id, "Recurso " + id, "Maquina", 1, EstadoRecurso.DISPONIBLE);
    }

    private ReporteRecurso lineaBase(Long recursoId) {
        return new ReporteRecurso(null, 1L, recursoId, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);
    }

    @Test
    @DisplayName("Error crítico: el reporte no tiene recursos cargados")
    void errorSinRecursos() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(),
                Map.of(), Map.of(), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r -> r.esErrorCritico() && r.mensaje().contains("no tiene recursos cargados"));
    }

    @Test
    @DisplayName("Error crítico: el producto no tiene procesos definidos")
    void errorSinProcesos() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = lineaBase(1L);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("no tiene procesos definidos"));
    }

    @Test
    @DisplayName("Error crítico: recurso repetido en el reporte")
    void errorRecursoRepetido() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l1 = lineaBase(1L);
        ReporteRecurso l2 = lineaBase(1L);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l1, l2),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("repetido en el reporte"));
    }

    @Test
    @DisplayName("Error crítico: recurso inexistente")
    void errorRecursoInexistente() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        ReporteRecurso l = lineaBase(999L);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(), Map.of(), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r -> r.esErrorCritico() && r.mensaje().contains("recurso no existe"));
    }

    @Test
    @DisplayName("Error crítico: el recurso no está vinculado a ningún proceso de este producto")
    void errorRecursoNoVinculado() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = lineaBase(1L);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(1L, r), Map.of(), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("no está vinculado a ningún proceso"));
    }

    @Test
    @DisplayName("Error crítico: numeroTurnos * horasPorTurno > 24")
    void errorTurnosSuperan24() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = new ReporteRecurso(null, 1L, 1L, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 3, new BigDecimal("9"), 20); // 3 * 9 = 27 > 24

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("supera 24"));
    }

    @Test
    @DisplayName("Error crítico: diasOperacion mayor que los días del período")
    void errorDiasOperacionSuperanPeriodo() {
        // Período de 7 días (semanal) pero días de operación = 10
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7), FrecuenciaReporte.SEMANAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = new ReporteRecurso(null, 1L, 1L, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 2, new BigDecimal("8"), 10);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("superan los días del período"));
    }

    @Test
    @DisplayName("Error crítico: tiempoNoDisponible mayor que las horas programadas")
    void errorTiempoNoDisponibleSuperaProgramadas() {
        // Horas programadas = 2 turnos * 8 horas * 20 dias * 1 recurso = 320 h. Tiempo no disp = 350 h.
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = new ReporteRecurso(null, 1L, 1L, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), new BigDecimal("350"), 2, new BigDecimal("8"), 20);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("supera las horas programadas"));
    }

    @Test
    @DisplayName("Error crítico: falta de equivalencia entre unidades de proceso y línea")
    void errorFaltaEquivalencia() {
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        // Proceso en litros y línea en kg sin equivalencia
        Proceso p = new Proceso(1L, 10L, null, "P1", "Proc", 1, "litros", BigDecimal.ZERO, null);
        ReporteRecurso l = new ReporteRecurso(null, 1L, 1L, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(p), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg", "litros"));

        assertThat(res).anyMatch(r2 -> r2.esErrorCritico() && r2.mensaje().contains("No existe una equivalencia"));
    }

    @Test
    @DisplayName("Advertencia: la frecuencia mensual no coincide con la duración del período de 15 días")
    void advertenciaFrecuenciaNoCoincideConDuracion() {
        // Periodo de 15 días con frecuencia MENSUAL
        Reporte rep = reporteBase(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), FrecuenciaReporte.MENSUAL);
        Recurso r = recursoBase(1L);
        ReporteRecurso l = lineaBase(1L);

        List<ValidacionResultado> res = validador.validar(
                rep, productoBase(), List.of(procesoBase(1L, 1)), List.of(l),
                Map.of(1L, r), Map.of(1L, List.of(1L)), new ConversorUnidades(List.of()), Set.of("kg"));

        assertThat(res).anyMatch(r2 -> !r2.esErrorCritico() && r2.mensaje().contains("no coincide con la frecuencia MENSUAL"));
    }
}
