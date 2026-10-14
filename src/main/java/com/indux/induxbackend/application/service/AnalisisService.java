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

    // Prioridad jerárquica (no ponderada):
    // 1) primero los recursos con déficit de horas
    // 2) mayor impacto (horas faltantes / horas requeridas)
    // 3) mayor déficit en horas
    // 4) mayor utilización
    // 5) menor holgura
    private static final Comparator<RecursoAnalizado> PRIORIDAD = (a, b) -> {
        IndicadoresRecurso x = a.indicadores();
        IndicadoresRecurso y = b.indicadores();
        int c = Boolean.compare(y.deficitHoras().signum() > 0, x.deficitHoras().signum() > 0);
        if (c != 0) return c;
        c = y.impacto().compareTo(x.impacto());
        if (c != 0) return c;
        c = y.deficitHoras().compareTo(x.deficitHoras());
        if (c != 0) return c;
        c = y.utilizacion().compareTo(x.utilizacion());
        if (c != 0) return c;
        return x.holgura().compareTo(y.holgura());
    };

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
            // Déficit de capacidad (en unidades) = demanda − capacidad (si es positivo)
            BigDecimal deficit = c.demanda().subtract(ind.capacidad()).max(BigDecimal.ZERO);
            calculados.add(new RecursoAnalizado(0, r.nombre(), ind, deficit, alerta(ind)));
        }

        calculados.sort(PRIORIDAD);

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
            case SOBRECARGA -> "Alerta crítica: sobrecarga, la demanda supera la capacidad disponible";
            case CAPACIDAD_AJUSTADA -> "Advertencia: capacidad muy ajustada, poca holgura. Conviene monitorear y prevenir";
            case UTILIZACION_ADECUADA -> "Utilización adecuada: mantener y monitorear";
            case BAJA_UTILIZACION -> "Baja utilización: evaluar redistribución o aprovechamiento de capacidad";
        };
    }
}