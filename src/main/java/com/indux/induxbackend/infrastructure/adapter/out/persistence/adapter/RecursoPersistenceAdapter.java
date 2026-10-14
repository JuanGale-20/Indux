package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.model.Recurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.RecursoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.RecursoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecursoPersistenceAdapter implements RecursoRepositoryPort {

    private final RecursoJpaRepository repository;

    @Override
    public Recurso guardar(Recurso recurso) {
        return RecursoMapper.toDomain(repository.save(RecursoMapper.toEntity(recurso)));
    }

    @Override
    public Optional<Recurso> buscarPorId(Long id) {
        return repository.findById(id).map(RecursoMapper::toDomain);
    }

    @Override
    public List<Recurso> listarPorEmpresa(Long empresaId) {
        return repository.findByEmpresaId(empresaId).stream().map(RecursoMapper::toDomain).toList();
    }

    @Override
    public List<Recurso> listarPorProceso(Long procesoId) {
        return repository.findByProcesoId(procesoId).stream().map(RecursoMapper::toDomain).toList();
    }

    @Override
    public boolean existePorEmpresaYCodigo(Long empresaId, String codigo) {
        return repository.existsByEmpresaIdAndCodigo(empresaId, codigo);
    }

    @Override
    public void eliminar(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException(
                    "No se puede eliminar el recurso porque tiene reportes asociados");
        }
    }
}