package com.indux.induxbackend.domain.model;

public record ValidacionResultado(Long id, Long reporteId, String grupo, String variable,
                                  SeveridadValidacion severidad, String mensaje) {

    public ValidacionResultado {
        grupo = truncar(grupo, 30);
        variable = truncar(variable, 60);
        mensaje = truncar(mensaje, 300);
        if (severidad == null) {
            severidad = SeveridadValidacion.ADVERTENCIA;
        }
    }

    public ValidacionResultado(String grupo, String variable, SeveridadValidacion severidad, String mensaje) {
        this(null, null, grupo, variable, severidad, mensaje);
    }

    public ValidacionResultado conReporte(Long nuevoReporteId) {
        return new ValidacionResultado(id, nuevoReporteId, grupo, variable, severidad, mensaje);
    }

    public boolean esErrorCritico() {
        return severidad == SeveridadValidacion.ERROR_CRITICO;
    }

    private static String truncar(String valor, int maximo) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim();
        return limpio.length() > maximo ? limpio.substring(0, maximo) : limpio;
    }
}
