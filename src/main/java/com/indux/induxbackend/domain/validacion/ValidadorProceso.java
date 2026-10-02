package com.indux.induxbackend.domain.validacion;

import java.util.ArrayList;
import java.util.List;

public class ValidadorProceso {

    private static final String GRUPO = "Proceso";

    private final Catalogo unidades;

    public ValidadorProceso(Catalogo unidades) {
        this.unidades = unidades;
    }

    public ResultadoValidacion validar(String codigo, String nombre, int secuencia, String unidadProcesada) {
        List<Hallazgo> hallazgos = new ArrayList<>();

        if (Texto.vacio(codigo)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Código del proceso",
                    "El código del proceso no puede estar vacío"));
        }
        if (Texto.vacio(nombre)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Nombre del proceso",
                    "El nombre del proceso no puede estar vacío"));
        }
        if (secuencia <= 0) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Secuencia del proceso",
                    "La secuencia debe ser un número mayor que 0"));
        }
        if (Texto.vacio(unidadProcesada)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Unidad procesada",
                    "La unidad procesada debe estar definida"));
        } else if (!unidades.estaVacio() && !unidades.contiene(unidadProcesada)) {
            hallazgos.add(Hallazgo.advertencia(GRUPO, "Unidad procesada",
                    "La unidad \"" + unidadProcesada.trim() + "\" no está en el catálogo ("
                            + unidades.descripcion() + "); revísala"));
        }
        return new ResultadoValidacion(hallazgos);
    }
}
