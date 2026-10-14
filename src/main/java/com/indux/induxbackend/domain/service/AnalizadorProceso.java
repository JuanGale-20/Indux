package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;

import java.math.BigDecimal;
import java.util.*;

public class AnalizadorProceso {

    // Prioridad jerárquica (no ponderada):
    // 1) mayor impacto (horas faltantes / horas requeridas)
    // 2) mayor déficit en horas
    // 3) mayor utilización
    // 4) menor holgura
    private static final Comparator<ResultadoRecurso> POR_GRAVEDAD = (a, b) -> {
        IndicadoresRecurso x = a.indicadores();
        IndicadoresRecurso y = b.indicadores();
        int c = y.impacto().compareTo(x.impacto());
        if (c != 0) return c;
        c = y.deficitHoras().compareTo(x.deficitHoras());
        if (c != 0) return c;
        c = y.utilizacion().compareTo(x.utilizacion());
        if (c != 0) return c;
        return x.holgura().compareTo(y.holgura());
    };

    private final CalculadoraIndicadores calculadora;
    private final BigDecimal umbralHolguraLimitada; // fracción de las horas disponibles, ej. 0.10

    public AnalizadorProceso(CalculadoraIndicadores calculadora, BigDecimal umbralHolguraLimitada) {
        this.calculadora = calculadora;
        this.umbralHolguraLimitada = umbralHolguraLimitada;
    }

    public List<ResultadoRecurso> analizar(BigDecimal demanda, List<EntradaRecurso> entradas) {
        List<ResultadoRecurso> resultados = entradas.stream()
                .map(entrada -> analizarRecurso(demanda, entrada))
                .toList();
        return priorizar(resultados);
    }

    private ResultadoRecurso analizarRecurso(BigDecimal demanda, EntradaRecurso e) {
        IndicadoresRecurso indicadores = calculadora.calcular(
                demanda, e.tiempoProcesamiento(), e.turnos(),
                e.horasPorTurno(), e.diasOperacion(), e.cantidadDisponible());

        BigDecimal deficit = demanda.subtract(indicadores.capacidad()).max(BigDecimal.ZERO);

        return new ResultadoRecurso(e, indicadores, deficit, generarAlertas(indicadores), null);
    }

    private List<Alerta> generarAlertas(IndicadoresRecurso i) {
        List<Alerta> alertas = new ArrayList<>();

        switch (i.clasificacion()) {
            case BAJA_UTILIZACION -> alertas.add(Alerta.CAPACIDAD_DISPONIBLE);
            case UTILIZACION_ADECUADA -> {
                // Mantener y monitorear: sin alerta
            }
            case CAPACIDAD_AJUSTADA -> alertas.add(Alerta.ALTA_UTILIZACION);
            case SOBRECARGA -> {
                alertas.add(Alerta.CAPACIDAD_INSUFICIENTE);
                alertas.add(Alerta.DEFICIT_CAPACIDAD);
            }
        }

        BigDecimal limite = i.horasDisponibles().multiply(umbralHolguraLimitada);
        if (i.holgura().signum() >= 0 && i.holgura().compareTo(limite) <= 0) {
            alertas.add(Alerta.CAPACIDAD_LIMITADA);
        }
        return List.copyOf(alertas);
    }

    // Solo se priorizan los recursos con déficit de horas
    private List<ResultadoRecurso> priorizar(List<ResultadoRecurso> resultados) {
        List<ResultadoRecurso> conDeficit = resultados.stream()
                .filter(r -> r.indicadores().deficitHoras().signum() > 0)
                .sorted(POR_GRAVEDAD)
                .toList();

        Map<Long, Integer> prioridades = new HashMap<>();
        int posicion = 1;
        for (ResultadoRecurso r : conDeficit) {
            prioridades.put(r.entrada().recursoId(), posicion++);
        }

        return resultados.stream()
                .map(r -> prioridades.containsKey(r.entrada().recursoId())
                        ? r.conPrioridad(prioridades.get(r.entrada().recursoId()))
                        : r)
                .toList();
    }
}