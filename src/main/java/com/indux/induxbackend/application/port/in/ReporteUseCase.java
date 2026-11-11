package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.Reporte;
import com.indux.induxbackend.domain.model.ReporteRecurso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ReporteUseCase {

    DetalleReporte crear(CrearReporteComando comando);

    DetalleReporte obtener(Long id);

    List<Reporte> listarPorProducto(Long productoId);

    List<Reporte> listarPorEmpresa(Long empresaId);

    DetalleReporte actualizar(Long id, ActualizarReporteComando comando);

    void eliminar(Long id);

    Set<Long> recursosDelLider(Long reporteId, Long liderId);

    record DetalleReporte(Reporte reporte, List<ReporteRecurso> lineas) {
    }

    record LineaComando(Long recursoId, BigDecimal tiempoProcesamiento, String unidadReferencia,
                        BigDecimal tiempoCalendario, BigDecimal tiempoNoDisponible,
                        int numeroTurnos, BigDecimal horasPorTurno, int diasOperacion) {
    }

    record CrearReporteComando(Long productoId, Long creadoPor, String plantaArea,
                               FrecuenciaReporte frecuencia, LocalDate periodoInicio,
                               LocalDate periodoFin, BigDecimal demanda, BigDecimal produccionReal,
                               List<LineaComando> lineas) {
    }

    /** lineas == null conserva las existentes; una lista (incluso vacía) las reemplaza. */
    record ActualizarReporteComando(String plantaArea, FrecuenciaReporte frecuencia,
                                    LocalDate periodoInicio, LocalDate periodoFin,
                                    BigDecimal demanda, BigDecimal produccionReal,
                                    List<LineaComando> lineas) {
    }
}
