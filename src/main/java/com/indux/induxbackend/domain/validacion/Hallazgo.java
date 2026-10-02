package com.indux.induxbackend.domain.validacion;

public record Hallazgo(String grupo, String variable, Severidad severidad, String mensaje) {

    public static Hallazgo critico(String grupo, String variable, String mensaje) {
        return new Hallazgo(grupo, variable, Severidad.ERROR_CRITICO, mensaje);
    }

    public static Hallazgo advertencia(String grupo, String variable, String mensaje) {
        return new Hallazgo(grupo, variable, Severidad.ADVERTENCIA, mensaje);
    }
}
