package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.out.*;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.AnalizadorReporte;
import com.indux.induxbackend.domain.service.ConversorUnidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CargadorContextoReporte {

    private final ReporteRepositoryPort reportes;
    private final ReporteRecursoRepositoryPort lineasRepo;
    private final ProductoRepositoryPort productos;
    private final ProcesoRepositoryPort procesosRepo;
    private final RecursoRepositoryPort recursosRepo;
    private final ProcesoRecursoRepositoryPort procesoRecursos;
    private final UnidadRepositoryPort unidadesRepo;

    public record ContextoReporte(
            Reporte reporte,
            Producto producto,
            List<Proceso> procesos,
            List<ReporteRecurso> lineas,
            Map<Long, Recurso> mapaRecursos,
            Map<Long, List<Long>> recursoAProcesos,
            ConversorUnidades conversor,
            Set<String> unidadesConocidas) {

        public AnalizadorReporte.ContextoAnalisis toAnalisis() {
            return new AnalizadorReporte.ContextoAnalisis(
                    reporte, producto, procesos, lineas, mapaRecursos, recursoAProcesos, conversor);
        }
    }

    public ContextoReporte cargar(Long reporteId) {
        Reporte reporte = reportes.buscarPorId(reporteId)
                .orElseThrow(() -> new NoEncontradoException("No existe el reporte con id " + reporteId));
        return cargar(reporte);
    }

    public ContextoReporte cargar(Reporte reporte) {
        Producto producto = productos.buscarPorId(reporte.productoId())
                .orElseThrow(() -> new NoEncontradoException("No existe el producto con id " + reporte.productoId()));

        List<Proceso> procesos = procesosRepo.listarPorProductoOrdenadoPorSecuencia(producto.id());
        Set<Long> idsProcesosProducto = procesos.stream().map(Proceso::id).collect(Collectors.toSet());

        List<ReporteRecurso> lineas = lineasRepo.listarPorReporte(reporte.id());

        Map<Long, Recurso> mapaRecursos = new HashMap<>();
        Map<Long, List<Long>> recursoAProcesos = new HashMap<>();

        for (ReporteRecurso linea : lineas) {
            recursosRepo.buscarPorId(linea.recursoId()).ifPresent(r -> mapaRecursos.put(r.id(), r));

            List<Long> procesosVinculados = procesoRecursos.listarPorRecurso(linea.recursoId()).stream()
                    .map(ProcesoRecurso::procesoId)
                    .filter(idsProcesosProducto::contains)
                    .toList();
            recursoAProcesos.put(linea.recursoId(), procesosVinculados);
        }

        List<EquivalenciaUnidad> equivalencias = unidadesRepo.listarEquivalencias(producto.empresaId(), producto.id());
        ConversorUnidades conversor = new ConversorUnidades(equivalencias);
        Set<String> unidadesConocidas = unidadesRepo.listarCodigosUnidad();

        return new ContextoReporte(reporte, producto, procesos, lineas,
                mapaRecursos, recursoAProcesos, conversor, unidadesConocidas);
    }

    public List<ContextoReporte> cargarOtrosContextos(ContextoReporte propio) {
        Long empresaId = propio.producto().empresaId();
        Set<Long> recursosPropios = propio.lineas().stream()
                .map(ReporteRecurso::recursoId)
                .collect(Collectors.toSet());

        if (recursosPropios.isEmpty()) {
            return List.of();
        }

        List<Reporte> candidatos = reportes.buscarCandidatosRecursoCompartido(
                empresaId,
                propio.producto().id(),
                propio.reporte().frecuencia(),
                propio.reporte().periodoInicio(),
                propio.reporte().periodoFin());

        // Deduplicar tomando el primero por cada productoId (la consulta viene ordenada por periodoFin DESC, id DESC)
        Map<Long, Reporte> unReportePorProducto = new LinkedHashMap<>();
        for (Reporte c : candidatos) {
            unReportePorProducto.putIfAbsent(c.productoId(), c);
        }

        List<ContextoReporte> otrosContextos = new ArrayList<>();
        for (Reporte candidato : unReportePorProducto.values()) {
            ContextoReporte ctxCandidato = cargar(candidato);
            boolean comparteRecurso = ctxCandidato.lineas().stream()
                    .anyMatch(l -> recursosPropios.contains(l.recursoId()));
            if (comparteRecurso) {
                otrosContextos.add(ctxCandidato);
            }
        }

        return otrosContextos;
    }
}
