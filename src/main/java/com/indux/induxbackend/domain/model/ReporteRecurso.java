package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.math.BigDecimal;

public record ReporteRecurso(Long id, Long reporteId, Long recursoId, BigDecimal tiempoProcesamiento,
                             String unidadReferencia, BigDecimal tiempoCalendario,
                             BigDecimal tiempoNoDisponible, int numeroTurnos,
                             BigDecimal horasPorTurno, int diasOperacion) {
    public ReporteRecurso {
        // reporteId puede ser nulo mientras se arma el reporte; se asigna al guardarlo
        Reglas.idObligatorio(recursoId, "El recurso");
        if (tiempoProcesamiento == null || tiempoProcesamiento.signum() <= 0) {
            throw new DominioException("El tiempo de procesamiento debe ser mayor que 0");
        }
        unidadReferencia = Reglas.textoObligatorio(unidadReferencia, "La unidad de referencia");
        if (tiempoCalendario == null || tiempoCalendario.signum() < 0) {
            throw new DominioException("El tiempo calendario no puede ser negativo");
        }
        if (tiempoNoDisponible != null && tiempoNoDisponible.signum() < 0) {
            throw new DominioException("El tiempo no disponible no puede ser negativo");
        }
        if (numeroTurnos <= 0) {
            throw new DominioException("El número de turnos debe ser mayor que 0");
        }
        if (horasPorTurno == null || horasPorTurno.signum() <= 0
                || horasPorTurno.compareTo(BigDecimal.valueOf(24)) > 0) {
            throw new DominioException("Las horas por turno deben estar entre 0 y 24");
        }
        if (diasOperacion <= 0) {
            throw new DominioException("Los días de operación deben ser mayores que 0");
        }
    }

    public ReporteRecurso conReporte(Long nuevoReporteId) {
        return new ReporteRecurso(id, nuevoReporteId, recursoId, tiempoProcesamiento, unidadReferencia,
                tiempoCalendario, tiempoNoDisponible, numeroTurnos, horasPorTurno, diasOperacion);
    }
}