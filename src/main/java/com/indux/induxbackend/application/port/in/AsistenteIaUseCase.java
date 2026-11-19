package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.asistente.MensajeAsistente;
import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;

import java.util.List;

public interface AsistenteIaUseCase {

    record ConsultaAsistenteComando(
            Long usuarioId,
            Long empresaId,
            Rol rolUsuario,
            String mensaje,
            Long reporteId,
            List<MensajeAsistente> historial
    ) {
        public ConsultaAsistenteComando {
            if (historial == null) {
                historial = List.of();
            }
        }
    }

    RespuestaAsistente consultar(ConsultaAsistenteComando comando);
}
