package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ProcesoUseCase;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcesoService implements ProcesoUseCase {

    private final ProcesoRepositoryPort procesos;
    private final ProductoRepositoryPort productos;
    private final UsuarioRepositoryPort usuarios;

    @Override
    public Proceso crear(CrearProcesoComando c) {
        Proceso nuevo = new Proceso(null, c.productoId(), c.liderId(), c.codigo(),
                c.nombre(), c.secuencia(), c.unidadProcesada());

        Producto producto = buscarProducto(nuevo.productoId());

        validarLider(nuevo.liderId(), producto);

        if (procesos.existePorProductoYCodigo(nuevo.productoId(), nuevo.codigo())) {
            throw new ConflictoException(
                    "Ya existe un proceso con el código " + nuevo.codigo() + " en este producto");
        }
        if (procesos.existePorProductoYSecuencia(nuevo.productoId(), nuevo.secuencia())) {
            throw new ConflictoException(
                    "Ya existe un proceso con la secuencia " + nuevo.secuencia() + " en este producto");
        }
        return procesos.guardar(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Proceso obtener(Long id) {
        return procesos.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el proceso con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proceso> listarPorProducto(Long productoId) {
        buscarProducto(productoId);
        return procesos.listarPorProductoOrdenadoPorSecuencia(productoId);
    }

    @Override
    public Proceso actualizar(Long id, ActualizarProcesoComando c) {
        Proceso actual = obtener(id);
        Proceso nuevo = new Proceso(id, actual.productoId(), c.liderId(), c.codigo(),
                c.nombre(), c.secuencia(), c.unidadProcesada());

        Producto producto = buscarProducto(actual.productoId());
        validarLider(nuevo.liderId(), producto);

        if (!nuevo.codigo().equals(actual.codigo())
                && procesos.existePorProductoYCodigo(nuevo.productoId(), nuevo.codigo())) {
            throw new ConflictoException(
                    "Ya existe un proceso con el código " + nuevo.codigo() + " en este producto");
        }
        if (nuevo.secuencia() != actual.secuencia()
                && procesos.existePorProductoYSecuencia(nuevo.productoId(), nuevo.secuencia())) {
            throw new ConflictoException(
                    "Ya existe un proceso con la secuencia " + nuevo.secuencia() + " en este producto");
        }
        return procesos.guardar(nuevo);
    }

    @Override
    public void eliminar(Long id) {
        obtener(id);
        procesos.eliminar(id);
    }

    private Producto buscarProducto(Long productoId) {
        return productos.buscarPorId(productoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el producto con id " + productoId));
    }

    private void validarLider(Long liderId, Producto producto) {
        if (liderId == null) {
            return;
        }
        Usuario lider = usuarios.buscarPorId(liderId)
                .orElseThrow(() -> new NoEncontradoException("No existe el usuario con id " + liderId));

        if (lider.rol() != Rol.LIDER) {
            throw new DominioException("El usuario " + liderId + " no tiene el rol Líder");
        }
        if (!lider.activo()) {
            throw new DominioException("El líder asignado está inactivo");
        }
        if (!producto.empresaId().equals(lider.empresaId())) {
            throw new DominioException("El líder pertenece a otra empresa");
        }
    }
}