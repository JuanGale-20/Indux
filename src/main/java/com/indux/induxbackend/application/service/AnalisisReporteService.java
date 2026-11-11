package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisReporteUseCase;
import com.indux.induxbackend.application.port.out.AnalisisRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ReporteRepositoryPort;
import com.indux.induxbackend.application.port.out.ValidacionRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalisisReporteService implements AnalisisReporteUseCase {

    private final CargadorContextoReporte cargadorContexto;
    private final ReporteRepositoryPort reportes;
    private final ValidacionRepositoryPort validacionesRepo;
    private final AnalisisRepositoryPort analisisRepo;
    private final ProcesoRecursoRepositoryPort procesoRecursos;

    private final ValidadorReporte validador = new ValidadorReporte();
    private final AnalizadorReporte analizador = new AnalizadorReporte();
    private final GeneradorRecomendaciones generadorRecomendaciones = new GeneradorRecomendaciones();
    private final DetectorCausas detectorCausas = new DetectorCausas();
    private final CalculadoraEntradas calculadoraEntradas = new CalculadoraEntradas();
    private final CalculadoraAporte calculadoraAporte = new CalculadoraAporte();

    @Override
    public ResultadoValidacionReporte validar(Long reporteId) {
        CargadorContextoReporte.ContextoReporte ctx = cargadorContexto.cargar(reporteId);
        List<ValidacionResultado> resultados = validador.validar(
                ctx.reporte(), ctx.producto(), ctx.procesos(), ctx.lineas(),
                ctx.mapaRecursos(), ctx.recursoAProcesos(), ctx.conversor(), ctx.unidadesConocidas());

        validacionesRepo.reemplazarPorReporte(reporteId, resultados);

        int errores = (int) resultados.stream().filter(ValidacionResultado::esErrorCritico).count();
        int advertencias = resultados.size() - errores;

        EstadoReporte nuevoEstado = errores > 0 ? EstadoReporte.CON_ERRORES : EstadoReporte.VALIDADO;
        reportes.guardar(ctx.reporte().conEstado(nuevoEstado));

        return new ResultadoValidacionReporte(nuevoEstado, errores, advertencias, resultados);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidacionResultado> obtenerValidaciones(Long reporteId) {
        verificarExistenciaReporte(reporteId);
        return validacionesRepo.listarPorReporte(reporteId);
    }

    @Override
    public DetalleAnalisisReporte analizar(Long reporteId) {
        CargadorContextoReporte.ContextoReporte ctx = cargadorContexto.cargar(reporteId);
        List<ValidacionResultado> resultados = validador.validar(
                ctx.reporte(), ctx.producto(), ctx.procesos(), ctx.lineas(),
                ctx.mapaRecursos(), ctx.recursoAProcesos(), ctx.conversor(), ctx.unidadesConocidas());

        validacionesRepo.reemplazarPorReporte(reporteId, resultados);

        boolean tieneErroresCriticos = resultados.stream().anyMatch(ValidacionResultado::esErrorCritico);
        if (tieneErroresCriticos) {
            reportes.guardar(ctx.reporte().conEstado(EstadoReporte.CON_ERRORES));
            return new DetalleAnalisisReporte(reporteId, EstadoReporte.CON_ERRORES, resultados, List.of());
        }

        List<CargadorContextoReporte.ContextoReporte> otros = cargadorContexto.cargarOtrosContextos(ctx);
        List<AnalizadorReporte.ContextoAnalisis> otrosAnalisis = otros.stream()
                .map(CargadorContextoReporte.ContextoReporte::toAnalisis)
                .toList();

        List<AnalisisRecursoReporte> calculados = analizador.analizar(ctx.toAnalisis(), otrosAnalisis);

        analisisRepo.reemplazarPorReporte(reporteId, calculados);
        reportes.guardar(ctx.reporte().conEstado(EstadoReporte.ANALIZADO));

        List<AnalisisRecursoReporte> persistidos = analisisRepo.listarPorReporte(reporteId);
        return new DetalleAnalisisReporte(reporteId, EstadoReporte.ANALIZADO, resultados, persistidos);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleAnalisisReporte obtenerAnalisis(Long reporteId) {
        Reporte reporte = reportes.buscarPorId(reporteId)
                .orElseThrow(() -> new NoEncontradoException("No existe el reporte con id " + reporteId));

        if (reporte.estado() != EstadoReporte.ANALIZADO) {
            throw new ConflictoException("El reporte no ha sido analizado");
        }

        List<ValidacionResultado> validaciones = validacionesRepo.listarPorReporte(reporteId);
        List<AnalisisRecursoReporte> recursos = analisisRepo.listarPorReporte(reporteId);
        return new DetalleAnalisisReporte(reporteId, reporte.estado(), validaciones, recursos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recomendacion> obtenerRecomendaciones(Long reporteId) {
        Reporte reporte = reportes.buscarPorId(reporteId)
                .orElseThrow(() -> new NoEncontradoException("No existe el reporte con id " + reporteId));

        if (reporte.estado() != EstadoReporte.ANALIZADO) {
            throw new ConflictoException("El reporte no ha sido analizado");
        }

        List<AnalisisRecursoReporte> recursos = analisisRepo.listarPorReporte(reporteId);
        return generadorRecomendaciones.generar(recursos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CausaDeficitRecurso> detectarCausas(Long reporteId) {
        Reporte reporte = reportes.buscarPorId(reporteId)
                .orElseThrow(() -> new NoEncontradoException("No existe el reporte con id " + reporteId));

        if (reporte.estado() != EstadoReporte.ANALIZADO) {
            throw new ConflictoException("El reporte no ha sido analizado");
        }

        List<AnalisisRecursoReporte> analisis = analisisRepo.listarPorReporte(reporteId);
        List<AnalisisRecursoReporte> conDeficit = analisis.stream()
                .filter(r -> r.deficitHoras() != null && r.deficitHoras().signum() > 0)
                .toList();

        if (conDeficit.isEmpty()) {
            return List.of();
        }

        CargadorContextoReporte.ContextoReporte ctx = cargadorContexto.cargar(reporte);
        Map<Long, ReporteRecurso> lineasPorRecurso = ctx.lineas().stream()
                .collect(Collectors.toMap(ReporteRecurso::recursoId, l -> l, (a, b) -> a));

        List<Proceso> procesosSinMerma = ctx.procesos().stream()
                .map(p -> new Proceso(p.id(), p.productoId(), p.liderId(), p.codigo(), p.nombre(), p.secuencia(),
                        p.unidadProcesada(), BigDecimal.ZERO, p.unidadEntrada()))
                .toList();
        Map<Long, CalculadoraEntradas.EntradaProceso> entradasSinMerma =
                calculadoraEntradas.calcular(ctx.reporte().demanda(), ctx.producto().unidadMedida(), procesosSinMerma, ctx.conversor());

        Set<Long> idsProcesosProducto = ctx.procesos().stream().map(Proceso::id).collect(Collectors.toSet());

        List<CausaDeficitRecurso> resultado = new ArrayList<>();
        for (AnalisisRecursoReporte r : conDeficit) {
            ReporteRecurso linea = lineasPorRecurso.get(r.recursoId());
            List<Long> procesosVinculados = ctx.recursoAProcesos().getOrDefault(r.recursoId(), List.of());

            BigDecimal aporteSinMerma = calculadoraAporte.calcular(linea, procesosVinculados, entradasSinMerma, ctx.conversor());

            BigDecimal tiempoReferencia = procesoRecursos.listarPorRecurso(r.recursoId()).stream()
                    .filter(pr -> idsProcesosProducto.contains(pr.procesoId()))
                    .map(ProcesoRecurso::tiempoReferencia)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            CausaDeficitRecurso causa = detectorCausas.detectar(r, linea, tiempoReferencia, aporteSinMerma);
            if (causa != null) {
                resultado.add(causa);
            }
        }

        return resultado;
    }

    private void verificarExistenciaReporte(Long reporteId) {
        if (reportes.buscarPorId(reporteId).isEmpty()) {
            throw new NoEncontradoException("No existe el reporte con id " + reporteId);
        }
    }
}
