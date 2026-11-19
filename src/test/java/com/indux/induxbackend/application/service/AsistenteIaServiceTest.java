package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisReporteUseCase;
import com.indux.induxbackend.application.port.in.AsistenteIaUseCase.ConsultaAsistenteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.out.*;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.exception.SinPermisoException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.model.asistente.MensajeAsistente;
import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenteIaServiceTest {

    @Mock
    private LlmClientPort llmClient;
    @Mock
    private ReporteUseCase reporteUseCase;
    @Mock
    private ReporteRepositoryPort reporteRepo;
    @Mock
    private ProductoRepositoryPort productoRepo;
    @Mock
    private RecursoRepositoryPort recursoRepo;
    @Mock
    private AnalisisReporteUseCase analisisReporteUseCase;

    private AsistenteIaService service;

    @BeforeEach
    void setUp() {
        service = new AsistenteIaService(
                llmClient, reporteUseCase, reporteRepo, productoRepo, recursoRepo, analisisReporteUseCase
        );
    }

    @Test
    void mensajeVacioLanzaExcepcion() {
        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "   ", null, List.of());
        assertThatThrownBy(() -> service.consultar(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede estar vacío");
    }

    @Test
    void preguntaSupera2500CaracteresLanzaExcepcion() {
        String preguntaLarga = "A".repeat(2501);
        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, preguntaLarga, null, List.of());

        assertThatThrownBy(() -> service.consultar(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede superar los 2500 caracteres");
        verifyNoInteractions(llmClient);
    }

    @Test
    void consultaTeoricaSinApiKeyDevuelveRespuestaTransparenteConMetodologiaParaAnalistaSinClaves() {
        when(llmClient.estaConfigurado()).thenReturn(false);

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Qué significa tener holgura negativa?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp).isNotNull();
        assertThat(resp.proveedorConfigurado()).isFalse();
        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.modeloUtilizado()).isEqualTo("Base de conocimiento SteelForce");
        assertThat(resp.respuesta()).contains("Holgura Operativa");
        // Para ANALISTA y LIDER, nunca se muestran variables internas de configuración
        assertThat(resp.respuesta()).doesNotContain("GEMINI_API_KEY");
        assertThat(resp.respuesta()).doesNotContain(".env");
    }

    @Test
    void consultaSinApiKeyParaAdminIncluyeNotaDiscretaDeConfiguracion() {
        when(llmClient.estaConfigurado()).thenReturn(false);

        var cmd = new ConsultaAsistenteComando(1L, null, Rol.ADMIN, "¿Qué significa tener holgura negativa?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp).isNotNull();
        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.respuesta()).contains("Nota para administradores");
    }

    @Test
    void consultaConLlmConfiguradoExitosoDevuelveMotorGeminiYModeloConfigurado() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");
        when(llmClient.generarRespuesta(any())).thenReturn(
                new LlmClientPort.ResultadoLlm("Explicación de SteelForce generada por Gemini en texto plano", "gemini-3.8-flash", true, null)
        );

        var cmd = new ConsultaAsistenteComando(
                1L, 10L, Rol.ANALISTA, "¿Por qué el proceso tiene 125% de utilización?", null,
                List.of(new MensajeAsistente("user", "Hola"))
        );

        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp.proveedorConfigurado()).isTrue();
        assertThat(resp.motor()).isEqualTo("GEMINI");
        assertThat(resp.respuesta()).isEqualTo("Explicación de SteelForce generada por Gemini en texto plano");
        assertThat(resp.modeloUtilizado()).isEqualTo("gemini-3.8-flash");
        verify(llmClient).generarRespuesta(any());
    }

    @Test
    void consultaConFallo404RespondeConMotorLocalSinFiltrarErrorDelProveedor() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");
        when(llmClient.generarRespuesta(any())).thenReturn(
                new LlmClientPort.ResultadoLlm(null, "gemini-3.8-flash", false, "404 Not Found: {\"error\": {\"message\": \"model deprecated\"}}")
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Qué es sobrecarga?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp).isNotNull();
        assertThat(resp.proveedorConfigurado()).isFalse();
        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.modeloUtilizado()).isEqualTo("Base de conocimiento SteelForce");
        // El usuario NUNCA ve el error 404 ni el JSON del proveedor
        assertThat(resp.respuesta()).doesNotContain("404");
        assertThat(resp.respuesta()).doesNotContain("Not Found");
        assertThat(resp.respuesta()).doesNotContain("{\"error\"");
        assertThat(resp.respuesta()).contains("El asistente avanzado no está disponible en este momento. Te respondo con la base de conocimiento de SteelForce.");
    }

    @Test
    void consultaConFallo429RespondeConMotorLocalSinFiltrarError() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");
        when(llmClient.generarRespuesta(any())).thenReturn(
                new LlmClientPort.ResultadoLlm(null, "gemini-3.8-flash", false, "429 Too Many Requests")
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Qué es cuello de botella?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.respuesta()).doesNotContain("429");
        assertThat(resp.respuesta()).contains("El asistente avanzado no está disponible en este momento");
    }

    @Test
    void consultaConFallo5xxRespondeConMotorLocalSinFiltrarError() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");
        when(llmClient.generarRespuesta(any())).thenReturn(
                new LlmClientPort.ResultadoLlm(null, "gemini-3.8-flash", false, "503 Service Unavailable: High demand")
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Qué es cuello de botella?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.respuesta()).doesNotContain("503");
        assertThat(resp.respuesta()).contains("El asistente avanzado no está disponible en este momento");
    }

    @Test
    void consultaConTimeoutRespondeConMotorLocalSinFiltrarError() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");
        when(llmClient.generarRespuesta(any())).thenReturn(
                new LlmClientPort.ResultadoLlm(null, "gemini-3.8-flash", false, "Read timed out")
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Qué es cuello de botella?", null, List.of());
        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp.motor()).isEqualTo("LOCAL");
        assertThat(resp.respuesta()).doesNotContain("timed out");
        assertThat(resp.respuesta()).contains("El asistente avanzado no está disponible en este momento");
    }

    @Test
    void consultaConReporteInexistenteLanzaNoEncontrado() {
        when(reporteRepo.buscarPorId(999L)).thenReturn(Optional.empty());

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "Analiza este reporte", 999L, List.of());

        assertThatThrownBy(() -> service.consultar(cmd))
                .isInstanceOf(NoEncontradoException.class)
                .hasMessageContaining("Reporte no encontrado");
    }

    @Test
    void consultaConReporteDeOtraEmpresaLanzaSinPermiso() {
        Reporte reporte = new Reporte(50L, 100L, 1L, "Planta Norte", FrecuenciaReporte.MENSUAL,
                LocalDate.now(), LocalDate.now().plusDays(30), new BigDecimal("1000"), new BigDecimal("950"),
                EstadoReporte.ANALIZADO);
        Producto prodEmpresaAjena = new Producto(100L, 999L, "P-AJENO", "Producto Ajeno", "unidades");

        when(reporteRepo.buscarPorId(50L)).thenReturn(Optional.of(reporte));
        when(productoRepo.buscarPorId(100L)).thenReturn(Optional.of(prodEmpresaAjena));

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "Analiza mi reporte", 50L, List.of());

        assertThatThrownBy(() -> service.consultar(cmd))
                .isInstanceOf(SinPermisoException.class)
                .hasMessageContaining("No tienes acceso");
        verifyNoInteractions(llmClient);
    }

    @Test
    void consultaConReporteAnalizadoCargaContextoYFiltraParaLider() {
        Reporte reporte = new Reporte(50L, 100L, 1L, "Planta Norte", FrecuenciaReporte.MENSUAL,
                LocalDate.now(), LocalDate.now().plusDays(30), new BigDecimal("1000"), new BigDecimal("950"),
                EstadoReporte.ANALIZADO);
        Producto prodPropio = new Producto(100L, 10L, "P-01", "Eje Mecánico", "piezas");

        when(reporteRepo.buscarPorId(50L)).thenReturn(Optional.of(reporte));
        when(productoRepo.buscarPorId(100L)).thenReturn(Optional.of(prodPropio));
        when(reporteUseCase.recursosDelLider(50L, 2L)).thenReturn(Set.of(1L)); // solo recurso 1

        AnalisisRecursoReporte anal1 = new AnalisisRecursoReporte(1L, 10L, 1L, "REC-01", "Torno CNC",
                new BigDecimal("200"), new BigDecimal("200"), new BigDecimal("160"), new BigDecimal("800"),
                new BigDecimal("125.0"), new BigDecimal("-40"), new BigDecimal("40"), new BigDecimal("20.0"),
                new BigDecimal("40"), ClasificacionRestriccion.SOBRECARGA, 1, "Sobrecarga en torno", Instant.now());

        AnalisisRecursoReporte anal2 = new AnalisisRecursoReporte(2L, 11L, 2L, "REC-02", "Prensa",
                new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("160"), new BigDecimal("1600"),
                new BigDecimal("62.5"), new BigDecimal("60"), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, ClasificacionRestriccion.BAJA_UTILIZACION, null, "Adecuado", Instant.now());

        when(analisisReporteUseCase.obtenerAnalisis(50L)).thenReturn(
                new DetalleAnalisisReporte(50L, EstadoReporte.ANALIZADO, List.of(), List.of(anal1, anal2))
        );

        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");

        ArgumentCaptor<LlmClientPort.PeticionLlm> captor = ArgumentCaptor.forClass(LlmClientPort.PeticionLlm.class);
        when(llmClient.generarRespuesta(captor.capture())).thenReturn(
                new LlmClientPort.ResultadoLlm("Análisis del torno completado", "gemini-3.8-flash", true, null)
        );

        var cmd = new ConsultaAsistenteComando(2L, 10L, Rol.LIDER, "¿Cómo está mi estación?", 50L, List.of());

        RespuestaAsistente resp = service.consultar(cmd);

        assertThat(resp).isNotNull();
        assertThat(resp.fuentesContexto()).contains("Reporte #50 (Eje Mecánico)");
        verify(reporteUseCase).recursosDelLider(50L, 2L);

        // Verificar que el contexto enviado a Gemini solo contiene el recurso 1 (Torno CNC) y NUNCA el recurso 2 (Prensa)
        String contextoEnviado = captor.getValue().messages().get(0).content();
        assertThat(contextoEnviado).contains("Torno CNC");
        assertThat(contextoEnviado).doesNotContain("REC-02");
        assertThat(contextoEnviado).doesNotContain("Prensa");
    }

    @Test
    void historialLargoSeAcotaAOchoMensajesMaximo() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");

        ArgumentCaptor<LlmClientPort.PeticionLlm> captor = ArgumentCaptor.forClass(LlmClientPort.PeticionLlm.class);
        when(llmClient.generarRespuesta(captor.capture())).thenReturn(
                new LlmClientPort.ResultadoLlm("Respuesta concisa", "gemini-3.8-flash", true, null)
        );

        List<MensajeAsistente> historialLargo = List.of(
                new MensajeAsistente("user", "msg 0"),
                new MensajeAsistente("assistant", "msg 1"),
                new MensajeAsistente("user", "msg 2"),
                new MensajeAsistente("assistant", "msg 3"),
                new MensajeAsistente("user", "msg 4"),
                new MensajeAsistente("assistant", "msg 5"),
                new MensajeAsistente("user", "msg 6"),
                new MensajeAsistente("assistant", "msg 7"),
                new MensajeAsistente("user", "msg 8"),
                new MensajeAsistente("assistant", "msg 9"),
                new MensajeAsistente("user", "msg 10"),
                new MensajeAsistente("assistant", "msg 11")
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Cuál es la restricción actual?", null, historialLargo);

        service.consultar(cmd);

        LlmClientPort.PeticionLlm peticion = captor.getValue();
        // 1 del system prompt + 8 acotados del historial + 1 de la pregunta actual del usuario = 10 mensajes
        assertThat(peticion.messages()).hasSize(10);
        assertThat(peticion.messages().get(0).role()).isEqualTo("system");
        assertThat(peticion.messages().get(1).content()).isEqualTo("msg 4");
        assertThat(peticion.messages().get(8).content()).isEqualTo("msg 11");
        assertThat(peticion.messages().get(9).content()).isEqualTo("¿Cuál es la restricción actual?");
    }

    @Test
    void peticionAlLlmTieneTemperaturaBajaYPromptConReglaDeNoInventarCifras() {
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.obtenerModeloPredeterminado()).thenReturn("gemini-3.8-flash");

        ArgumentCaptor<LlmClientPort.PeticionLlm> captor = ArgumentCaptor.forClass(LlmClientPort.PeticionLlm.class);
        when(llmClient.generarRespuesta(captor.capture())).thenReturn(
                new LlmClientPort.ResultadoLlm("Respuesta técnica", "gemini-3.8-flash", true, null)
        );

        var cmd = new ConsultaAsistenteComando(1L, 10L, Rol.ANALISTA, "¿Cómo balancear la línea?", null, List.of());

        service.consultar(cmd);

        LlmClientPort.PeticionLlm peticion = captor.getValue();
        // Temperatura <= 0.3 para evitar variabilidad / alucinaciones (en código configurado en 0.25)
        assertThat(peticion.temperature()).isLessThanOrEqualTo(0.3);

        String systemPrompt = peticion.messages().get(0).content();
        // Regla estricta de no inventar cifras
        assertThat(systemPrompt).contains("NUNCA inventes números");
        assertThat(systemPrompt).contains("No dispongo de ese dato");
        // Prohibición absoluta de LaTeX
        assertThat(systemPrompt).contains("Prohibición Absoluta de LaTeX");
        // Alineación ODS 9
        assertThat(systemPrompt).contains("ODS 9");
    }
}