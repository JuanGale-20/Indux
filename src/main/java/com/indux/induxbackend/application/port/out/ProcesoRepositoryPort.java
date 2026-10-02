package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Proceso;

import java.util.List;
import java.util.Optional;

public interface ProcesoRepositoryPort {
    Proceso guardar(Proceso proceso);

    Optional<Proceso> buscarPorId(Long id);

    List<Proceso> listarPorProductoOrdenadoPorSecuencia(Long productoId);

    boolean existePorProductoYCodigo(Long productoId, String codigo);

    boolean existePorProductoYSecuencia(Long productoId, int secuencia);
}
