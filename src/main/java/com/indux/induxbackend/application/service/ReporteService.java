package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.out.*;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteService implements ReporteUseCase {

    private final ReporteRepositoryPort reportes;
    private final ReporteRecursoRepositoryPort lineas;
    private final ProductoRepositoryPort productos;
    private final RecursoRepositoryPort recursos;
    private final ProcesoRepositoryPort procesosRepo;
    private final ProcesoRecursoRepositoryPort procesoRecursos;

    @Autowired
    public ReporteService(ReporteRepositoryPort reportes,
                          ReporteRecursoRepositoryPort lineas,
                          ProductoRepositoryPort productos,
                          RecursoRepositoryPort recursos,
                          ProcesoRepositoryPort procesosRepo,
                          ProcesoRecursoRepositoryPort procesoRecursos) {
        this.reportes = reportes;
        this.lineas = lineas;
        this.productos = productos;
        this.recursos = recursos;
        this.procesosRepo = procesosRepo;
        this.procesoRecursos = procesoRecursos;
    }

    public ReporteService(ReporteRepositoryPort reportes,
                          ReporteRecursoRepositoryPort lineas,
                          ProductoRepositoryPort productos,
                          RecursoRepositoryPort recursos) {
        this(reportes, lineas, productos, recursos, null, null);
    }

    @Override
    public DetalleReporte crear(CrearReporteComando c) {
        Reporte nuevo = new Reporte(null, c.productoId(), c.creadoPor(), c.plantaArea(), c.frecuencia(),
                c.periodoInicio(), c.periodoFin(), c.demanda(), c.produccionReal(), EstadoReporte.BORRADOR);

        Producto producto = buscarProducto(c.productoId());
        List<ReporteRecurso> nuevasLineas = construirLineas(c.lineas(), producto.empresaId());

        Reporte guardado = reportes.guardar(nuevo);
        nuevasLineas.forEach(l -> lineas.guardar(l.conReporte(guardado.id())));
        return new DetalleReporte(guardado, lineas.listarPorReporte(guardado.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleReporte obtener(Long id) {
        Reporte reporte = buscar(id);
        return new DetalleReporte(reporte, lineas.listarPorReporte(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reporte> listarPorProducto(Long productoId) {
        buscarProducto(productoId);
        return reportes.listarPorProducto(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reporte> listarPorEmpresa(Long empresaId) {
        return reportes.listarPorEmpresa(empresaId);
    }

    @Override
    public DetalleReporte actualizar(Long id, ActualizarReporteComando c) {
        Reporte actual = buscar(id);
        Reporte nuevo = new Reporte(id, actual.productoId(), actual.creadoPor(), c.plantaArea(),
                c.frecuencia(), c.periodoInicio(), c.periodoFin(), c.demanda(), c.produccionReal(),
                EstadoReporte.BORRADOR);

        List<ReporteRecurso> nuevasLineas = null;
        if (c.lineas() != null) {
            Producto producto = buscarProducto(actual.productoId());
            nuevasLineas = construirLineas(c.lineas(), producto.empresaId());
        }

        Reporte guardado = reportes.guardar(nuevo);
        if (nuevasLineas != null) {
            lineas.eliminarPorReporte(id);
            nuevasLineas.forEach(l -> lineas.guardar(l.conReporte(id)));
        }
        return new DetalleReporte(guardado, lineas.listarPorReporte(id));
    }

    @Override
    public void eliminar(Long id) {
        buscar(id);
        lineas.eliminarPorReporte(id);
        reportes.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> recursosDelLider(Long reporteId, Long liderId) {
        if (procesosRepo == null || procesoRecursos == null) {
            return Collections.emptySet();
        }
        Reporte reporte = buscar(reporteId);
        List<Proceso> procesos = procesosRepo.listarPorProductoOrdenadoPorSecuencia(reporte.productoId());
        Set<Long> procesosDelLider = procesos.stream()
                .filter(p -> Objects.equals(p.liderId(), liderId))
                .map(Proceso::id)
                .collect(Collectors.toSet());

        if (procesosDelLider.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> recursosIds = new HashSet<>();
        for (Long procId : procesosDelLider) {
            procesoRecursos.listarPorProceso(procId).forEach(pr -> recursosIds.add(pr.recursoId()));
        }
        return recursosIds;
    }

    private Reporte buscar(Long id) {
        return reportes.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el reporte con id " + id));
    }

    private Producto buscarProducto(Long productoId) {
        return productos.buscarPorId(productoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el producto con id " + productoId));
    }

    private List<ReporteRecurso> construirLineas(List<LineaComando> comandos, Long empresaId) {
        if (comandos == null) {
            return List.of();
        }
        Set<Long> vistos = new HashSet<>();
        return comandos.stream().map(l -> {
            if (!vistos.add(l.recursoId())) {
                throw new DominioException("El recurso " + l.recursoId() + " está repetido en el reporte");
            }
            Recurso recurso = recursos.buscarPorId(l.recursoId())
                    .orElseThrow(() -> new NoEncontradoException(
                            "No existe el recurso con id " + l.recursoId()));
            if (!Objects.equals(recurso.empresaId(), empresaId)) {
                throw new DominioException("El recurso " + l.recursoId()
                        + " no pertenece a la empresa del producto");
            }
            return new ReporteRecurso(null, null, l.recursoId(), l.tiempoProcesamiento(),
                    l.unidadReferencia(), l.tiempoCalendario(), l.tiempoNoDisponible(),
                    l.numeroTurnos(), l.horasPorTurno(), l.diasOperacion());
        }).toList();
    }
}
