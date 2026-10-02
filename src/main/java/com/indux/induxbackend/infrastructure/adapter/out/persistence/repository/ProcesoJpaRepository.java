package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProcesoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcesoJpaRepository extends JpaRepository<ProcesoJpaEntity, Long> {

    List<ProcesoJpaEntity> findByProductoIdOrderBySecuenciaAsc(Long productoId);

    boolean existsByProductoIdAndCodigo(Long productoId, String codigo);

    boolean existsByProductoIdAndSecuencia(Long productoId, int secuencia);
}
