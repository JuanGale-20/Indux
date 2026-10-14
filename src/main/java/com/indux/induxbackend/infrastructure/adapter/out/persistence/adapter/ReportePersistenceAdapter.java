package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ReporteRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.model.Reporte;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ReporteMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ReporteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportePersistenceAdapter implements ReporteRepositoryPort {

    private final ReporteJpaRepository repository;

    @Override
    public Reporte guardar(Reporte reporte) {
        return ReporteMapper.toDomain(repository.save(ReporteMapper.toEntity(reporte)));
    }

    @Override
    public Optional<Reporte> buscarPorId(Long id) {
        return repository.findById(id).map(ReporteMapper::toDomain);
    }

    @Override
    public List<Reporte> listarPorProducto(Long productoId) {
        return repository.findByProductoIdOrderByPeriodoInicioDesc(productoId)
                .stream().map(ReporteMapper::toDomain).toList();
    }

    @Override
    public List<Reporte> listarPorEmpresa(Long empresaId) {
        return repository.findByEmpresaId(empresaId).stream().map(ReporteMapper::toDomain).toList();
    }

    @Override
    public void eliminar(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException("No se puede eliminar el reporte porque tiene datos asociados");
        }
    }
}