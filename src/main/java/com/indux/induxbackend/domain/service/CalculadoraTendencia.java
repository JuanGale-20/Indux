package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.DireccionTendencia;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.HistoricoRecurso;
import com.indux.induxbackend.domain.model.PuntoHistoricoRecurso;
import com.indux.induxbackend.domain.model.TendenciaRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

public class CalculadoraTendencia {

    /**
     * Umbral de variación en puntos porcentuales para definir la tendencia (5 puntos).
     * Valor inicial a confirmar con las ingenieras industriales.
     */
    public static final BigDecimal UMBRAL_PUNTOS = new BigDecimal("5");

    public HistoricoRecurso calcular(Long recursoId, String codigo, String nombre, List<PuntoHistoricoRecurso> puntosBrutos) {
        List<PuntoHistoricoRecurso> depurados = depurarYOrdenar(puntosBrutos);
        TendenciaRecurso tendencia = calcularTendencia(depurados);
        return new HistoricoRecurso(recursoId, codigo, nombre, depurados, tendencia);
    }

    public List<PuntoHistoricoRecurso> depurarYOrdenar(List<PuntoHistoricoRecurso> puntos) {
        if (puntos == null || puntos.isEmpty()) {
            return List.of();
        }

        // Agrupar por (periodoInicio, periodoFin, frecuencia) y conservar el de mayor reporteId
        Map<ClavePeriodo, PuntoHistoricoRecurso> porPeriodo = new HashMap<>();
        for (PuntoHistoricoRecurso p : puntos) {
            ClavePeriodo clave = new ClavePeriodo(p.periodoInicio(), p.periodoFin(), p.frecuencia());
            PuntoHistoricoRecurso actual = porPeriodo.get(clave);
            if (actual == null || (p.reporteId() != null && (actual.reporteId() == null || p.reporteId() > actual.reporteId()))) {
                porPeriodo.put(clave, p);
            }
        }

        // Ordenar cronológicamente ascendente por inicio, luego fin, luego reporteId
        List<PuntoHistoricoRecurso> resultado = new ArrayList<>(porPeriodo.values());
        resultado.sort(Comparator
                .comparing(PuntoHistoricoRecurso::periodoInicio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PuntoHistoricoRecurso::periodoFin, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PuntoHistoricoRecurso::reporteId, Comparator.nullsLast(Comparator.naturalOrder())));

        return resultado;
    }

    public TendenciaRecurso calcularTendencia(List<PuntoHistoricoRecurso> puntos) {
        int n = (puntos == null) ? 0 : puntos.size();
        if (n < 2) {
            return new TendenciaRecurso(
                    DireccionTendencia.SIN_DATOS_SUFICIENTES,
                    null,
                    n,
                    false,
                    "Se necesitan al menos 2 períodos analizados para calcular la tendencia"
            );
        }

        PuntoHistoricoRecurso ultimo = puntos.get(n - 1);
        PuntoHistoricoRecurso anterior = puntos.get(n - 2);

        BigDecimal ultUtil = ultimo.utilizacion() != null ? ultimo.utilizacion() : BigDecimal.ZERO;
        BigDecimal antUtil = anterior.utilizacion() != null ? anterior.utilizacion() : BigDecimal.ZERO;
        BigDecimal variacion = ultUtil.subtract(antUtil).setScale(2, RoundingMode.HALF_UP);

        DireccionTendencia direccion;
        if (variacion.compareTo(UMBRAL_PUNTOS) >= 0) {
            direccion = DireccionTendencia.CRECIENTE;
        } else if (variacion.compareTo(UMBRAL_PUNTOS.negate()) <= 0) {
            direccion = DireccionTendencia.DECRECIENTE;
        } else {
            direccion = DireccionTendencia.ESTABLE;
        }

        boolean sostenida = false;
        if (n >= 3) {
            PuntoHistoricoRecurso trasAnterior = puntos.get(n - 3);
            BigDecimal trasUtil = trasAnterior.utilizacion() != null ? trasAnterior.utilizacion() : BigDecimal.ZERO;
            BigDecimal varPrevia = antUtil.subtract(trasUtil).setScale(2, RoundingMode.HALF_UP);

            if (direccion == DireccionTendencia.CRECIENTE) {
                sostenida = varPrevia.compareTo(UMBRAL_PUNTOS) >= 0;
            } else if (direccion == DireccionTendencia.DECRECIENTE) {
                sostenida = varPrevia.compareTo(UMBRAL_PUNTOS.negate()) <= 0;
            } else {
                sostenida = varPrevia.abs().compareTo(UMBRAL_PUNTOS) < 0 && variacion.abs().compareTo(UMBRAL_PUNTOS) < 0;
            }
        }

        String mensaje = matchMensaje(direccion, variacion, sostenida);

        return new TendenciaRecurso(direccion, variacion, n, sostenida, mensaje);
    }

    private String matchMensaje(DireccionTendencia direccion, BigDecimal variacion, boolean sostenida) {
        return switch (direccion) {
            case CRECIENTE -> "La utilización subió " + formatear(variacion) + " puntos respecto al período anterior"
                    + (sostenida ? " (tendencia creciente sostenida en los últimos 3 períodos)" : "");
            case DECRECIENTE -> "La utilización bajó " + formatear(variacion.abs()) + " puntos respecto al período anterior"
                    + (sostenida ? " (tendencia decreciente sostenida en los últimos 3 períodos)" : "");
            case ESTABLE -> "La utilización se mantiene estable respecto al período anterior (variación de "
                    + formatear(variacion) + " puntos)"
                    + (sostenida ? " (estabilidad sostenida en los últimos 3 períodos)" : "");
            case SIN_DATOS_SUFICIENTES -> "Se necesitan al menos 2 períodos analizados para calcular la tendencia";
        };
    }

    public static String formatear(BigDecimal valor) {
        if (valor == null) {
            return "0";
        }
        BigDecimal redondeado = valor.setScale(2, RoundingMode.HALF_UP);
        BigDecimal simplificado = redondeado.stripTrailingZeros();
        if (simplificado.scale() < 0) {
            simplificado = simplificado.setScale(0, RoundingMode.UNNECESSARY);
        }
        return simplificado.toPlainString();
    }

    private record ClavePeriodo(LocalDate inicio, LocalDate fin, FrecuenciaReporte frecuencia) {}
}
