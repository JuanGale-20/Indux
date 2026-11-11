package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.UnidadRepositoryPort;
import com.indux.induxbackend.domain.model.EquivalenciaUnidad;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.UnidadMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.EquivalenciaUnidadJpaRepository;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.UnidadMedidaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UnidadPersistenceAdapter implements UnidadRepositoryPort {

    private final EquivalenciaUnidadJpaRepository equivalencias;
    private final UnidadMedidaJpaRepository unidades;

    @Override
    @Transactional(readOnly = true)
    public List<EquivalenciaUnidad> listarEquivalencias(Long empresaId, Long productoId) {
        return equivalencias.findByEmpresaYProducto(empresaId, productoId).stream()
                .map(UnidadMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> listarCodigosUnidad() {
        return unidades.findCodigos();
    }
}
