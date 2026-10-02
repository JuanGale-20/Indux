package com.indux.induxbackend.domain.validacion;

import java.util.ArrayList;
import java.util.List;

public record ResultadoValidacion(List<Hallazgo> hallazgos) {

    public ResultadoValidacion {
        hallazgos = List.copyOf(hallazgos);
    }

    public boolean tieneErroresCriticos() {
        return hallazgos.stream().anyMatch(h -> h.severidad() == Severidad.ERROR_CRITICO);
    }

    public boolean puedeAnalizarse() {
        return !tieneErroresCriticos();
    }

    public List<Hallazgo> erroresCriticos() {
        return hallazgos.stream().filter(h -> h.severidad() == Severidad.ERROR_CRITICO).toList();
    }

    public List<Hallazgo> advertencias() {
        return hallazgos.stream().filter(h -> h.severidad() == Severidad.ADVERTENCIA).toList();
    }

    public EstadoValidacion estado() {
        if (tieneErroresCriticos()) {
            return EstadoValidacion.CON_ERRORES_CRITICOS;
        }
        return hallazgos.isEmpty() ? EstadoValidacion.VALIDO : EstadoValidacion.CON_ADVERTENCIAS;
    }

    public ResultadoValidacion combinar(ResultadoValidacion otro) {
        List<Hallazgo> todos = new ArrayList<>(hallazgos);
        todos.addAll(otro.hallazgos());
        return new ResultadoValidacion(todos);
    }
}
