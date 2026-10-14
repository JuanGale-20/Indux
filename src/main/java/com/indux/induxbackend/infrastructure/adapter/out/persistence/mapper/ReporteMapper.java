package com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper;

import com.indux.induxbackend.domain.model.Reporte;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ReporteJpaEntity;

public final class ReporteMapper {

    private ReporteMapper() {
    }

    public static Reporte toDomain(ReporteJpaEntity e) {
        return new Reporte(e.getId(), e.getProductoId(), e.getCreadoPor(), e.getPlantaArea(),
                e.getFrecuencia(), e.getPeriodoInicio(), e.getPeriodoFin(),
                e.getDemanda(), e.getProduccionReal(), e.getEstado());
    }

    public static ReporteJpaEntity toEntity(Reporte d) {
        ReporteJpaEntity e = new ReporteJpaEntity();
        e.setId(d.id());
        e.setProductoId(d.productoId());
        e.setCreadoPor(d.creadoPor());
        e.setPlantaArea(d.plantaArea());
        e.setFrecuencia(d.frecuencia());
        e.setPeriodoInicio(d.periodoInicio());
        e.setPeriodoFin(d.periodoFin());
        e.setDemanda(d.demanda());
        e.setProduccionReal(d.produccionReal());
        e.setEstado(d.estado());
        return e;
    }
}