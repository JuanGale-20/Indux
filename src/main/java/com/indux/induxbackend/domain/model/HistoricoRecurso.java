package com.indux.induxbackend.domain.model;

import java.util.List;

public record HistoricoRecurso(
        Long recursoId,
        String codigo,
        String nombre,
        List<PuntoHistoricoRecurso> puntos,
        TendenciaRecurso tendencia
) {}
