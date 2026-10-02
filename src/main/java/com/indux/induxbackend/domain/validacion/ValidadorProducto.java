package com.indux.induxbackend.domain.validacion;

import java.util.ArrayList;
import java.util.List;

public class ValidadorProducto {

    private static final String GRUPO = "Producto";

    private final Catalogo unidades;

    public ValidadorProducto(Catalogo unidades) {
        this.unidades = unidades;
    }

    public ResultadoValidacion validar(String codigo, String nombre, String unidadMedida) {
        List<Hallazgo> hallazgos = new ArrayList<>();

        if (Texto.vacio(codigo)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Código del producto",
                    "El código del producto no puede estar vacío"));
        }
        if (Texto.vacio(nombre)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Nombre del producto",
                    "El nombre del producto no puede estar vacío"));
        }
        if (Texto.vacio(unidadMedida)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Unidad de medida",
                    "La unidad de medida debe estar definida"));
        } else if (!unidades.estaVacio() && !unidades.contiene(unidadMedida)) {
            hallazgos.add(Hallazgo.advertencia(GRUPO, "Unidad de medida",
                    "La unidad \"" + unidadMedida.trim() + "\" no está en el catálogo ("
                            + unidades.descripcion() + "); revísala"));
        }
        return new ResultadoValidacion(hallazgos);
    }
}
