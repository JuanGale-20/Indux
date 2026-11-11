package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.CausaDeficitRecurso;
import com.indux.induxbackend.domain.model.DetalleFactorCausa;
import com.indux.induxbackend.domain.model.FactorCausa;
import com.indux.induxbackend.domain.model.ReporteRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DetectorCausas {

    /**
     * Umbral inicial sobre el tiempo de referencia (10 %).
     * Valor inicial a confirmar con las ingenieras industriales.
     */
    public static final BigDecimal UMBRAL_TIEMPO = new BigDecimal("1.10");

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public CausaDeficitRecurso detectar(
            AnalisisRecursoReporte r,
            ReporteRecurso linea,
            BigDecimal tiempoReferencia,
            BigDecimal aporteSinMerma) {

        if (r == null || r.deficitHoras() == null || r.deficitHoras().signum() <= 0) {
            return null;
        }

        BigDecimal deficit = r.deficitHoras();
        List<DetalleFactorCausa> factores = new ArrayList<>();

        // 1. RECURSO_COMPARTIDO = max(0, totales - aporteHoras)
        BigDecimal totales = r.horasRequeridas() != null ? r.horasRequeridas() : BigDecimal.ZERO;
        BigDecimal aportePropio = r.aporteHoras() != null ? r.aporteHoras() : BigDecimal.ZERO;
        BigDecimal compartidoHoras = totales.subtract(aportePropio).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
        if (compartidoHoras.signum() > 0) {
            BigDecimal pct = calcularPorcentaje(compartidoHoras, deficit);
            String desc = "Otros productos le exigen " + formatear(compartidoHoras) + " h al mismo recurso";
            factores.add(new DetalleFactorCausa(FactorCausa.RECURSO_COMPARTIDO, compartidoHoras, pct, desc));
        }

        // 2. TIEMPO_PROCESAMIENTO: solo si tiempoProcesamiento > tiempoReferencia * 1.10
        BigDecimal t = linea != null ? linea.tiempoProcesamiento() : null;
        if (tiempoReferencia != null && tiempoReferencia.signum() > 0 && t != null) {
            BigDecimal limite = tiempoReferencia.multiply(UMBRAL_TIEMPO);
            if (t.compareTo(limite) > 0) {
                BigDecimal excesoHoras = aportePropio.multiply(t.subtract(tiempoReferencia))
                        .divide(t, 4, RoundingMode.HALF_UP)
                        .max(BigDecimal.ZERO);
                if (excesoHoras.signum() > 0) {
                    BigDecimal pctSupera = t.subtract(tiempoReferencia).multiply(CIEN)
                            .divide(tiempoReferencia, 2, RoundingMode.HALF_UP);
                    BigDecimal pctDeficit = calcularPorcentaje(excesoHoras, deficit);
                    String desc = "El tiempo de procesamiento supera la referencia en " + formatear(pctSupera) + " %";
                    factores.add(new DetalleFactorCausa(FactorCausa.TIEMPO_PROCESAMIENTO, excesoHoras, pctDeficit, desc));
                }
            }
        }

        // 3. MERMA = aporteHoras - aporteHorasConMermaCero
        if (aporteSinMerma != null) {
            BigDecimal mermaHoras = aportePropio.subtract(aporteSinMerma).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
            if (mermaHoras.signum() > 0) {
                BigDecimal pct = calcularPorcentaje(mermaHoras, deficit);
                String desc = "La merma de los procesos obliga a procesar " + formatear(mermaHoras) + " h adicionales";
                factores.add(new DetalleFactorCausa(FactorCausa.MERMA, mermaHoras, pct, desc));
            }
        }

        // 4. TIEMPO_NO_DISPONIBLE = tiempoNoDisponible de la línea (0 si es nulo)
        BigDecimal noDisponible = (linea != null && linea.tiempoNoDisponible() != null)
                ? linea.tiempoNoDisponible().max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        if (noDisponible.signum() > 0) {
            BigDecimal pct = calcularPorcentaje(noDisponible, deficit);
            String desc = "El tiempo no disponible resta " + formatear(noDisponible) + " h a la capacidad";
            factores.add(new DetalleFactorCausa(FactorCausa.TIEMPO_NO_DISPONIBLE, noDisponible, pct, desc));
        }

        // 5. DEMANDA (residual) = max(0, deficit - suma de los anteriores)
        BigDecimal sumaAnteriores = factores.stream()
                .map(DetalleFactorCausa::horas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal demandaHoras = deficit.subtract(sumaAnteriores).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
        if (demandaHoras.signum() > 0) {
            BigDecimal pct = calcularPorcentaje(demandaHoras, deficit);
            String desc = "La demanda del período supera la capacidad programada en " + formatear(demandaHoras) + " h";
            factores.add(new DetalleFactorCausa(FactorCausa.DEMANDA, demandaHoras, pct, desc));
        }

        // Ordenar de mayor a menor por horas
        factores.sort(Comparator.comparing(DetalleFactorCausa::horas).reversed());

        FactorCausa causaPrincipal = factores.isEmpty() ? null : factores.getFirst().factor();

        return new CausaDeficitRecurso(
                r.recursoId(),
                r.codigo(),
                r.nombre(),
                r.prioridad(),
                deficit,
                causaPrincipal,
                factores
        );
    }

    private static BigDecimal calcularPorcentaje(BigDecimal horas, BigDecimal deficit) {
        if (deficit == null || deficit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal pct = horas.multiply(CIEN).divide(deficit, 2, RoundingMode.HALF_UP);
        return pct.min(CIEN);
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
}
