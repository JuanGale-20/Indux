package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ProductoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductoPersistenceAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository repository;

    @Override
    public Producto guardar(Producto producto) {
        return ProductoMapper.toDomain(repository.save(ProductoMapper.toEntity(producto)));
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return repository.findById(id).map(ProductoMapper::toDomain);
    }

    @Override
    public List<Producto> listarPorEmpresa(Long empresaId) {
        return repository.findByEmpresaId(empresaId).stream().map(ProductoMapper::toDomain).toList();
    }

    @Override
    public boolean existePorEmpresaYCodigo(Long empresaId, String codigo) {
        return repository.existsByEmpresaIdAndCodigo(empresaId, codigo);
    }
}
