package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.validacion.ResultadoValidacion;

public interface ValidacionUseCase {

    ResultadoValidacion validarEmpresa(String nombre, String sector);

    ResultadoValidacion validarProducto(String codigo, String nombre, String unidadMedida);

    ResultadoValidacion validarProceso(String codigo, String nombre, int secuencia, String unidadProcesada);
}
