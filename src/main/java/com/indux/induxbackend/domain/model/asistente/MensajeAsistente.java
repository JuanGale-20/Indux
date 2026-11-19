package com.indux.induxbackend.domain.model.asistente;

public record MensajeAsistente(
        String rol,
        String contenido
) {
    public MensajeAsistente {
        if (rol == null || rol.isBlank()) {
            rol = "user";
        }
        if (contenido == null) {
            contenido = "";
        }
    }
}
