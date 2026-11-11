package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.NivelRecomendacion;
import com.indux.induxbackend.domain.model.Recomendacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GeneradorRecomendaciones {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public List<Recomendacion> generar(List<AnalisisRecursoReporte> analisis) {
        if (analisis == null || analisis.isEmpty()) {
            return List.of();
        }

        List<Recomendacion> recomendaciones = new ArrayList<>();
        for (AnalisisRecursoReporte r : analisis) {
            recomendaciones.add(generarUna(r));
        }
        return recomendaciones;
    }

    private Recomendacion generarUna(AnalisisRecursoReporte r) {
        boolean compartido = esCompartido(r);

        NivelRecomendacion nivel;
        String resumen;
        List<String> acciones = new ArrayList<>();

        switch (r.clasificacion()) {
            case SOBRECARGA -> {
                boolean esPrincipal = r.prioridad() != null && r.prioridad() == 1;
                nivel = esPrincipal ? NivelRecomendacion.CRITICA : NivelRecomendacion.ALTA;
                resumen = "Sobrecarga: faltan " + formatear(r.deficitHoras()) + " h (impacto "
                        + formatear(r.impacto()) + " %)";

                if (esPrincipal) {
                    acciones.add("Atención prioritaria: es la restricción principal del sistema y debe atenderse en primer lugar");
                }

                if (r.horasDisponibles().signum() > 0) {
                    BigDecimal capAdicional = r.deficitHoras().multiply(CIEN)
                            .divide(r.horasDisponibles(), 2, RoundingMode.HALF_UP);
                    acciones.add("Cubrir el déficit de " + formatear(r.deficitHoras())
                            + " h (se requiere un " + formatear(capAdicional) + " % de capacidad adicional)");
                } else {
                    acciones.add("Cubrir el déficit de " + formatear(r.deficitHoras())
                            + " h (el recurso no tiene horas disponibles)");
                }

                acciones.add("Evaluar ampliar turnos, horas de operación o sumar un recurso equivalente");
                acciones.add("Reducir el tiempo de procesamiento mediante balanceo de carga, reducción de tiempos de preparación o eliminación de esperas");
                acciones.add("Programar mantenimiento preventivo para evitar paradas no planificadas");

                if (compartido) {
                    acciones.add("Revisar el reparto de carga y la secuenciación entre los productos que comparten este recurso");
                }
            }
            case CAPACIDAD_AJUSTADA -> {
                nivel = NivelRecomendacion.MEDIA;
                BigDecimal porcentajeDisp = r.horasDisponibles().signum() > 0
                        ? r.holgura().multiply(CIEN).divide(r.horasDisponibles(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                resumen = "Capacidad muy ajustada: holgura de " + formatear(r.holgura())
                        + " h (" + formatear(porcentajeDisp) + " % disponible)";

                acciones.add("Monitorear la holgura disponible de " + formatear(r.holgura())
                        + " h (representa el " + formatear(porcentajeDisp) + " % de la capacidad programada)");
                acciones.add("Monitorear la utilización en cada período para anticipar posibles sobrecargas");
                acciones.add("Definir un plan de contingencia con horas extra o turno adicional si la demanda se incrementa");
                acciones.add("Programar mantenimiento preventivo para asegurar la continuidad operativa");

                if (compartido) {
                    acciones.add("Coordinar la planeación de la producción con los otros productos que comparten el recurso");
                }
            }
            case UTILIZACION_ADECUADA -> {
                nivel = NivelRecomendacion.INFORMATIVA;
                resumen = "Utilización adecuada (" + formatear(r.utilizacion()) + " %)";
                acciones.add("Mantener el ritmo de operación actual");
                acciones.add("Hacer seguimiento periódico a la tendencia de utilización");
            }
            case BAJA_UTILIZACION -> {
                nivel = NivelRecomendacion.BAJA;
                BigDecimal ociosa = r.holgura().max(BigDecimal.ZERO);
                BigDecimal porcentajeOcioso = r.horasDisponibles().signum() > 0
                        ? ociosa.multiply(CIEN).divide(r.horasDisponibles(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                resumen = "Baja utilización: " + formatear(ociosa) + " h sin usar ("
                        + formatear(porcentajeOcioso) + " % disponible)";

                acciones.add("Capacidad no utilizada de " + formatear(ociosa)
                        + " h (representa el " + formatear(porcentajeOcioso) + " % de lo disponible)");
                acciones.add("Evaluar redistribuir carga o aprovechar la capacidad ociosa con otros productos o nuevos lotes");
                acciones.add("Revisar si la cantidad de recursos o turnos asignados supera las necesidades actuales");
            }
            default -> {
                nivel = NivelRecomendacion.INFORMATIVA;
                resumen = "Sin recomendaciones específicas";
            }
        }

        return new Recomendacion(r.recursoId(), r.codigo(), r.nombre(), r.prioridad(), nivel, resumen, acciones);
    }

    private static boolean esCompartido(AnalisisRecursoReporte r) {
        return r.diagnostico() != null && r.diagnostico().toLowerCase().contains("compartido");
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
