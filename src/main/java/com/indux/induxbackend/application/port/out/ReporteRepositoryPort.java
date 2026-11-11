package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.Reporte;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReporteRepositoryPort {

    Reporte guardar(Reporte reporte);

    Optional<Reporte> buscarPorId(Long id);

    List<Reporte> listarPorProducto(Long productoId);

    List<Reporte> listarPorEmpresa(Long empresaId);

    List<Reporte> buscarCandidatosRecursoCompartido(Long empresaId, Long productoIdExcluir,
                                                   FrecuenciaReporte frecuencia,
                                                   LocalDate inicio, LocalDate fin);

    void eliminar(Long id);
}
