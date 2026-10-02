package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {

    List<ProductoJpaEntity> findByEmpresaId(Long empresaId);

    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
}
