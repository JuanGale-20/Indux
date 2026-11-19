package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.AsistenteIaUseCase;
import com.indux.induxbackend.application.port.in.AsistenteIaUseCase.ConsultaAsistenteComando;
import com.indux.induxbackend.domain.model.asistente.MensajeAsistente;
import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ConsultaAsistenteRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.RespuestaAsistenteResponse;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
@Tag(name = "Asistente IA", description = "Asistente inteligente con modelo LLM para análisis operativo y metodología SteelForce")
public class AsistenteIaController {

    private final AsistenteIaUseCase asistenteUseCase;

    @Operation(summary = "Enviar consulta al asistente de IA con contexto de planta y SteelForce")
    @PostMapping("/chat")
    public ResponseEntity<RespuestaAsistenteResponse> consultar(
            @Valid @RequestBody ConsultaAsistenteRequest request,
            Authentication autenticacion
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();

        List<MensajeAsistente> historial = (request.historial() != null)
                ? request.historial().stream()
                .map(m -> new MensajeAsistente(m.rol(), m.contenido()))
                .toList()
                : List.of();

        ConsultaAsistenteComando comando = new ConsultaAsistenteComando(
                usuario.id(),
                usuario.empresaId(),
                usuario.rol(),
                request.mensaje(),
                request.reporteId(),
                historial
        );

        RespuestaAsistente respuesta = asistenteUseCase.consultar(comando);
        return ResponseEntity.ok(RespuestaAsistenteResponse.desde(respuesta));
    }
}
