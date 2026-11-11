package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ReporteUseCase.CrearReporteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase.LineaComando;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ReporteRecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ReporteRepositoryPort;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.EstadoRecurso;
import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Recurso;
import com.indux.induxbackend.domain.model.Reporte;
import com.indux.induxbackend.domain.model.ReporteRecurso;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteServiceTest {

    private final ReporteRepositoryPort reportes = mock(ReporteRepositoryPort.class);
    private final ReporteRecursoRepositoryPort lineas = mock(ReporteRecursoRepositoryPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final RecursoRepositoryPort recursos = mock(RecursoRepositoryPort.class);
    private final ReporteService servicio = new ReporteService(reportes, lineas, productos, recursos);

    private Reporte guardado() {
        return new Reporte(5L, 1L, 9L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("700"), EstadoReporte.BORRADOR);
    }

    private LineaComando linea(Long recursoId) {
        return new LineaComando(recursoId, new BigDecimal("0.5"), "unidad", new BigDecimal("720"),
                null, 2, new BigDecimal("8"), 22);
    }

    private CrearReporteComando comando(List<LineaComando> lineasComando) {
        return new CrearReporteComando(1L, 9L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("700"), lineasComando);
    }

    private void productoDeEmpresa(Long empresaId) {
        Producto producto = mock(Producto.class);
        when(producto.empresaId()).thenReturn(empresaId);
        when(productos.buscarPorId(1L)).thenReturn(Optional.of(producto));
    }

    private Recurso recurso(Long id, Long empresaId) {
        return new Recurso(id, empresaId, "R" + id, "Recurso " + id, "MAQUINA", 1, EstadoRecurso.DISPONIBLE);
    }

    @Test
    void creaElReporteYAsignaSuIdALasLineas() {
        productoDeEmpresa(7L);
        when(recursos.buscarPorId(3L)).thenReturn(Optional.of(recurso(3L, 7L)));
        when(reportes.guardar(any())).thenReturn(guardado());

        servicio.crear(comando(List.of(linea(3L))));

        ArgumentCaptor<ReporteRecurso> captor = ArgumentCaptor.forClass(ReporteRecurso.class);
        verify(lineas).guardar(captor.capture());
        assertThat(captor.getValue().reporteId()).isEqualTo(5L);
        assertThat(captor.getValue().recursoId()).isEqualTo(3L);
    }

    @Test
    void rechazaRecursoDeOtraEmpresa() {
        productoDeEmpresa(7L);
        when(recursos.buscarPorId(3L)).thenReturn(Optional.of(recurso(3L, 99L)));

        assertThatThrownBy(() -> servicio.crear(comando(List.of(linea(3L)))))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("no pertenece a la empresa");
        verify(reportes, never()).guardar(any());
    }

    @Test
    void rechazaRecursoRepetidoEnElMismoReporte() {
        productoDeEmpresa(7L);
        when(recursos.buscarPorId(3L)).thenReturn(Optional.of(recurso(3L, 7L)));

        assertThatThrownBy(() -> servicio.crear(comando(List.of(linea(3L), linea(3L)))))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("repetido");
    }
}
