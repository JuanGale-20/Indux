package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.EstadoRecurso;
import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.domain.model.Recurso;

import java.math.BigDecimal;
import java.util.List;

public interface RecursoUseCase {

    Recurso crear(CrearRecursoComando comando);

    Recurso obtener(Long id);

    List<Recurso> listarPorEmpresa(Long empresaId);

    List<Recurso> listarPorProceso(Long procesoId);

    Recurso actualizar(Long id, ActualizarRecursoComando comando);

    void eliminar(Long id);

    ProcesoRecurso vincular(Long recursoId, VincularProcesoComando comando);

    void desvincular(Long recursoId, Long procesoId);

    List<ProcesoRecurso> listarVinculos(Long recursoId);

    record CrearRecursoComando(Long empresaId, String codigo, String nombre, String tipo,
                               int cantidadDisponible, EstadoRecurso estado) {
    }

    /** Si el estado viene nulo se conserva el actual. */
    record ActualizarRecursoComando(String codigo, String nombre, String tipo,
                                    int cantidadDisponible, EstadoRecurso estado) {
    }

    record VincularProcesoComando(Long procesoId, BigDecimal tiempoReferencia) {
    }
}