package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.application.port.in.AnalisisUseCase.ResultadoAnalisis;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;

import java.math.BigDecimal;
import java.util.List;

public record AnalisisResponse(List<RecursoResultado> recursos) {

    public record RecursoResultado(int prioridad, String nombre,
                                   BigDecimal horasRequeridas, BigDecimal horasDisponibles,
                                   BigDecimal capacidad, BigDecimal utilizacion, BigDecimal holgura,
                                   BigDecimal deficitCapacidad,
                                   ClasificacionRestriccion clasificacion, String alerta) {
    }

    public static AnalisisResponse desde(ResultadoAnalisis r) {
        return new AnalisisResponse(r.recursos().stream()
                .map(x -> new RecursoResultado(x.prioridad(), x.nombre(),
                        x.indicadores().horasRequeridas(), x.indicadores().horasDisponibles(),
                        x.indicadores().capacidad(), x.indicadores().utilizacion(),
                        x.indicadores().holgura(), x.deficitCapacidad(),
                        x.clasificacion(), x.alerta()))
                .toList());
    }
}