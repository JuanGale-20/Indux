package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProcesoJpaEntity;

public final class ProcesoMapper {

    private ProcesoMapper() {
    }

    public static Proceso toDomain(ProcesoJpaEntity e) {
        return new Proceso(e.getId(), e.getProductoId(), e.getLiderId(), e.getCodigo(),
                e.getNombre(), e.getSecuencia(), e.getUnidadProcesada());
    }

    public static ProcesoJpaEntity toEntity(Proceso d) {
        ProcesoJpaEntity e = new ProcesoJpaEntity();
        e.setId(d.id());
        e.setProductoId(d.productoId());
        e.setLiderId(d.liderId());
        e.setCodigo(d.codigo());
        e.setNombre(d.nombre());
        e.setSecuencia(d.secuencia());
        e.setUnidadProcesada(d.unidadProcesada());
        return e;
    }
}
