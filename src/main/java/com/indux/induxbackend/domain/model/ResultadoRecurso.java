package com.indux.induxbackend.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoRecurso(
        EntradaRecurso entrada,
        IndicadoresRecurso indicadores,
        BigDecimal deficitCapacidad,   // unidades que faltan para cubrir la demanda
        List<Alerta> alertas,
        Integer prioridad
) {
    public ResultadoRecurso conPrioridad(int nuevaPrioridad) {
        return new ResultadoRecurso(entrada, indicadores, deficitCapacidad, alertas, nuevaPrioridad);
    }
}
