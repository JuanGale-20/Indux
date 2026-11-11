package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.Proceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class CalculadoraEntradas {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final int ESCALA = 6;

    public record EntradaProceso(Long procesoId, BigDecimal cantidad, String unidad) {}

    public Map<Long, EntradaProceso> calcular(BigDecimal demanda,
                                              String unidadProducto,
                                              List<Proceso> procesosOrdenados,
                                              ConversorUnidades conversor) {
        if (procesosOrdenados == null || procesosOrdenados.isEmpty()) {
            return Collections.emptyMap();
        }

        Proceso ultimo = procesosOrdenados.get(procesosOrdenados.size() - 1);
        BigDecimal salida = conversor.convertir(demanda, unidadProducto, ultimo.unidadProcesada());

        Map<Long, EntradaProceso> resultado = new LinkedHashMap<>();

        for (int i = procesosOrdenados.size() - 1; i >= 0; i--) {
            Proceso p = procesosOrdenados.get(i);
            String unidadEntrada = (p.unidadEntrada() != null && !p.unidadEntrada().isBlank())
                    ? p.unidadEntrada().trim()
                    : p.unidadProcesada().trim();

            BigDecimal base = conversor.convertir(salida, p.unidadProcesada(), unidadEntrada);

            BigDecimal merma = p.merma() != null ? p.merma() : BigDecimal.ZERO;
            BigDecimal unoMenosMerma = BigDecimal.ONE.subtract(merma.divide(CIEN, 10, RoundingMode.HALF_UP));
            BigDecimal entrada = base.divide(unoMenosMerma, ESCALA, RoundingMode.HALF_UP);

            resultado.put(p.id(), new EntradaProceso(p.id(), entrada, unidadEntrada));

            if (i > 0) {
                Proceso anterior = procesosOrdenados.get(i - 1);
                salida = conversor.convertir(entrada, unidadEntrada, anterior.unidadProcesada());
            }
        }

        return resultado;
    }
}
