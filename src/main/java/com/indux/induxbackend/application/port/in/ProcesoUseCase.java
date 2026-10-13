package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Proceso;

import java.util.List;

public interface ProcesoUseCase {

    Proceso crear(CrearProcesoComando comando);

    Proceso obtener(Long id);

    List<Proceso> listarPorProducto(Long productoId);

    Proceso actualizar(Long id, ActualizarProcesoComando comando);

    void eliminar(Long id);

    record CrearProcesoComando(Long productoId, Long liderId, String codigo,
                               String nombre, int secuencia, String unidadProcesada) {
    }

    record ActualizarProcesoComando(Long liderId, String codigo, String nombre,
                                    int secuencia, String unidadProcesada) {
    }
}