package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.RecursoUseCase;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Recurso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecursoService implements RecursoUseCase {

    private final RecursoRepositoryPort recursos;
    private final EmpresaRepositoryPort empresas;
    private final ProcesoRecursoRepositoryPort vinculos;
    private final ProcesoRepositoryPort procesos;
    private final ProductoRepositoryPort productos;

    @Override
    public Recurso crear(CrearRecursoComando c) {
        Recurso nuevo = new Recurso(null, c.empresaId(), c.codigo(), c.nombre(), c.tipo(),
                c.cantidadDisponible(), c.estado());

        empresas.buscarPorId(nuevo.empresaId())
                .orElseThrow(() -> new NoEncontradoException("No existe la empresa con id " + nuevo.empresaId()));

        if (recursos.existePorEmpresaYCodigo(nuevo.empresaId(), nuevo.codigo())) {
            throw new ConflictoException("Ya existe un recurso con el código " + nuevo.codigo());
        }
        return recursos.guardar(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Recurso obtener(Long id) {
        return recursos.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el recurso con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recurso> listarPorEmpresa(Long empresaId) {
        return recursos.listarPorEmpresa(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recurso> listarPorProceso(Long procesoId) {
        buscarProceso(procesoId);
        return recursos.listarPorProceso(procesoId);
    }

    @Override
    public Recurso actualizar(Long id, ActualizarRecursoComando c) {
        Recurso actual = obtener(id);
        var estado = (c.estado() == null) ? actual.estado() : c.estado();
        Recurso nuevo = new Recurso(id, actual.empresaId(), c.codigo(), c.nombre(), c.tipo(),
                c.cantidadDisponible(), estado);

        if (!nuevo.codigo().equals(actual.codigo())
                && recursos.existePorEmpresaYCodigo(nuevo.empresaId(), nuevo.codigo())) {
            throw new ConflictoException("Ya existe un recurso con el código " + nuevo.codigo());
        }
        return recursos.guardar(nuevo);
    }

    @Override
    public void eliminar(Long id) {
        obtener(id); // 404 si no existe
        recursos.eliminar(id);
    }

    @Override
    public ProcesoRecurso vincular(Long recursoId, VincularProcesoComando c) {
        Recurso recurso = obtener(recursoId);
        ProcesoRecurso vinculo = new ProcesoRecurso(c.procesoId(), recursoId, c.tiempoReferencia());

        Proceso proceso = buscarProceso(vinculo.procesoId());
        Producto producto = productos.buscarPorId(proceso.productoId())
                .orElseThrow(() -> new NoEncontradoException(
                        "No existe el producto con id " + proceso.productoId()));

        if (!producto.empresaId().equals(recurso.empresaId())) {
            throw new DominioException("El proceso pertenece a otra empresa");
        }
        if (vinculos.existe(vinculo.procesoId(), recursoId)) {
            throw new ConflictoException("El recurso ya está vinculado a ese proceso");
        }
        return vinculos.vincular(vinculo);
    }

    @Override
    public void desvincular(Long recursoId, Long procesoId) {
        obtener(recursoId);
        if (!vinculos.existe(procesoId, recursoId)) {
            throw new NoEncontradoException("El recurso no está vinculado a ese proceso");
        }
        vinculos.desvincular(procesoId, recursoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoRecurso> listarVinculos(Long recursoId) {
        obtener(recursoId);
        return vinculos.listarPorRecurso(recursoId);
    }

    private Proceso buscarProceso(Long procesoId) {
        return procesos.buscarPorId(procesoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el proceso con id " + procesoId));
    }
}
