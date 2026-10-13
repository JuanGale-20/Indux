package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ProcesoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ProcesoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcesoPersistenceAdapter implements ProcesoRepositoryPort {

    private final ProcesoJpaRepository repository;

    @Override
    public Proceso guardar(Proceso proceso) {
        return ProcesoMapper.toDomain(repository.save(ProcesoMapper.toEntity(proceso)));
    }

    @Override
    public Optional<Proceso> buscarPorId(Long id) {
        return repository.findById(id).map(ProcesoMapper::toDomain);
    }

    @Override
    public List<Proceso> listarPorProductoOrdenadoPorSecuencia(Long productoId) {
        return repository.findByProductoIdOrderBySecuenciaAsc(productoId)
                .stream().map(ProcesoMapper::toDomain).toList();
    }

    @Override
    public boolean existePorProductoYCodigo(Long productoId, String codigo) {
        return repository.existsByProductoIdAndCodigo(productoId, codigo);
    }

    @Override
    public boolean existePorProductoYSecuencia(Long productoId, int secuencia) {
        return repository.existsByProductoIdAndSecuencia(productoId, secuencia);
    }

    @Override
    public void eliminar(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException("No se puede eliminar el proceso porque tiene recursos asociados");
        }
    }
}