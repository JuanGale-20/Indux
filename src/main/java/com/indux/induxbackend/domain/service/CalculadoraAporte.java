package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.ReporteRecurso;
import com.indux.induxbackend.domain.service.CalculadoraEntradas.EntradaProceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class CalculadoraAporte {

    private static final int ESCALA = 4;

    public BigDecimal calcular(ReporteRecurso linea,
                               List<Long> procesosVinculados,
                               Map<Long, EntradaProceso> entradas,
                               ConversorUnidades conversor) {
        if (lineasNulasOIncompletas(linea, procesosVinculados, entradas)) {
            return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
        }

        BigDecimal sumaHoras = BigDecimal.ZERO;
        for (Long procesoId : procesosVinculados) {
            EntradaProceso entrada = entradas.get(procesoId);
            if (entrada != null) {
                BigDecimal cantidadEnReferencia = conversor.convertir(
                        entrada.cantidad(), entrada.unidad(), linea.unidadReferencia());
                BigDecimal horas = cantidadEnReferencia.multiply(linea.tiempoProcesamiento());
                sumaHoras = sumaHoras.add(horas);
            }
        }
        return sumaHoras.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private static boolean lineasNulasOIncompletas(ReporteRecurso linea,
                                                  List<Long> procesosVinculados,
                                                  Map<Long, EntradaProceso> entradas) {
        return linea == null || procesosVinculados == null || procesosVinculados.isEmpty()
                || entradas == null || entradas.isEmpty();
    }
}
