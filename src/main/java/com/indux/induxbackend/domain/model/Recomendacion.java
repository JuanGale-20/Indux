package com.indux.induxbackend.domain.model;

import java.util.List;

public record Recomendacion(
        Long recursoId,
        String codigo,
        String nombre,
        Integer prioridad,
        NivelRecomendacion nivel,
        String resumen,
        List<String> acciones) {
}
