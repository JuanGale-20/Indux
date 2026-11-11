package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.out.AnalisisRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ReporteRepositoryPort;
import com.indux.induxbackend.application.port.out.ValidacionRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.ConversorUnidades;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalisisReporteServiceTest {

    @Mock
    private CargadorContextoReporte cargadorContexto;

    @Mock
    private ReporteRepositoryPort reportes;

    @Mock
    private ValidacionRepositoryPort validacionesRepo;

    @Mock
    private AnalisisRepositoryPort analisisRepo;

    @Mock
    private ProcesoRecursoRepositoryPort procesoRecursos;

    @InjectMocks
    private AnalisisReporteService service;

    @Test
    @DisplayName("analizar con errores críticos devuelve CON_ERRORES y no guarda análisis")
    void analizarConErroresCriticos() {
        Long reporteId = 1L;
        Reporte reporte = new Reporte(reporteId, 10L, 100L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.BORRADOR);

        Producto producto = new Producto(10L, 1L, "P1", "Prod 1", "kg");

        // Sin procesos -> genera error crítico
        CargadorContextoReporte.ContextoReporte ctx = new CargadorContextoReporte.ContextoReporte(
                reporte, producto, List.of(), List.of(), Map.of(), Map.of(),
                new ConversorUnidades(List.of()), Set.of("kg"));

        when(cargadorContexto.cargar(reporteId)).thenReturn(ctx);

        DetalleAnalisisReporte resultado = service.analizar(reporteId);

        assertThat(resultado.estado()).isEqualTo(EstadoReporte.CON_ERRORES);
        assertThat(resultado.recursos()).isEmpty();
        assertThat(resultado.validaciones()).anyMatch(ValidacionResultado::esErrorCritico);

        verify(validacionesRepo).reemplazarPorReporte(eq(reporteId), any());
        verify(reportes).guardar(argThat(r -> r.estado() == EstadoReporte.CON_ERRORES));
        verify(analisisRepo, never()).reemplazarPorReporte(any(), any());
    }

    @Test
    @DisplayName("analizar sin errores críticos guarda análisis y deja el reporte en ANALIZADO")
    void analizarSinErroresCriticos() {
        Long reporteId = 1L;
        Reporte reporte = new Reporte(reporteId, 10L, 100L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.BORRADOR);

        Producto producto = new Producto(10L, 1L, "P1", "Prod 1", "kg");
        Proceso proceso = new Proceso(100L, 10L, null, "PROC-1", "Proceso 1", 1, "kg", BigDecimal.ZERO, null);
        Recurso recurso = new Recurso(50L, 1L, "REC-1", "Recurso 1", "Maquina", 1, EstadoRecurso.DISPONIBLE);
        ReporteRecurso linea = new ReporteRecurso(500L, reporteId, 50L, new BigDecimal("0.2"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        CargadorContextoReporte.ContextoReporte ctx = new CargadorContextoReporte.ContextoReporte(
                reporte, producto, List.of(proceso), List.of(linea),
                Map.of(50L, recurso), Map.of(50L, List.of(100L)),
                new ConversorUnidades(List.of()), Set.of("kg"));

        when(cargadorContexto.cargar(reporteId)).thenReturn(ctx);
        when(cargadorContexto.cargarOtrosContextos(ctx)).thenReturn(List.of());

        DetalleAnalisisReporte resultado = service.analizar(reporteId);

        assertThat(resultado.estado()).isEqualTo(EstadoReporte.ANALIZADO);
        verify(validacionesRepo).reemplazarPorReporte(eq(reporteId), any());
        verify(analisisRepo).reemplazarPorReporte(eq(reporteId), any());
        verify(reportes).guardar(argThat(r -> r.estado() == EstadoReporte.ANALIZADO));
    }

    @Test
    @DisplayName("detectarCausas falla si el reporte no está ANALIZADO")
    void detectarCausasNoAnalizado() {
        Long reporteId = 2L;
        Reporte reporte = new Reporte(reporteId, 10L, 100L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.BORRADOR);

        when(reportes.buscarPorId(reporteId)).thenReturn(Optional.of(reporte));

        assertThatThrownBy(() -> service.detectarCausas(reporteId))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("no ha sido analizado");
    }

    @Test
    @DisplayName("detectarCausas retorna causas para recursos con déficit")
    void detectarCausasConDeficit() {
        Long reporteId = 3L;
        Reporte reporte = new Reporte(reporteId, 10L, 100L, "Planta 1", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.ANALIZADO);

        AnalisisRecursoReporte r = new AnalisisRecursoReporte(
                1L, 500L, 50L, "REC-1", "Recurso 1",
                new BigDecimal("360"), new BigDecimal("360"), new BigDecimal("320"),
                new BigDecimal("1600"), new BigDecimal("112.50"), new BigDecimal("-40"),
                new BigDecimal("40"), new BigDecimal("11.11"), new BigDecimal("200"),
                ClasificacionRestriccion.SOBRECARGA, 1, "Alerta crítica", Instant.now()
        );

        when(reportes.buscarPorId(reporteId)).thenReturn(Optional.of(reporte));
        when(analisisRepo.listarPorReporte(reporteId)).thenReturn(List.of(r));

        Producto producto = new Producto(10L, 1L, "P1", "Prod 1", "kg");
        Proceso proceso = new Proceso(100L, 10L, null, "PROC-1", "Proceso 1", 1, "kg", BigDecimal.ZERO, null);
        Recurso recurso = new Recurso(50L, 1L, "REC-1", "Recurso 1", "Maquina", 1, EstadoRecurso.DISPONIBLE);
        ReporteRecurso linea = new ReporteRecurso(500L, reporteId, 50L, new BigDecimal("0.36"), "kg",
                new BigDecimal("500"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        CargadorContextoReporte.ContextoReporte ctx = new CargadorContextoReporte.ContextoReporte(
                reporte, producto, List.of(proceso), List.of(linea),
                Map.of(50L, recurso), Map.of(50L, List.of(100L)),
                new ConversorUnidades(List.of()), Set.of("kg"));

        when(cargadorContexto.cargar(reporte)).thenReturn(ctx);
        when(procesoRecursos.listarPorRecurso(50L)).thenReturn(List.of(new ProcesoRecurso(100L, 50L, new BigDecimal("0.36"))));

        List<CausaDeficitRecurso> causas = service.detectarCausas(reporteId);

        assertThat(causas).hasSize(1);
        assertThat(causas.getFirst().causaPrincipal()).isEqualTo(FactorCausa.DEMANDA);
    }
}
