package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.PuntoHistoricoRecurso;

import java.util.List;

public interface AnalisisRepositoryPort {

    void reemplazarPorReporte(Long reporteId, List<AnalisisRecursoReporte> analisis);

    List<AnalisisRecursoReporte> listarPorReporte(Long reporteId);

    List<AnalisisRecursoReporte> listarPorRecurso(Long recursoId, int limite);

    List<PuntoHistoricoRecurso> listarHistoricoPorRecurso(Long recursoId, int limite);
}
