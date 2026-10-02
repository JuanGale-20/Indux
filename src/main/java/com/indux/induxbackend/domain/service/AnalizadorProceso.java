package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;

import java.math.BigDecimal;
import java.util.*;

public class AnalizadorProceso {

    private static final Comparator<ResultadoRecurso> POR_GRAVEDAD =
            Comparator.comparing((ResultadoRecurso r) -> r.indicadores().utilizacion()).reversed()
                    .thenComparing(r -> r.indicadores().holgura())
                    .thenComparing(ResultadoRecurso::deficitCapacidad, Comparator.reverseOrder());

    private final CalculadoraIndicadores calculadora;
    private final BigDecimal umbralHolguraLimitada; // fracción de las horas disponibles, ej. 0.05

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
            case SIN_RESTRICCION -> alertas.add(Alerta.CAPACIDAD_DISPONIBLE);
            case POSIBLE_RESTRICCION -> alertas.add(Alerta.ALTA_UTILIZACION);
            case RESTRICCION_CAPACIDAD -> {
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

    // Regla 5.1: solo se priorizan los recursos con alguna restricción
    private List<ResultadoRecurso> priorizar(List<ResultadoRecurso> resultados) {
        List<ResultadoRecurso> conRestriccion = resultados.stream()
                .filter(r -> r.indicadores().clasificacion() != ClasificacionRestriccion.SIN_RESTRICCION)
                .sorted(POR_GRAVEDAD)
                .toList();

        Map<Long, Integer> prioridades = new HashMap<>();
        int posicion = 1;
        for (ResultadoRecurso r : conRestriccion) {
            prioridades.put(r.entrada().recursoId(), posicion++);
        }

        return resultados.stream()
                .map(r -> prioridades.containsKey(r.entrada().recursoId())
                        ? r.conPrioridad(prioridades.get(r.entrada().recursoId()))
                        : r)
                .toList();
    }
}
