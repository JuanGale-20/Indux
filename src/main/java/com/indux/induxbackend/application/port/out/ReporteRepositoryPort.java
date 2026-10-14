package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Reporte;

import java.util.List;
import java.util.Optional;

public interface ReporteRepositoryPort {

    Reporte guardar(Reporte reporte);

    Optional<Reporte> buscarPorId(Long id);

    List<Reporte> listarPorProducto(Long productoId);

    List<Reporte> listarPorEmpresa(Long empresaId);

    void eliminar(Long id);
}