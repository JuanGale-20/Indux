package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.CalculadoraEntradas.EntradaProceso;
import com.indux.induxbackend.domain.service.CalculadoraRecursoCompartido.DatosCalculoRecurso;

import java.math.BigDecimal;
import java.util.*;

public class AnalizadorReporte {

    private final CalculadoraEntradas calculadoraEntradas = new CalculadoraEntradas();
    private final CalculadoraAporte calculadoraAporte = new CalculadoraAporte();
    private final CalculadoraRecursoCompartido calculadoraCompartido = new CalculadoraRecursoCompartido();

    public record ContextoAnalisis(
            Reporte reporte,
            Producto producto,
            List<Proceso> procesos,
            List<ReporteRecurso> lineas,
            Map<Long, Recurso> mapaRecursos,
            Map<Long, List<Long>> recursoAProcesos,
            ConversorUnidades conversor) {

        public ReporteRecurso buscarLinea(Long recursoId) {
            if (lineas == null) {
                return null;
            }
            return lineas.stream()
                    .filter(l -> Objects.equals(l.recursoId(), recursoId))
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<AnalisisRecursoReporte> analizar(ContextoAnalisis propio, List<ContextoAnalisis> otrosContextos) {
        if (propio == null || propio.lineas() == null || propio.lineas().isEmpty()) {
            return List.of();
        }

        Map<Long, EntradaProceso> entradasPropias = calculadoraEntradas.calcular(
                propio.reporte().demanda(),
                propio.producto().unidadMedida(),
                propio.procesos(),
                propio.conversor());

        // Cachear entradas de otros contextos
        Map<Long, Map<Long, EntradaProceso>> entradasOtrosPorReporte = new HashMap<>();
        if (otrosContextos != null) {
            for (ContextoAnalisis otro : otrosContextos) {
                Map<Long, EntradaProceso> entradasOtro = calculadoraEntradas.calcular(
                        otro.reporte().demanda(),
                        otro.producto().unidadMedida(),
                        otro.procesos(),
                        otro.conversor());
                entradasOtrosPorReporte.put(otro.reporte().id(), entradasOtro);
            }
        }

        List<DatosCalculoRecurso> datos = new ArrayList<>();

        for (ReporteRecurso linea : propio.lineas()) {
            Long recursoId = linea.recursoId();
            List<Long> procesosVinculados = propio.recursoAProcesos().getOrDefault(recursoId, Collections.emptyList());

            BigDecimal aportePropio = calculadoraAporte.calcular(
                    linea, procesosVinculados, entradasPropias, propio.conversor());

            BigDecimal horasTotales = aportePropio;
            int otrosProductosCount = 0;

            if (otrosContextos != null) {
                for (ContextoAnalisis otro : otrosContextos) {
                    ReporteRecurso lineaOtro = otro.buscarLinea(recursoId);
                    if (lineaOtro != null) {
                        List<Long> procesosOtro = otro.recursoAProcesos().getOrDefault(recursoId, Collections.emptyList());
                        Map<Long, EntradaProceso> entradasOtro = entradasOtrosPorReporte.get(otro.reporte().id());
                        BigDecimal aporteOtro = calculadoraAporte.calcular(
                                lineaOtro, procesosOtro, entradasOtro, otro.conversor());
                        horasTotales = horasTotales.add(aporteOtro);
                        otrosProductosCount++;
                    }
                }
            }

            Recurso recurso = propio.mapaRecursos().get(recursoId);
            datos.add(new DatosCalculoRecurso(linea, recurso, aportePropio, horasTotales, otrosProductosCount));
        }

        return calculadoraCompartido.calcularYPriorizar(datos);
    }
}
