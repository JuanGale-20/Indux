package com.indux.induxbackend.domain.validacion;

import java.util.ArrayList;
import java.util.List;

public class ValidadorEmpresa {

    private static final String GRUPO = "Empresa";

    private final Catalogo sectores;

    public ValidadorEmpresa(Catalogo sectores) {
        this.sectores = sectores;
    }

    public ResultadoValidacion validar(String nombre, String sector) {
        List<Hallazgo> hallazgos = new ArrayList<>();

        if (Texto.vacio(nombre)) {
            hallazgos.add(Hallazgo.critico(GRUPO, "Nombre de empresa",
                    "El nombre de la empresa no puede estar vacío"));
        }
        if (!sectores.estaVacio() && !sectores.contiene(sector)) {
            hallazgos.add(Hallazgo.advertencia(GRUPO, "Sector manufacturero",
                    "El sector debe pertenecer a una categoría válida (" + sectores.descripcion() + ")"));
        }
        return new ResultadoValidacion(hallazgos);
    }
}