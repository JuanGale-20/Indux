package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.UnidadMedidaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface UnidadMedidaJpaRepository extends JpaRepository<UnidadMedidaJpaEntity, String> {

    @Query("SELECT u.codigo FROM UnidadMedidaJpaEntity u")
    Set<String> findCodigos();
}
