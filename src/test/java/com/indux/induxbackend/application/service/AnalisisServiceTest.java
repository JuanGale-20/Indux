package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisUseCase.AnalisisComando;
import com.indux.induxbackend.application.port.in.AnalisisUseCase.RecursoAnalizado;
import com.indux.induxbackend.application.port.in.AnalisisUseCase.RecursoComando;
import com.indux.induxbackend.application.port.in.AnalisisUseCase.ResultadoAnalisis;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalisisServiceTest {

    private static RecursoComando recurso(String nombre, String tiempo) {
        return new RecursoComando(nombre, new BigDecimal(tiempo), 2, new BigDecimal("8"), 20, 1);
    }

    private static void assertNumero(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void ordenaPorPrioridadYClasifica() {
        ResultadoAnalisis r = new AnalisisService().analizar(new AnalisisComando(
                new BigDecimal("1000"),
                List.of(recurso("B", "0.2"), recurso("A", "0.5"), recurso("C", "0.3"))));

        RecursoAnalizado a = r.recursos().get(0);
        assertEquals("A", a.nombre());                       // 156.25 %
        assertEquals("C", r.recursos().get(1).nombre());     // 93.75 %
        assertEquals("B", r.recursos().get(2).nombre());     // 62.50 %
        assertEquals(1, a.prioridad());

        assertNumero("156.25", a.indicadores().utilizacion());
        assertNumero("180", a.indicadores().deficitHoras());   // 500 requeridas - 320 disponibles
        assertNumero("36", a.indicadores().impacto());          // 180 / 500
        assertNumero("360", a.deficitCapacidad());              // 1000 - 640

        assertEquals("SOBRECARGA", a.clasificacion().name());
        assertEquals("CAPACIDAD_AJUSTADA", r.recursos().get(1).clasificacion().name());
        assertEquals("BAJA_UTILIZACION", r.recursos().get(2).clasificacion().name());
        assertNumero("0", r.recursos().get(1).indicadores().deficitHoras());
    }

    @Test
    void ejemploDeLasIngenieras98DeCien() {
        // 100 horas disponibles (1 turno x 10 h x 10 días x 1 recurso), 98 requeridas
        ResultadoAnalisis r = new AnalisisService().analizar(new AnalisisComando(
                new BigDecimal("980"),
                List.of(new RecursoComando("R", new BigDecimal("0.1"), 1,
                        new BigDecimal("10"), 10, 1))));

        RecursoAnalizado x = r.recursos().get(0);
        assertNumero("98", x.indicadores().horasRequeridas());
        assertNumero("100", x.indicadores().horasDisponibles());
        assertNumero("2", x.indicadores().holgura());
        assertNumero("98", x.indicadores().utilizacion());
        assertNumero("0", x.indicadores().deficitHoras());
        assertEquals("CAPACIDAD_AJUSTADA", x.clasificacion().name());
    }

    @Test
    void conIgualImpactoPrioriza​ElMayorDeficitEnHoras() {
        // Misma utilización (156.25 %) y mismo impacto (36 %), pero Y tiene más horas faltantes
        RecursoComando x = new RecursoComando("X", new BigDecimal("0.5"), 2, new BigDecimal("8"), 20, 1);
        RecursoComando y = new RecursoComando("Y", new BigDecimal("1.0"), 4, new BigDecimal("8"), 20, 1);

        ResultadoAnalisis r = new AnalisisService().analizar(
                new AnalisisComando(new BigDecimal("1000"), List.of(x, y)));

        assertEquals("Y", r.recursos().get(0).nombre());
        assertNumero("360", r.recursos().get(0).indicadores().deficitHoras());
        assertEquals("X", r.recursos().get(1).nombre());
        assertNumero("180", r.recursos().get(1).indicadores().deficitHoras());
    }
}