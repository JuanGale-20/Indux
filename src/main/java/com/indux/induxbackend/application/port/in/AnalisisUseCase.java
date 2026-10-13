package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.IndicadoresRecurso;

import java.math.BigDecimal;
import java.util.List;

public interface AnalisisUseCase {

    ResultadoAnalisis analizar(AnalisisComando comando);

    record RecursoComando(String nombre, BigDecimal tiempoProcesamiento, int turnos,
                          BigDecimal horasPorTurno, int diasOperacion, int cantidadRecursos) {
    }

    record AnalisisComando(BigDecimal demanda, List<RecursoComando> recursos) {
    }

    record RecursoAnalizado(int prioridad, String nombre, IndicadoresRecurso indicadores,
                            BigDecimal deficitCapacidad, String alerta) {
        public ClasificacionRestriccion clasificacion() {
            return indicadores.clasificacion();
        }
    }

    record ResultadoAnalisis(List<RecursoAnalizado> recursos) {
    }
}