package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.IndicadoresRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraIndicadores {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public IndicadoresRecurso calcular(BigDecimal demanda,
                                       BigDecimal tiempoProcesamiento,
                                       int turnos,
                                       BigDecimal horasPorTurno,
                                       int diasOperacion,
                                       int cantidadRecursos) {

        validar(demanda, tiempoProcesamiento, turnos, horasPorTurno, diasOperacion, cantidadRecursos);

        // Horas requeridas = cantidad requerida (demanda) × tiempo de procesamiento
        BigDecimal horasRequeridas = demanda.multiply(tiempoProcesamiento);

        // Horas disponibles = turnos × horas por turno × días de operación × cantidad de recursos
        BigDecimal horasDisponibles = horasPorTurno
                .multiply(BigDecimal.valueOf(turnos))
                .multiply(BigDecimal.valueOf(diasOperacion))
                .multiply(BigDecimal.valueOf(cantidadRecursos));

        // Capacidad = horas disponibles ÷ tiempo de procesamiento
        BigDecimal capacidad = horasDisponibles.divide(tiempoProcesamiento, 4, RoundingMode.HALF_UP);

        // Utilización (%) = horas requeridas ÷ horas disponibles × 100
        BigDecimal utilizacion = horasRequeridas.multiply(CIEN)
                .divide(horasDisponibles, 2, RoundingMode.HALF_UP);

        // Holgura = horas disponibles − horas requeridas
        BigDecimal holgura = horasDisponibles.subtract(horasRequeridas);

        // Déficit de horas = horas faltantes (solo si es positivo)
        BigDecimal deficitHoras = horasRequeridas.subtract(horasDisponibles).max(BigDecimal.ZERO);

        // Impacto (%) = horas faltantes ÷ horas requeridas × 100
        BigDecimal impacto = horasRequeridas.signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : deficitHoras.multiply(CIEN).divide(horasRequeridas, 2, RoundingMode.HALF_UP);

        return new IndicadoresRecurso(
                horasRequeridas, horasDisponibles, capacidad, utilizacion, holgura,
                deficitHoras, impacto,
                ClasificacionRestriccion.desdeUtilizacion(utilizacion));
    }

    private void validar(BigDecimal demanda, BigDecimal tiempoProcesamiento, int turnos,
                         BigDecimal horasPorTurno, int diasOperacion, int cantidadRecursos) {
        if (demanda.signum() < 0) {
            throw new IllegalArgumentException("La demanda no puede ser negativa");
        }
        if (tiempoProcesamiento.signum() <= 0 || horasPorTurno.signum() <= 0
                || turnos <= 0 || diasOperacion <= 0 || cantidadRecursos <= 0) {
            throw new IllegalArgumentException("Tiempo, turnos, horas, días y recursos deben ser mayores que 0");
        }
    }
}