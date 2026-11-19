package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;
import java.util.List;

public record RespuestaAsistenteResponse(
        String respuesta,
        String modelo,
        boolean proveedorConfigurado,
        List<String> fuentesContexto,
        String motor
) {
    public static RespuestaAsistenteResponse desde(RespuestaAsistente r) {
        return new RespuestaAsistenteResponse(
                r.respuesta(),
                r.modeloUtilizado(),
                r.proveedorConfigurado(),
                r.fuentesContexto(),
                r.motor()
        );
    }
}