package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase.CrearReporteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase.DetalleReporte;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportacionServiceTest {

    @Mock
    private ProductoRepositoryPort productosRepo;

    @Mock
    private RecursoRepositoryPort recursosRepo;

    @Mock
    private ReporteUseCase reporteUseCase;

    @InjectMocks
    private ImportacionService importacionService;

    private final Long empresaId = 1L;
    private final Long usuarioId = 10L;

    @BeforeEach
    void setUp() {
        Producto p1 = new Producto(100L, empresaId, "PROD-01", "Producto 1", "kg");
        Recurso r1 = new Recurso(201L, empresaId, "REC-01", "Recurso 1", "Maquina", 1, EstadoRecurso.DISPONIBLE);
        Recurso r2 = new Recurso(202L, empresaId, "REC-02", "Recurso 2", "Maquina", 1, EstadoRecurso.DISPONIBLE);

        lenient().when(productosRepo.listarPorEmpresa(empresaId)).thenReturn(List.of(p1));
        lenient().when(recursosRepo.listarPorEmpresa(empresaId)).thenReturn(List.of(r1, r2));
    }

    private ByteArrayInputStream crearCsv(String cuerpo) {
        String cabecera = "producto_codigo,planta_area,frecuencia,periodo_inicio,periodo_fin,demanda,produccion_real,"
                + "recurso_codigo,tiempo_procesamiento,unidad_referencia,tiempo_calendario,tiempo_no_disponible,numero_turnos,horas_por_turno,dias_operacion\n";
        return new ByteArrayInputStream((cabecera + cuerpo).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Agrupa filas en un mismo reporte y la vista previa no guarda nada")
    void vistaPreviaAgrupaYNoGuarda() {
        String cuerpo = "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.2,kg,720,0,2,8,20\n"
                + "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-02,0.3,kg,720,0,2,8,20";

        ResultadoImportacion res = importacionService.importar(crearCsv(cuerpo), "datos.csv", empresaId, usuarioId, false);

        assertThat(res.valido()).isTrue();
        assertThat(res.guardado()).isFalse();
        assertThat(res.filasLeidas()).isEqualTo(2);
        assertThat(res.reportes()).hasSize(1);
        assertThat(res.reportes().get(0).recursos()).isEqualTo(2);
        assertThat(res.reportes().get(0).reporteId()).isNull();

        verify(reporteUseCase, never()).crear(any());
    }

    @Test
    @DisplayName("Confirmar guarda los reportes agrupados y devuelve sus IDs")
    void confirmarGuardaYDevuelveIds() {
        String cuerpo = "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.2,kg,720,0,2,8,20\n"
                + "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-02,0.3,kg,720,0,2,8,20";

        Reporte repCreado = new Reporte(555L, 100L, usuarioId, "Planta Norte", FrecuenciaReporte.MENSUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("1000"), new BigDecimal("950"), EstadoReporte.BORRADOR);

        when(reporteUseCase.crear(any(CrearReporteComando.class)))
                .thenReturn(new DetalleReporte(repCreado, List.of()));

        ResultadoImportacion res = importacionService.importar(crearCsv(cuerpo), "datos.csv", empresaId, usuarioId, true);

        assertThat(res.valido()).isTrue();
        assertThat(res.guardado()).isTrue();
        assertThat(res.reportes()).hasSize(1);
        assertThat(res.reportes().get(0).reporteId()).isEqualTo(555L);

        verify(reporteUseCase, times(1)).crear(any(CrearReporteComando.class));
    }

    @Test
    @DisplayName("Producto inexistente reporta error con número de fila y no guarda")
    void productoInexistenteError() {
        String cuerpo = "PROD-INVENTADO,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.2,kg,720,0,2,8,20";

        ResultadoImportacion res = importacionService.importar(crearCsv(cuerpo), "datos.csv", empresaId, usuarioId, true);

        assertThat(res.valido()).isFalse();
        assertThat(res.guardado()).isFalse();
        assertThat(res.errores()).anyMatch(e -> e.fila() == 2 && e.columna().equals("producto_codigo")
                && e.mensaje().contains("no existe en la empresa"));

        verify(reporteUseCase, never()).crear(any());
    }

    @Test
    @DisplayName("Demanda distinta dentro del mismo reporte reporta error con fila inicial")
    void demandaDistintaMismoReporte() {
        String cuerpo = "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.2,kg,720,0,2,8,20\n"
                + "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1200,950,REC-02,0.3,kg,720,0,2,8,20";

        ResultadoImportacion res = importacionService.importar(crearCsv(cuerpo), "datos.csv", empresaId, usuarioId, false);

        assertThat(res.valido()).isFalse();
        assertThat(res.errores()).anyMatch(e -> e.fila() == 3 && e.columna().equals("demanda")
                && e.mensaje().contains("distinta a la informada en la fila 2"));
    }

    @Test
    @DisplayName("Recurso repetido dentro del mismo reporte reporta error")
    void recursoRepetidoMismoReporte() {
        String cuerpo = "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.2,kg,720,0,2,8,20\n"
                + "PROD-01,Planta Norte,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.3,kg,720,0,2,8,20";

        ResultadoImportacion res = importacionService.importar(crearCsv(cuerpo), "datos.csv", empresaId, usuarioId, false);

        assertThat(res.valido()).isFalse();
        assertThat(res.errores()).anyMatch(e -> e.fila() == 3 && e.columna().equals("recurso_codigo")
                && e.mensaje().contains("repetido en el reporte"));
    }
}
