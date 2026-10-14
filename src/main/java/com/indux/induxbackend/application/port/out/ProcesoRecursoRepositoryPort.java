package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.ProcesoRecurso;

import java.util.List;

public interface ProcesoRecursoRepositoryPort {

    ProcesoRecurso vincular(ProcesoRecurso vinculo);

    void desvincular(Long procesoId, Long recursoId);

    boolean existe(Long procesoId, Long recursoId);

    List<ProcesoRecurso> listarPorProceso(Long procesoId);

    List<ProcesoRecurso> listarPorRecurso(Long recursoId);
}