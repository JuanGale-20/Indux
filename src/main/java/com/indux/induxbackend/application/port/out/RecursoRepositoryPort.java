package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Recurso;

import java.util.List;
import java.util.Optional;

public interface RecursoRepositoryPort {

    Recurso guardar(Recurso recurso);

    Optional<Recurso> buscarPorId(Long id);

    List<Recurso> listarPorEmpresa(Long empresaId);

    List<Recurso> listarPorProceso(Long procesoId);

    boolean existePorEmpresaYCodigo(Long empresaId, String codigo);

    void eliminar(Long id);
}