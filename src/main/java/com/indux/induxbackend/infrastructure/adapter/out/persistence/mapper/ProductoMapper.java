package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto toDomain(ProductoJpaEntity e) {
        return new Producto(e.getId(), e.getEmpresaId(), e.getCodigo(), e.getNombre(), e.getUnidadMedida());
    }

    public static ProductoJpaEntity toEntity(Producto d) {
        ProductoJpaEntity e = new ProductoJpaEntity();
        e.setId(d.id());
        e.setEmpresaId(d.empresaId());
        e.setCodigo(d.codigo());
        e.setNombre(d.nombre());
        e.setUnidadMedida(d.unidadMedida());
        return e;
    }
}
