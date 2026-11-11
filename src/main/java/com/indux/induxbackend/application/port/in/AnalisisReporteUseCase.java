package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.CausaDeficitRecurso;
import com.indux.induxbackend.domain.model.DetalleAnalisisReporte;
import com.indux.induxbackend.domain.model.Recomendacion;
import com.indux.induxbackend.domain.model.ResultadoValidacionReporte;
import com.indux.induxbackend.domain.model.ValidacionResultado;

import java.util.List;

public interface AnalisisReporteUseCase {

    ResultadoValidacionReporte validar(Long reporteId);

    List<ValidacionResultado> obtenerValidaciones(Long reporteId);

    DetalleAnalisisReporte analizar(Long reporteId);

    DetalleAnalisisReporte obtenerAnalisis(Long reporteId);

    List<Recomendacion> obtenerRecomendaciones(Long reporteId);

    List<CausaDeficitRecurso> detectarCausas(Long reporteId);
}
