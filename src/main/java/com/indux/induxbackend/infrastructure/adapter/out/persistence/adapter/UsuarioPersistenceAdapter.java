package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.model.Usuario;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.UsuarioMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;

    @Override
    public Usuario guardar(Usuario usuario) {
        return UsuarioMapper.toDomain(repository.save(UsuarioMapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id).map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(UsuarioMapper::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(email);
    }
}
