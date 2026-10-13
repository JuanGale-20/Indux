package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisUseCase;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.IndicadoresRecurso;
import com.indux.induxbackend.domain.service.CalculadoraIndicadores;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalisisService implements AnalisisUseCase {

    private final CalculadoraIndicadores calculadora = new CalculadoraIndicadores();

    @Override
    public ResultadoAnalisis analizar(AnalisisComando c) {
        List<RecursoAnalizado> calculados = new ArrayList<>();

        for (RecursoComando r : c.recursos()) {
            IndicadoresRecurso ind;
            try {
                ind = calculadora.calcular(c.demanda(), r.tiempoProcesamiento(), r.turnos(),
                        r.horasPorTurno(), r.diasOperacion(), r.cantidadRecursos());
            } catch (IllegalArgumentException e) {
                throw new DominioException("Recurso \"" + r.nombre() + "\": " + e.getMessage());
            }
            // Déficit de capacidad = demanda − capacidad (si es positivo)
            BigDecimal deficit = c.demanda().subtract(ind.capacidad()).max(BigDecimal.ZERO);
            calculados.add(new RecursoAnalizado(0, r.nombre(), ind, deficit, alerta(ind)));
        }

        // Prioridad: mayor utilización, luego menor holgura, luego mayor déficit
        calculados.sort(Comparator
                .comparing((RecursoAnalizado x) -> x.indicadores().utilizacion()).reversed()
                .thenComparing(x -> x.indicadores().holgura())
                .thenComparing(RecursoAnalizado::deficitCapacidad, Comparator.reverseOrder()));

        List<RecursoAnalizado> ordenados = new ArrayList<>();
        for (int i = 0; i < calculados.size(); i++) {
            RecursoAnalizado x = calculados.get(i);
            ordenados.add(new RecursoAnalizado(i + 1, x.nombre(), x.indicadores(),
                    x.deficitCapacidad(), x.alerta()));
        }
        return new ResultadoAnalisis(ordenados);
    }

    private String alerta(IndicadoresRecurso ind) {
        return switch (ind.clasificacion()) {
            case RESTRICCION_CAPACIDAD -> "Restricción de capacidad: la demanda supera la capacidad disponible";
            case POSIBLE_RESTRICCION -> "Posible restricción: utilización alta, conviene vigilarla";
            case SIN_RESTRICCION -> "Sin alerta";
        };
    }
}