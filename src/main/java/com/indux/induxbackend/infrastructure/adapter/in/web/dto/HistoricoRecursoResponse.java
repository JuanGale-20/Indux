package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.HistoricoRecurso;

import java.util.List;

public record HistoricoRecursoResponse(
        Long recursoId,
        String codigo,
        String nombre,
        List<PuntoHistoricoResponse> puntos,
        TendenciaResponse tendencia
) {
    public static HistoricoRecursoResponse desde(HistoricoRecurso h) {
        List<PuntoHistoricoResponse> puntos = h.puntos() != null
                ? h.puntos().stream().map(PuntoHistoricoResponse::desde).toList()
                : List.of();
        TendenciaResponse tendencia = h.tendencia() != null
                ? TendenciaResponse.desde(h.tendencia())
                : null;
        return new HistoricoRecursoResponse(h.recursoId(), h.codigo(), h.nombre(), puntos, tendencia);
    }
}
