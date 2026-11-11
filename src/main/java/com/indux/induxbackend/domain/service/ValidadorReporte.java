package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ValidadorReporte {

    private final CalculadoraEntradas calculadoraEntradas = new CalculadoraEntradas();

    public List<ValidacionResultado> validar(Reporte reporte,
                                             Producto producto,
                                             List<Proceso> procesos,
                                             List<ReporteRecurso> lineas,
                                             Map<Long, Recurso> mapaRecursos,
                                             Map<Long, List<Long>> recursoAProcesos,
                                             ConversorUnidades conversor,
                                             Set<String> unidadesConocidas) {
        List<ValidacionResultado> resultados = new ArrayList<>();

        long diasPeriodo = ChronoUnit.DAYS.between(reporte.periodoInicio(), reporte.periodoFin()) + 1;

        // Grupo: REPORTE
        validarReporte(reporte, lineas, diasPeriodo, resultados);

        // Grupo: PROCESO
        validarProcesos(procesos, lineas, recursoAProcesos, resultados);

        // Grupo: RECURSO
        validarRecursos(lineas, mapaRecursos, recursoAProcesos, diasPeriodo, resultados);

        // Grupo: UNIDADES
        validarUnidades(reporte, producto, procesos, lineas, mapaRecursos, recursoAProcesos, conversor, unidadesConocidas, resultados);

        return resultados;
    }

    private void validarReporte(Reporte reporte, List<ReporteRecurso> lineas, long diasPeriodo, List<ValidacionResultado> resultados) {
        if (lineas == null || lineas.isEmpty()) {
            resultados.add(new ValidacionResultado(
                    "REPORTE", "recursos", SeveridadValidacion.ERROR_CRITICO,
                    "El reporte no tiene recursos cargados"));
        }

        if (reporte.demanda() != null && reporte.demanda().signum() == 0) {
            resultados.add(new ValidacionResultado(
                    "REPORTE", "demanda", SeveridadValidacion.ADVERTENCIA,
                    "La demanda del reporte es 0"));
        }

        if (reporte.frecuencia() != null) {
            boolean duracionValida = switch (reporte.frecuencia()) {
                case DIARIA -> diasPeriodo == 1;
                case SEMANAL -> diasPeriodo == 7;
                case MENSUAL -> diasPeriodo >= 28 && diasPeriodo <= 31;
                case ANUAL -> diasPeriodo == 365 || diasPeriodo == 366;
            };
            if (!duracionValida) {
                resultados.add(new ValidacionResultado(
                        "REPORTE", "frecuencia", SeveridadValidacion.ADVERTENCIA,
                        "La duración del período (" + diasPeriodo + " días) no coincide con la frecuencia " + reporte.frecuencia()));
            }
        }
    }

    private void validarProcesos(List<Proceso> procesos,
                                 List<ReporteRecurso> lineas,
                                 Map<Long, List<Long>> recursoAProcesos,
                                 List<ValidacionResultado> resultados) {
        if (procesos == null || procesos.isEmpty()) {
            resultados.add(new ValidacionResultado(
                    "PROCESO", "procesos", SeveridadValidacion.ERROR_CRITICO,
                    "El producto no tiene procesos definidos"));
            return;
        }

        Set<Long> procesosCubiertos = new HashSet<>();
        if (lineas != null) {
            for (ReporteRecurso l : lineas) {
                List<Long> vinculados = recursoAProcesos.getOrDefault(l.recursoId(), Collections.emptyList());
                procesosCubiertos.addAll(vinculados);
            }
        }

        for (Proceso p : procesos) {
            if (!procesosCubiertos.contains(p.id())) {
                resultados.add(new ValidacionResultado(
                        "PROCESO", "proceso " + p.codigo(), SeveridadValidacion.ADVERTENCIA,
                        "El proceso '" + p.nombre() + "' no tiene ningún recurso en el reporte (no se analizará esa etapa)"));
            }
        }
    }

    private void validarRecursos(List<ReporteRecurso> lineas,
                                 Map<Long, Recurso> mapaRecursos,
                                 Map<Long, List<Long>> recursoAProcesos,
                                 long diasPeriodo,
                                 List<ValidacionResultado> resultados) {
        if (lineas == null) {
            return;
        }

        Set<Long> vistos = new HashSet<>();

        for (ReporteRecurso linea : lineas) {
            Recurso recurso = mapaRecursos.get(linea.recursoId());
            String codigo = recurso != null ? recurso.codigo() : String.valueOf(linea.recursoId());
            String variable = "recurso " + codigo;

            if (!vistos.add(linea.recursoId())) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "El recurso está repetido en el reporte"));
            }

            if (recurso == null) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "El recurso no existe"));
                continue;
            }

            List<Long> vinculados = recursoAProcesos.getOrDefault(recurso.id(), Collections.emptyList());
            if (vinculados.isEmpty()) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "El recurso no está vinculado a ningún proceso de este producto"));
            }

            BigDecimal horasTurnoTotal = BigDecimal.valueOf(linea.numeroTurnos()).multiply(linea.horasPorTurno());
            if (horasTurnoTotal.compareTo(BigDecimal.valueOf(24)) > 0) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "El número de turnos por las horas por turno (" + horasTurnoTotal + " h) supera 24"));
            }

            if (linea.diasOperacion() > diasPeriodo) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "Los días de operación (" + linea.diasOperacion() + ") superan los días del período (" + diasPeriodo + ")"));
            }

            BigDecimal programadas = linea.horasPorTurno()
                    .multiply(BigDecimal.valueOf(linea.numeroTurnos()))
                    .multiply(BigDecimal.valueOf(linea.diasOperacion()))
                    .multiply(BigDecimal.valueOf(recurso.cantidadDisponible()));

            if (linea.tiempoNoDisponible() != null && linea.tiempoNoDisponible().compareTo(programadas) > 0) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ERROR_CRITICO,
                        "El tiempo no disponible (" + linea.tiempoNoDisponible() + " h) supera las horas programadas (" + programadas + " h)"));
            }

            if (recurso.estado() == EstadoRecurso.NO_DISPONIBLE) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ADVERTENCIA,
                        "El recurso está en estado no disponible (se analizará con 0 horas disponibles)"));
            }

            if (recurso.tipo() == null || recurso.tipo().isBlank()) {
                resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ADVERTENCIA,
                        "El recurso no tiene tipo asignado"));
            }

            if (linea.tiempoCalendario() != null && linea.tiempoCalendario().signum() > 0) {
                BigDecimal programadasTurnos = linea.horasPorTurno()
                        .multiply(BigDecimal.valueOf(linea.numeroTurnos()))
                        .multiply(BigDecimal.valueOf(linea.diasOperacion()));
                if (programadasTurnos.compareTo(linea.tiempoCalendario()) > 0) {
                    resultados.add(new ValidacionResultado(
                        "RECURSO", variable, SeveridadValidacion.ADVERTENCIA,
                        "Las horas programadas (" + programadasTurnos + " h) superan el tiempo calendario informado (" + linea.tiempoCalendario() + " h)"));
                }
            }
        }
    }

    private void validarUnidades(Reporte reporte,
                                 Producto producto,
                                 List<Proceso> procesos,
                                 List<ReporteRecurso> lineas,
                                 Map<Long, Recurso> mapaRecursos,
                                 Map<Long, List<Long>> recursoAProcesos,
                                 ConversorUnidades conversor,
                                 Set<String> unidadesConocidas,
                                 List<ValidacionResultado> resultados) {
        Set<String> unidadesUsadas = new LinkedHashSet<>();
        if (producto != null && producto.unidadMedida() != null) {
            unidadesUsadas.add(producto.unidadMedida().trim());
        }
        if (procesos != null) {
            for (Proceso p : procesos) {
                if (p.unidadProcesada() != null) {
                    unidadesUsadas.add(p.unidadProcesada().trim());
                }
                if (p.unidadEntrada() != null && !p.unidadEntrada().isBlank()) {
                    unidadesUsadas.add(p.unidadEntrada().trim());
                }
            }
        }
        if (lineas != null) {
            for (ReporteRecurso l : lineas) {
                if (l.unidadReferencia() != null) {
                    unidadesUsadas.add(l.unidadReferencia().trim());
                }
            }
        }

        if (unidadesConocidas != null) {
            for (String u : unidadesUsadas) {
                boolean conocida = unidadesConocidas.stream().anyMatch(c -> c.equalsIgnoreCase(u));
                if (!conocida) {
                    resultados.add(new ValidacionResultado(
                            "UNIDADES", u, SeveridadValidacion.ADVERTENCIA,
                            "La unidad '" + u + "' no está en el catálogo de unidades de medida"));
                }
            }
        }

        if (producto == null || procesos == null || procesos.isEmpty() || conversor == null) {
            return;
        }

        Map<Long, CalculadoraEntradas.EntradaProceso> entradas;
        try {
            entradas = calculadoraEntradas.calcular(reporte.demanda(), producto.unidadMedida(), procesos, conversor);
        } catch (DominioException ex) {
            resultados.add(new ValidacionResultado(
                    "UNIDADES", "cadena de procesos", SeveridadValidacion.ERROR_CRITICO, ex.getMessage()));
            return;
        }

        if (lineas != null) {
            for (ReporteRecurso linea : lineas) {
                Recurso recurso = mapaRecursos.get(linea.recursoId());
                String codigo = recurso != null ? recurso.codigo() : String.valueOf(linea.recursoId());
                String variable = "recurso " + codigo;

                List<Long> vinculados = recursoAProcesos.getOrDefault(linea.recursoId(), Collections.emptyList());
                for (Long procesoId : vinculados) {
                    CalculadoraEntradas.EntradaProceso entrada = entradas.get(procesoId);
                    if (entrada != null && !conversor.puedeConvertir(entrada.unidad(), linea.unidadReferencia())) {
                        resultados.add(new ValidacionResultado(
                                "UNIDADES", variable, SeveridadValidacion.ERROR_CRITICO,
                                "No existe una equivalencia entre '" + entrada.unidad() + "' y '"
                                        + linea.unidadReferencia() + "'. Define la equivalencia de unidades para poder convertir"));
                    }
                }
            }
        }
    }
}
