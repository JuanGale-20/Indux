package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisUseCase.AnalisisComando;
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

    @Test
    void ordenaPorUtilizacionYClasifica() {
        ResultadoAnalisis r = new AnalisisService().analizar(new AnalisisComando(
                new BigDecimal("1000"),
                List.of(recurso("B", "0.2"), recurso("A", "0.5"), recurso("C", "0.3"))));


        assertEquals("A", r.recursos().get(0).nombre());   // 156.25 %
        assertEquals("C", r.recursos().get(1).nombre());   // 93.75 %
        assertEquals("B", r.recursos().get(2).nombre());   // 62.50 %
        assertEquals(1, r.recursos().get(0).prioridad());
        assertEquals(0, new BigDecimal("156.25").compareTo(r.recursos().get(0).indicadores().utilizacion()));
        assertEquals(0, new BigDecimal("360").compareTo(r.recursos().get(0).deficitCapacidad()));
        assertEquals("RESTRICCION_CAPACIDAD", r.recursos().get(0).clasificacion().name());
        assertEquals("POSIBLE_RESTRICCION", r.recursos().get(1).clasificacion().name());
        assertEquals("SIN_RESTRICCION", r.recursos().get(2).clasificacion().name());
    }
}