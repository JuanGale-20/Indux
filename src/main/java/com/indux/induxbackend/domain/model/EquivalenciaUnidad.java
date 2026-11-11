package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.math.BigDecimal;

public record EquivalenciaUnidad(Long id, Long empresaId, Long productoId,
                                 String unidadOrigen, String unidadDestino, BigDecimal factor) {

    public EquivalenciaUnidad {
        unidadOrigen = Reglas.textoObligatorio(unidadOrigen, "La unidad de origen");
        unidadDestino = Reglas.textoObligatorio(unidadDestino, "La unidad de destino");
        if (unidadOrigen.equalsIgnoreCase(unidadDestino)) {
            throw new DominioException("La unidad de origen y destino no pueden ser iguales");
        }
        if (factor == null || factor.signum() <= 0) {
            throw new DominioException("El factor de conversión debe ser mayor que 0");
        }
    }
}
