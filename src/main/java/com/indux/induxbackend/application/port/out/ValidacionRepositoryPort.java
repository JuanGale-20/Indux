package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.ValidacionResultado;

import java.util.List;

public interface ValidacionRepositoryPort {

    void reemplazarPorReporte(Long reporteId, List<ValidacionResultado> resultados);

    List<ValidacionResultado> listarPorReporte(Long reporteId);
}
