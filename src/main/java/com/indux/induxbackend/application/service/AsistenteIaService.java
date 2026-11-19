package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AnalisisReporteUseCase;
import com.indux.induxbackend.application.port.in.AsistenteIaUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.out.*;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.exception.SinPermisoException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.model.asistente.MensajeAsistente;
import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenteIaService implements AsistenteIaUseCase {

    private final LlmClientPort llmClient;
    private final ReporteUseCase reporteUseCase;
    private final ReporteRepositoryPort reporteRepo;
    private final ProductoRepositoryPort productoRepo;
    private final RecursoRepositoryPort recursoRepo;
    private final AnalisisReporteUseCase analisisReporteUseCase;

    private static final String SYSTEM_PROMPT = """
            Eres el Asistente Inteligente oficial de Indux, plataforma empresarial especializada en análisis de capacidad operativa, detección de cuellos de botella y optimización de procesos industriales bajo la metodología SteelForce (alineada con el ODS 9: Industria, Innovación e Infraestructura).

            Directrices estrictas de comunicación, calidad y formato:
            1. Idioma y Tono: Responde SIEMPRE en español, con un tono profesional, técnico, conciso y de ingeniería industrial.
            2. Alcance Temático Estricto: Limítate exclusivamente a: capacidad operativa, detección y alivio de cuellos de botella, cálculo de horas requeridas y disponibles, índice de utilización, holgura operativa, déficit de capacidad, metodología SteelForce, recomendaciones de planta y ODS 9. Si el usuario te consulta sobre temas ajenos (política, cocina, entretenimiento, tareas escolares no industriales, etc.), niégate con amabilidad explicando que tu función está dedicada exclusivamente a la ingeniería y optimización operativa de Indux.
            3. Prohibición Absoluta de LaTeX: Está terminantemente PROHIBIDO utilizar sintaxis de LaTeX en tus respuestas (no utilices barras invertidas, ni delimitadores $$ o $, ni comandos como \\frac, \\times, \\le, \\ge, \\text{}, \\%). Expresa todas las fórmulas en texto plano legible en una sola línea, por ejemplo:
               - Utilización = (Horas requeridas / Horas disponibles) × 100 %
               - Holgura = Horas disponibles - Horas requeridas
               - Déficit de horas = max(0, Horas requeridas - Horas disponibles)
               - Impacto del déficit = (Déficit de horas / Horas requeridas) × 100 %
            4. Clasificación SteelForce (4 Niveles):
               - Baja Utilización: Utilización < 80 % (Capacidad ociosa).
               - Utilización Adecuada: 80 % ≤ Utilización ≤ 90 % (Rango óptimo balanceado).
               - Capacidad Ajustada: 90 % < Utilización ≤ 100 % (Alerta operacional ante variabilidad).
               - Sobrecarga: Utilización > 100 % (Cuello de botella activo).
               - Prioridad 1: Restricción Primaria de la planta.
            5. Fidelidad de Datos (Cero Alucinación): NUNCA inventes números, reportes, recursos ni sustituyas los cálculos oficiales de SteelForce. Basa tus diagnósticos únicamente en los datos reales del reporte proporcionados en el contexto. Si falta algún parámetro o no fue medido, responde con transparencia: "No dispongo de ese dato en el reporte".
            6. Confidencialidad: No menciones datos de otras empresas ni incluyas credenciales, contraseñas, hashes ni correos.
            7. Estilo: Utiliza Markdown simple (títulos ###, listas con guiones y negritas **).
            """;

    @Override
    public RespuestaAsistente consultar(ConsultaAsistenteComando comando) {
        validarComando(comando);

        List<String> fuentesContexto = new ArrayList<>();
        StringBuilder contextoDatos = new StringBuilder();

        // 1. Cargar contexto de reporte si fue especificado
        if (comando.reporteId() != null) {
            Reporte reporte = reporteRepo.buscarPorId(comando.reporteId())
                    .orElseThrow(() -> new NoEncontradoException("Reporte no encontrado con ID: " + comando.reporteId()));

            Producto producto = productoRepo.buscarPorId(reporte.productoId())
                    .orElseThrow(() -> new NoEncontradoException("Producto no encontrado con ID: " + reporte.productoId()));

            // Aislamiento multi-tenancy
            if (comando.rolUsuario() != Rol.ADMIN && !Objects.equals(producto.empresaId(), comando.empresaId())) {
                throw new SinPermisoException("No tienes acceso a los datos de este reporte");
            }

            fuentesContexto.add("Reporte #" + reporte.id() + " (" + producto.nombre() + ")");
            contextoDatos.append("=== CONTEXTO DEL REPORTE SELECCIONADO (DATOS REALES DEL SISTEMA) ===\n");
            contextoDatos.append("- ID Reporte: ").append(reporte.id()).append("\n");
            contextoDatos.append("- Producto: ").append(producto.nombre()).append(" [").append(producto.codigo()).append("]\n");
            contextoDatos.append("- Planta/Área: ").append(reporte.plantaArea()).append("\n");
            contextoDatos.append("- Frecuencia: ").append(reporte.frecuencia()).append("\n");
            contextoDatos.append("- Periodo: ").append(reporte.periodoInicio()).append(" al ").append(reporte.periodoFin()).append("\n");
            contextoDatos.append("- Demanda del Periodo: ").append(reporte.demanda()).append(" ").append(producto.unidadMedida()).append("\n");
            contextoDatos.append("- Producción Real: ").append(reporte.produccionReal()).append(" ").append(producto.unidadMedida()).append("\n");
            contextoDatos.append("- Estado actual del reporte: ").append(reporte.estado()).append("\n");

            if (reporte.estado() == EstadoReporte.ANALIZADO) {
                try {
                    DetalleAnalisisReporte analisis = analisisReporteUseCase.obtenerAnalisis(reporte.id());
                    Set<Long> recursosPermitidos = (comando.rolUsuario() == Rol.LIDER)
                            ? reporteUseCase.recursosDelLider(reporte.id(), comando.usuarioId())
                            : null;

                    contextoDatos.append("\n--- RESULTADOS DEL ANÁLISIS STEELFORCE ---\n");
                    for (AnalisisRecursoReporte anal : analisis.recursos()) {
                        if (recursosPermitidos != null && !recursosPermitidos.contains(anal.recursoId())) {
                            continue;
                        }
                        contextoDatos.append(String.format(
                                "* Recurso: %s (Código: %s) | Clasificación: %s | Utilización: %.2f %% | Horas requeridas: %.2f h | Horas disponibles: %.2f h | Holgura: %.2f h | Déficit: %.2f h | Impacto: %.2f %% | Prioridad: %s\n  Diagnóstico: %s\n",
                                anal.nombre(), anal.codigo(), anal.clasificacion(), anal.utilizacion(),
                                anal.horasRequeridas(), anal.horasDisponibles(), anal.holgura(), anal.deficitHoras(),
                                anal.impacto(), anal.prioridad() != null ? anal.prioridad() : "N/A", anal.diagnostico()
                        ));
                    }

                    // Recomendaciones
                    try {
                        List<Recomendacion> recs = analisisReporteUseCase.obtenerRecomendaciones(reporte.id());
                        if (!recs.isEmpty()) {
                            contextoDatos.append("\n--- RECOMENDACIONES GENERADAS POR STEELFORCE ---\n");
                            for (Recomendacion r : recs) {
                                if (recursosPermitidos != null && !recursosPermitidos.contains(r.recursoId())) {
                                    continue;
                                }
                                contextoDatos.append(String.format(
                                        "* [%s - Prioridad %s] %s: %s (Acciones sugeridas: %s)\n",
                                        r.nivel(), r.prioridad(), r.nombre(), r.resumen(), String.join("; ", r.acciones())
                                ));
                            }
                        }
                    } catch (Exception e) {
                        log.debug("No se pudieron cargar recomendaciones: {}", e.getMessage());
                    }

                    // Causas
                    try {
                        List<CausaDeficitRecurso> causas = analisisReporteUseCase.detectarCausas(reporte.id());
                        if (!causas.isEmpty()) {
                            contextoDatos.append("\n--- DESGLOSE CAUSAL DEL DÉFICIT ---\n");
                            for (CausaDeficitRecurso c : causas) {
                                if (recursosPermitidos != null && !recursosPermitidos.contains(c.recursoId())) {
                                    continue;
                                }
                                contextoDatos.append(String.format(
                                        "* Recurso %s (Déficit: %.2f h) -> Causa principal: %s\n",
                                        c.nombre(), c.deficitHoras(), c.causaPrincipal()
                                ));
                            }
                        }
                    } catch (Exception e) {
                        log.debug("No se pudieron cargar causas: {}", e.getMessage());
                    }

                } catch (Exception e) {
                    log.warn("No se pudo obtener el análisis del reporte {}: {}", reporte.id(), e.getMessage());
                }
            } else if (reporte.estado() == EstadoReporte.VALIDADO || reporte.estado() == EstadoReporte.CON_ERRORES) {
                var valids = analisisReporteUseCase.obtenerValidaciones(reporte.id());
                contextoDatos.append("\n--- HALLAZGOS DE VALIDACIÓN ---\n");
                for (ValidacionResultado v : valids) {
                    contextoDatos.append(String.format("* [%s] %s (%s): %s\n", v.severidad(), v.grupo(), v.variable(), v.mensaje()));
                }
            }
        } else if (comando.empresaId() != null) {
            fuentesContexto.add("Resumen operativo de la empresa");
            try {
                List<Reporte> ultimos = reporteRepo.listarPorEmpresa(comando.empresaId());
                contextoDatos.append("=== RESUMEN GENERAL DE LA EMPRESA ===\n");
                contextoDatos.append("- Total de reportes registrados: ").append(ultimos.size()).append("\n");
                long analizados = ultimos.stream().filter(r -> r.estado() == EstadoReporte.ANALIZADO).count();
                contextoDatos.append("- Reportes analizados: ").append(analizados).append("\n");
            } catch (Exception e) {
                log.debug("Error al listar reportes de empresa: {}", e.getMessage());
            }
        }

        // 2. Si el proveedor LLM está configurado, invocarlo con Google Gemini (AI Studio)
        if (llmClient.estaConfigurado()) {
            List<LlmClientPort.MensajeLlm> mensajesParaLlm = new ArrayList<>();
            String promptCompleto = SYSTEM_PROMPT;
            if (contextoDatos.length() > 0) {
                promptCompleto += "\n\n" + contextoDatos;
            }
            mensajesParaLlm.add(new LlmClientPort.MensajeLlm("system", promptCompleto));

            // Agregar historial previo si existe (últimos 8 mensajes para controlar ventana de contexto)
            if (comando.historial() != null && !comando.historial().isEmpty()) {
                int desde = Math.max(0, comando.historial().size() - 8);
                for (int i = desde; i < comando.historial().size(); i++) {
                    MensajeAsistente m = comando.historial().get(i);
                    String role = "assistant".equalsIgnoreCase(m.rol()) ? "assistant" : "user";
                    mensajesParaLlm.add(new LlmClientPort.MensajeLlm(role, m.contenido()));
                }
            }

            mensajesParaLlm.add(new LlmClientPort.MensajeLlm("user", comando.mensaje()));

            var peticion = new LlmClientPort.PeticionLlm(
                    llmClient.obtenerModeloPredeterminado(),
                    mensajesParaLlm,
                    0.25, // Temperatura baja para respuestas estables y técnicas
                    1500
            );

            var resultado = llmClient.generarRespuesta(peticion);
            if (resultado.exito()) {
                return new RespuestaAsistente(
                        resultado.contenido(),
                        resultado.modelo(),
                        true,
                        fuentesContexto,
                        "GEMINI"
                );
            }

            // Error técnico registrado SOLO en logs del servidor. NUNCA se expone al usuario final.
            log.warn("Falla en el proveedor LLM Gemini: {}. Activando contingencia local SteelForce.", resultado.detalleError());
            return new RespuestaAsistente(
                    "El asistente avanzado no está disponible en este momento. Te respondo con la base de conocimiento de SteelForce.\n\n" +
                    generarRespuestaDeterministica(comando, contextoDatos.toString()),
                    "Base de conocimiento SteelForce",
                    false,
                    fuentesContexto,
                    "LOCAL"
            );
        }

        // 3. Si no hay API Key configurada, respuesta determinística con el motor local
        String respuestaFallback = generarRespuestaDeterministica(comando, contextoDatos.toString());
        if (comando.rolUsuario() == Rol.ADMIN) {
            respuestaFallback += "\n\n*(Nota para administradores: Para habilitar el modelo Gemini en vivo, configura GEMINI_API_KEY en las variables de entorno del backend).*";
        }

        return new RespuestaAsistente(
                respuestaFallback,
                "Base de conocimiento SteelForce",
                false,
                fuentesContexto,
                "LOCAL"
        );
    }

    private void validarComando(ConsultaAsistenteComando comando) {
        if (comando == null || comando.mensaje() == null || comando.mensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje de consulta no puede estar vacío");
        }
        if (comando.mensaje().length() > 2500) {
            throw new IllegalArgumentException("El mensaje de consulta no puede superar los 2500 caracteres");
        }
    }

    private String generarRespuestaDeterministica(ConsultaAsistenteComando comando, String contexto) {
        String msg = comando.mensaje().toLowerCase(Locale.ROOT);

        if (contexto != null && !contexto.isBlank() && (msg.contains("este reporte") || msg.contains("analiza") || msg.contains("cuello") || msg.contains("utilizaci"))) {
            return "### Diagnóstico Operativo SteelForce sobre los datos cargados:\n\n" +
                   "Basándome en la información oficial registrada en Indux:\n\n" +
                   contexto + "\n\n" +
                   "**Interpretación Técnica:**\n" +
                   "- Todo recurso con utilización superior al 100 % se encuentra en estado de **SOBRECARGA** y constituye un cuello de botella activo.\n" +
                   "- El recurso con Prioridad 1 es tu **Restricción Primaria**: cualquier mejora en otras estaciones no incrementará la producción global si no se alivia primero esta restricción.\n" +
                   "- Se recomienda revisar el factor causal principal del déficit (por ejemplo, si se debe a *Demanda Excesiva*, evaluar habilitación de horas extra o tercer turno; si es *Recurso Compartido*, desincronizar lotes de producción).";
        }

        if (msg.contains("holgura")) {
            return "### Holgura Operativa en SteelForce\n\n" +
                   "La Holgura Operativa se define matemáticamente como la diferencia entre las horas disponibles y las horas requeridas:\n\n" +
                   "Holgura = Horas disponibles - Horas requeridas\n\n" +
                   "- **Holgura Positiva (> 0):** El puesto cuenta con capacidad remanente para amortiguar variabilidad o absorber lotes adicionales.\n" +
                   "- **Holgura Cero (= 0):** El puesto opera exactamente a plena capacidad (Utilización = 100 %).\n" +
                   "- **Holgura Negativa (< 0):** Indica un Déficit de Horas, lo que sitúa al recurso en estado de SOBRECARGA (Utilización > 100 %) y cuello de botella activo.";
        }

        if (msg.contains("utilizac") || msg.contains("125%") || msg.contains("100%")) {
            return "### Índice de Utilización Operativa\n\n" +
                   "El índice de utilización mide la carga porcentual exigida a un recurso respecto a su disponibilidad efectiva:\n\n" +
                   "Utilización = (Horas requeridas / Horas disponibles) × 100 %\n\n" +
                   "En SteelForce se clasifica en 4 estados operativos:\n" +
                   "1. **Baja Utilización (Utilización < 80 %):** Capacidad ociosa disponible.\n" +
                   "2. **Utilización Adecuada (80 % ≤ Utilización ≤ 90 %):** Régimen óptimo y balanceado según buenas prácticas de manufactura.\n" +
                   "3. **Capacidad Ajustada (90 % < Utilización ≤ 100 %):** Alerta operacional; alta sensibilidad a paradas técnicas o microparadas.\n" +
                   "4. **Sobrecarga (Utilización > 100 %):** Cuello de botella. Por ejemplo, una utilización de 125 % significa que la demanda requiere un 25 % más de horas de las que el recurso puede entregar en su esquema de turnos actual.";
        }

        if (msg.contains("cuello") || msg.contains("botella") || msg.contains("restricci")) {
            return "### Gestión y Alivio de Cuellos de Botella en SteelForce\n\n" +
                   "Siguiendo la Teoría de Restricciones aplicada en la metodología SteelForce:\n\n" +
                   "1. **Identificar la Restricción Primaria (Prioridad 1):** Es el puesto con mayor déficit de horas e impacto operacional.\n" +
                   "2. **Explotar el Cuello de Botella:** Garantizar que esta máquina nunca se detenga por falta de material o descansos no escalonados.\n" +
                   "3. **Reducción de Tiempos de Preparación (SMED):** Disminuir tiempos de set-up para convertir horas improductivas en capacidad útil.\n" +
                   "4. **Subordinación:** Adecuar el ritmo de las estaciones aguas arriba para no acumular inventario en proceso (WIP) innecesario frente a la restricción.\n" +
                   "5. **Elevar la Capacidad:** Si la sobrecarga persiste, programar un turno adicional, horas extras o balancear lotes con máquinas homólogas.";
        }

        if (msg.contains("causa") || msg.contains("déficit") || msg.contains("deficit")) {
            return "### Detección Causal del Déficit en SteelForce\n\n" +
                   "Cuando un recurso entra en sobrecarga, el algoritmo DetectorCausas desglosa el déficit total en 5 factores acotados matemáticamente:\n\n" +
                   "1. **RECURSO_COMPARTIDO:** Horas demandadas por otros productos de la empresa que compiten por la misma máquina en periodos solapados.\n" +
                   "2. **TIEMPO_PROCESAMIENTO:** Exceso de tiempo de ciclo cuando el procesamiento real supera en más del 10 % el tiempo de referencia de ingeniería.\n" +
                   "3. **MERMA:** Insumos adicionales exigidos por mermas porcentuales en etapas previas o en la misma estación.\n" +
                   "4. **TIEMPO_NO_DISPONIBLE:** Paradas técnicas, mantenimientos correctivos o demoras no programadas.\n" +
                   "5. **DEMANDA:** Carga residual imputable directamente al volumen comercial exigido.";
        }

        return "### Asistente Inteligente Indux (Metodología SteelForce)\n\n" +
               "Estoy a tu disposición para orientarte en la optimización operativa de tus plantas industriales:\n\n" +
               "- **Explicación de Indicadores:** Pregúntame sobre horas requeridas, horas disponibles, utilización, holgura y déficit.\n" +
               "- **Diagnóstico de Reportes:** Selecciona un reporte en el selector superior y te explicaré el cuello de botella detectado y sus factores causales.\n" +
               "- **Planes de Acción:** Puedo recomendarte acciones preventivas y correctivas basadas en los 5 niveles de recomendación de SteelForce (Crítica, Alta, Media, Baja e Informativa).\n" +
               "- **Alineación ODS 9:** Asesoría sobre eficiencia energética y uso racional de maquinaria industrial.";
    }
}