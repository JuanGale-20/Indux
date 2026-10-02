package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;

public record EntradaRecurso(
        Long recursoId,
        String codigo,
        String nombre,
        int cantidadDisponible,
        BigDecimal tiempoProcesamiento,
        int turnos,
        BigDecimal horasPorTurno,
        int diasOperacion
) {
}
