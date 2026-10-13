package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ProductoUseCase;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService implements ProductoUseCase {

    private final ProductoRepositoryPort productos;
    private final EmpresaRepositoryPort empresas;

    @Override
    public Producto crear(CrearProductoComando c) {
        // El constructor valida los campos obligatorios (errores críticos)
        Producto nuevo = new Producto(null, c.empresaId(), c.codigo(), c.nombre(), c.unidadMedida());

        empresas.buscarPorId(nuevo.empresaId())
                .orElseThrow(() -> new NoEncontradoException("No existe la empresa con id " + nuevo.empresaId()));

        if (productos.existePorEmpresaYCodigo(nuevo.empresaId(), nuevo.codigo())) {
            throw new ConflictoException("Ya existe un producto con el código " + nuevo.codigo());
        }
        return productos.guardar(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productos.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el producto con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorEmpresa(Long empresaId) {
        return productos.listarPorEmpresa(empresaId);
    }

    @Override
    public Producto actualizar(Long id, ActualizarProductoComando c) {
        Producto actual = obtener(id);
        Producto nuevo = new Producto(id, actual.empresaId(), c.codigo(), c.nombre(), c.unidadMedida());

        if (!nuevo.codigo().equals(actual.codigo())
                && productos.existePorEmpresaYCodigo(nuevo.empresaId(), nuevo.codigo())) {
            throw new ConflictoException("Ya existe un producto con el código " + nuevo.codigo());
        }
        return productos.guardar(nuevo);
    }

    @Override
    public void eliminar(Long id) {
        obtener(id); // 404 si no existe
        productos.eliminar(id);
    }
}