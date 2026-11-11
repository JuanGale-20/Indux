package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.EquivalenciaUnidad;
import com.indux.induxbackend.domain.model.UnidadMedida;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.EquivalenciaUnidadJpaEntity;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.UnidadMedidaJpaEntity;

public final class UnidadMapper {

    private UnidadMapper() {}

    public static EquivalenciaUnidad toDomain(EquivalenciaUnidadJpaEntity e) {
        if (e == null) {
            return null;
        }
        return new EquivalenciaUnidad(
                e.getId(),
                e.getEmpresaId(),
                e.getProductoId(),
                e.getUnidadOrigen(),
                e.getUnidadDestino(),
                e.getFactor());
    }

    public static EquivalenciaUnidadJpaEntity toEntity(EquivalenciaUnidad d) {
        if (d == null) {
            return null;
        }
        return new EquivalenciaUnidadJpaEntity(
                d.id(),
                d.empresaId(),
                d.productoId(),
                d.unidadOrigen(),
                d.unidadDestino(),
                d.factor());
    }

    public static UnidadMedida toDomain(UnidadMedidaJpaEntity e) {
        if (e == null) {
            return null;
        }
        return new UnidadMedida(e.getCodigo(), e.getNombre(), e.getMagnitud());
    }
}
