package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.AnalisisReporteUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase.ActualizarReporteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase.CrearReporteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase.DetalleReporte;
import com.indux.induxbackend.application.port.in.ReporteUseCase.LineaComando;
import com.indux.induxbackend.domain.model.CausaDeficitRecurso;
import com.indux.induxbackend.domain.model.DetalleAnalisisReporte;
import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.Recomendacion;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.*;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Gestión de reportes de producción, validación y análisis de capacidad")
public class ReporteController {

    private final ReporteUseCase reportes;
    private final AnalisisReporteUseCase analisis;
    private final AccesoPorEmpresa acceso;

    @Operation(summary = "Crear un nuevo reporte de producción")
    @PostMapping
    public ResponseEntity<ReporteResponse> crear(@Valid @RequestBody CrearReporteRequest r,
                                                 Authentication autenticacion) {
        acceso.producto(autenticacion, r.productoId());
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        DetalleReporte creado = reportes.crear(new CrearReporteComando(
                r.productoId(), usuario.id(), r.plantaArea(), r.frecuencia(), r.periodoInicio(),
                r.periodoFin(), r.demanda(), r.produccionReal(), lineas(r.lineas())));
        return ResponseEntity.created(URI.create("/api/reportes/" + creado.reporte().id()))
                .body(ReporteResponse.desde(creado));
    }

    @Operation(summary = "Listar reportes por producto o empresa")
    @GetMapping
    public List<ReporteResumenResponse> listar(@RequestParam(required = false) Long productoId,
                                               @RequestParam(required = false) Long empresaId,
                                               Authentication autenticacion) {
        if (productoId != null) {
            acceso.producto(autenticacion, productoId);
            return reportes.listarPorProducto(productoId).stream().map(ReporteResumenResponse::desde).toList();
        }
        Long empresa = (empresaId != null)
                ? empresaId
                : ((UsuarioAutenticado) autenticacion.getPrincipal()).empresaId();
        acceso.verificarEmpresa(autenticacion, empresa);
        return reportes.listarPorEmpresa(empresa).stream().map(ReporteResumenResponse::desde).toList();
    }

    @Operation(summary = "Obtener detalle de un reporte por ID")
    @GetMapping("/{id}")
    public ReporteResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() == Rol.LIDER) {
            Set<Long> permitidos = reportes.recursosDelLider(id, usuario.id());
            DetalleReporte filtrado = new DetalleReporte(
                    detalle.reporte(),
                    detalle.lineas().stream().filter(l -> permitidos.contains(l.recursoId())).toList());
            return ReporteResponse.desde(filtrado);
        }
        return ReporteResponse.desde(detalle);
    }

    @Operation(summary = "Actualizar datos y líneas de un reporte")
    @PutMapping("/{id}")
    public ReporteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarReporteRequest r,
                                      Authentication autenticacion) {
        acceso.producto(autenticacion, reportes.obtener(id).reporte().productoId());
        return ReporteResponse.desde(reportes.actualizar(id, new ActualizarReporteComando(
                r.plantaArea(), r.frecuencia(), r.periodoInicio(), r.periodoFin(),
                r.demanda(), r.produccionReal(), lineas(r.lineas()))));
    }

    @Operation(summary = "Eliminar un reporte")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        acceso.producto(autenticacion, reportes.obtener(id).reporte().productoId());
        reportes.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validar coherencia de los datos del reporte")
    @PostMapping("/{id}/validar")
    public ResponseEntity<ResumenValidacionResponse> validar(@PathVariable Long id, Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        return ResponseEntity.ok(ResumenValidacionResponse.desde(analisis.validar(id)));
    }

    @Operation(summary = "Obtener resultados de validación del reporte")
    @GetMapping("/{id}/validaciones")
    public ResponseEntity<List<ValidacionResultadoResponse>> obtenerValidaciones(@PathVariable Long id,
                                                                                Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        List<ValidacionResultadoResponse> res = analisis.obtenerValidaciones(id).stream()
                .map(ValidacionResultadoResponse::desde)
                .toList();
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Ejecutar análisis de capacidad sobre el reporte")
    @PostMapping("/{id}/analizar")
    public ResponseEntity<AnalisisReporteResponse> analizar(@PathVariable Long id, Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        DetalleAnalisisReporte resultado = analisis.analizar(id);
        if (resultado.estado() == EstadoReporte.CON_ERRORES) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(AnalisisReporteResponse.desde(resultado));
        }

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() == Rol.LIDER) {
            resultado = filtrarParaLider(resultado, id, usuario.id());
        }
        return ResponseEntity.ok(AnalisisReporteResponse.desde(resultado));
    }

    @Operation(summary = "Consultar último análisis guardado del reporte")
    @GetMapping("/{id}/analisis")
    public ResponseEntity<AnalisisReporteResponse> obtenerAnalisis(@PathVariable Long id, Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        DetalleAnalisisReporte resultado = analisis.obtenerAnalisis(id);
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() == Rol.LIDER) {
            resultado = filtrarParaLider(resultado, id, usuario.id());
        }
        return ResponseEntity.ok(AnalisisReporteResponse.desde(resultado));
    }

    @Operation(summary = "Obtener recomendaciones accionables para los recursos del reporte")
    @GetMapping("/{id}/recomendaciones")
    public ResponseEntity<List<Recomendacion>> obtenerRecomendaciones(@PathVariable Long id,
                                                                      Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        List<Recomendacion> recs = analisis.obtenerRecomendaciones(id);
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() == Rol.LIDER) {
            Set<Long> permitidos = reportes.recursosDelLider(id, usuario.id());
            recs = recs.stream().filter(r -> permitidos.contains(r.recursoId())).toList();
        }
        return ResponseEntity.ok(recs);
    }

    @Operation(summary = "Identificar y desglosar las causas del déficit en recursos restringidos")
    @GetMapping("/{id}/causas")
    public ResponseEntity<List<CausaDeficitResponse>> obtenerCausas(@PathVariable Long id,
                                                                    Authentication autenticacion) {
        DetalleReporte detalle = reportes.obtener(id);
        acceso.producto(autenticacion, detalle.reporte().productoId());

        List<CausaDeficitRecurso> causas = analisis.detectarCausas(id);
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() == Rol.LIDER) {
            Set<Long> permitidos = reportes.recursosDelLider(id, usuario.id());
            causas = causas.stream().filter(c -> permitidos.contains(c.recursoId())).toList();
        }

        List<CausaDeficitResponse> response = causas.stream()
                .map(CausaDeficitResponse::desde)
                .toList();
        return ResponseEntity.ok(response);
    }

    private DetalleAnalisisReporte filtrarParaLider(DetalleAnalisisReporte d, Long reporteId, Long liderId) {
        Set<Long> permitidos = reportes.recursosDelLider(reporteId, liderId);
        return new DetalleAnalisisReporte(
                d.reporteId(),
                d.estado(),
                d.validaciones(),
                d.recursos().stream().filter(r -> permitidos.contains(r.recursoId())).toList());
    }

    private static List<LineaComando> lineas(List<LineaReporteRequest> lineas) {
        if (lineas == null) {
            return null;
        }
        return lineas.stream().map(l -> new LineaComando(l.recursoId(), l.tiempoProcesamiento(),
                l.unidadReferencia(), l.tiempoCalendario(), l.tiempoNoDisponible(),
                l.numeroTurnos(), l.horasPorTurno(), l.diasOperacion())).toList();
    }
}
