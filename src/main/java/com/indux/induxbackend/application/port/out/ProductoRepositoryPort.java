package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {
    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(Long id);

    List<Producto> listarPorEmpresa(Long empresaId);

    boolean existePorEmpresaYCodigo(Long empresaId, String codigo);
}
