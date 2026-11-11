package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.EstadoRecurso;
import com.indux.induxbackend.domain.model.Recurso;
import com.indux.induxbackend.domain.model.ReporteRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CalculadoraRecursoCompartido {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal UTILIZACION_MAXIMA_DISPONIBILIDAD_CERO = new BigDecimal("999999.99");

    public record DatosCalculoRecurso(
            ReporteRecurso linea,
            Recurso recurso,
            BigDecimal aporteHorasPropio,
            BigDecimal horasRequeridasTotales,
            int cantidadOtrosProductos) {}

    public List<AnalisisRecursoReporte> calcularYPriorizar(List<DatosCalculoRecurso> datos) {
        if (datos == null || datos.isEmpty()) {
            return List.of();
        }

        Instant ahora = Instant.now();
        List<AnalisisRecursoReporte> sinPriorizar = new ArrayList<>();

        for (DatosCalculoRecurso d : datos) {
            sinPriorizar.add(calcularUno(d, ahora));
        }

        // Separar con déficit y sin déficit
        List<AnalisisRecursoReporte> conDeficit = sinPriorizar.stream()
                .filter(a -> a.deficitHoras().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(AnalisisRecursoReporte::impacto).reversed()
                        .thenComparing(Comparator.comparing(AnalisisRecursoReporte::deficitHoras).reversed())
                        .thenComparing(Comparator.comparing(AnalisisRecursoReporte::utilizacion).reversed())
                        .thenComparing(AnalisisRecursoReporte::holgura))
                .toList();

        List<AnalisisRecursoReporte> sinDeficit = sinPriorizar.stream()
                .filter(a -> a.deficitHoras().compareTo(BigDecimal.ZERO) <= 0)
                .sorted(Comparator.comparing(AnalisisRecursoReporte::utilizacion).reversed())
                .toList();

        List<AnalisisRecursoReporte> resultado = new ArrayList<>();
        int prioridadActual = 1;
        for (AnalisisRecursoReporte r : conDeficit) {
            resultado.add(new AnalisisRecursoReporte(
                    r.id(), r.reporteRecursoId(), r.recursoId(), r.codigo(), r.nombre(),
                    r.aporteHoras(), r.horasRequeridas(), r.horasDisponibles(),
                    r.capacidad(), r.utilizacion(), r.holgura(), r.deficitHoras(),
                    r.impacto(), r.deficitCapacidad(), r.clasificacion(),
                    prioridadActual++, r.diagnostico(), r.calculadoEn()));
        }

        for (AnalisisRecursoReporte r : sinDeficit) {
            resultado.add(new AnalisisRecursoReporte(
                    r.id(), r.reporteRecursoId(), r.recursoId(), r.codigo(), r.nombre(),
                    r.aporteHoras(), r.horasRequeridas(), r.horasDisponibles(),
                    r.capacidad(), r.utilizacion(), r.holgura(), r.deficitHoras(),
                    r.impacto(), r.deficitCapacidad(), r.clasificacion(),
                    null, r.diagnostico(), r.calculadoEn()));
        }

        return resultado;
    }

    private AnalisisRecursoReporte calcularUno(DatosCalculoRecurso d, Instant ahora) {
        ReporteRecurso linea = d.linea();
        Recurso recurso = d.recurso();
        BigDecimal horasRequeridas = d.horasRequeridasTotales().setScale(4, RoundingMode.HALF_UP);
        BigDecimal aporteHoras = d.aporteHorasPropio().setScale(4, RoundingMode.HALF_UP);

        BigDecimal horasDisponibles;
        if (recurso != null && recurso.estado() == EstadoRecurso.NO_DISPONIBLE) {
            horasDisponibles = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        } else {
            int cantidad = recurso != null ? recurso.cantidadDisponible() : 1;
            BigDecimal turnos = BigDecimal.valueOf(linea.numeroTurnos());
            BigDecimal dias = BigDecimal.valueOf(linea.diasOperacion());
            BigDecimal cant = BigDecimal.valueOf(cantidad);

            BigDecimal programadas = linea.horasPorTurno()
                    .multiply(turnos)
                    .multiply(dias)
                    .multiply(cant);

            BigDecimal noDisponible = linea.tiempoNoDisponible() != null
                    ? linea.tiempoNoDisponible()
                    : BigDecimal.ZERO;

            horasDisponibles = programadas.subtract(noDisponible)
                    .max(BigDecimal.ZERO)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal tiempoProc = linea.tiempoProcesamiento();
        BigDecimal capacidad = horasDisponibles.divide(tiempoProc, 4, RoundingMode.HALF_UP);

        BigDecimal utilizacion;
        if (horasDisponibles.signum() == 0) {
            utilizacion = horasRequeridas.signum() > 0
                    ? UTILIZACION_MAXIMA_DISPONIBILIDAD_CERO
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            utilizacion = horasRequeridas.multiply(CIEN)
                    .divide(horasDisponibles, 2, RoundingMode.HALF_UP);
        }

        BigDecimal holgura = horasDisponibles.subtract(horasRequeridas).setScale(4, RoundingMode.HALF_UP);
        BigDecimal deficitHoras = horasRequeridas.subtract(horasDisponibles).max(BigDecimal.ZERO)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal impacto;
        if (horasRequeridas.signum() == 0) {
            impacto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            impacto = deficitHoras.multiply(CIEN).divide(horasRequeridas, 2, RoundingMode.HALF_UP);
        }

        BigDecimal deficitCapacidad = deficitHoras.divide(tiempoProc, 4, RoundingMode.HALF_UP);

        ClasificacionRestriccion clasificacion = ClasificacionRestriccion.desdeUtilizacion(utilizacion);

        String diagnostico = generarDiagnostico(clasificacion, d.cantidadOtrosProductos());

        String codigo = recurso != null ? recurso.codigo() : "";
        String nombre = recurso != null ? recurso.nombre() : "";
        Long recursoId = recurso != null ? recurso.id() : linea.recursoId();

        return new AnalisisRecursoReporte(
                null, linea.id(), recursoId, codigo, nombre,
                aporteHoras, horasRequeridas, horasDisponibles,
                capacidad, utilizacion, holgura, deficitHoras,
                impacto, deficitCapacidad, clasificacion,
                null, diagnostico, ahora);
    }

    private String generarDiagnostico(ClasificacionRestriccion clasificacion, int otrosProductos) {
        String base = switch (clasificacion) {
            case SOBRECARGA -> "Alerta crítica: sobrecarga, la demanda supera la capacidad disponible";
            case CAPACIDAD_AJUSTADA -> "Advertencia: capacidad muy ajustada, poca holgura. Conviene monitorear y prevenir";
            case UTILIZACION_ADECUADA -> "Utilización adecuada: mantener y monitorear";
            case BAJA_UTILIZACION -> "Baja utilización: evaluar redistribución o aprovechamiento de capacidad";
        };

        if (otrosProductos > 0) {
            base += ". Recurso compartido con " + otrosProductos
                    + (otrosProductos == 1 ? " producto más" : " productos más");
        }
        return base;
    }
}
