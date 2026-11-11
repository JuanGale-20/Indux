package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.EquivalenciaUnidad;

import java.util.List;
import java.util.Set;

public interface UnidadRepositoryPort {

    List<EquivalenciaUnidad> listarEquivalencias(Long empresaId, Long productoId);

    Set<String> listarCodigosUnidad();
}
