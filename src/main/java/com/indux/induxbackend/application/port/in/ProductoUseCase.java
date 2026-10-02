package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Producto;

import java.util.List;

public interface ProductoUseCase {

    Producto crear(CrearProductoComando comando);

    Producto obtener(Long id);

    List<Producto> listarPorEmpresa(Long empresaId);

    record CrearProductoComando(Long empresaId, String codigo, String nombre, String unidadMedida) {
    }
}
