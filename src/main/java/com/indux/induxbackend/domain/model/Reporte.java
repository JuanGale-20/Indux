package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Reporte(Long id, Long productoId, Long creadoPor, String plantaArea,
                      FrecuenciaReporte frecuencia, LocalDate periodoInicio, LocalDate periodoFin,
                      BigDecimal demanda, BigDecimal produccionReal, EstadoReporte estado) {
    public Reporte {
        Reglas.idObligatorio(productoId, "El producto del reporte");
        Reglas.idObligatorio(creadoPor, "El autor del reporte");
        plantaArea = Reglas.textoObligatorio(plantaArea, "La planta o área");
        if (frecuencia == null) {
            throw new DominioException("La frecuencia del reporte es obligatoria");
        }
        if (periodoInicio == null || periodoFin == null) {
            throw new DominioException("El período del reporte es obligatorio");
        }
        if (periodoFin.isBefore(periodoInicio)) {
            throw new DominioException("El fin del período no puede ser anterior al inicio");
        }
        if (demanda == null || demanda.signum() < 0) {
            throw new DominioException("La demanda no puede ser negativa");
        }
        if (produccionReal == null || produccionReal.signum() < 0) {
            throw new DominioException("La producción real no puede ser negativa");
        }
        estado = (estado == null) ? EstadoReporte.BORRADOR : estado;
    }

    public Reporte conEstado(EstadoReporte nuevoEstado) {
        return new Reporte(id, productoId, creadoPor, plantaArea, frecuencia, periodoInicio, periodoFin,
                demanda, produccionReal, nuevoEstado);
    }
}