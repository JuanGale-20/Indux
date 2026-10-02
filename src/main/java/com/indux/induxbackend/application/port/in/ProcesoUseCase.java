package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Proceso;

import java.util.List;

public interface ProcesoUseCase {

    Proceso crear(CrearProcesoComando comando);

    List<Proceso> listarPorProducto(Long productoId);

    record CrearProcesoComando(Long productoId, Long liderId, String codigo,
                               String nombre, int secuencia, String unidadProcesada) {
    }
}
