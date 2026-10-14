package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.ReporteRecurso;

import java.util.List;

public interface ReporteRecursoRepositoryPort {

    ReporteRecurso guardar(ReporteRecurso reporteRecurso);

    List<ReporteRecurso> listarPorReporte(Long reporteId);

    void eliminarPorReporte(Long reporteId);
}