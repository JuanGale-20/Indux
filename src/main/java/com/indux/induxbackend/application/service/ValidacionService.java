package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ValidacionUseCase;
import com.indux.induxbackend.domain.validacion.ResultadoValidacion;
import com.indux.induxbackend.domain.validacion.ValidadorEmpresa;
import com.indux.induxbackend.domain.validacion.ValidadorProceso;
import com.indux.induxbackend.domain.validacion.ValidadorProducto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidacionService implements ValidacionUseCase {

    private final ValidadorEmpresa empresa;
    private final ValidadorProducto producto;
    private final ValidadorProceso proceso;

    @Override
    public ResultadoValidacion validarEmpresa(String nombre, String sector) {
        return empresa.validar(nombre, sector);
    }

    @Override
    public ResultadoValidacion validarProducto(String codigo, String nombre, String unidadMedida) {
        return producto.validar(codigo, nombre, unidadMedida);
    }

    @Override
    public ResultadoValidacion validarProceso(String codigo, String nombre, int secuencia, String unidadProcesada) {
        return proceso.validar(codigo, nombre, secuencia, unidadProcesada);
    }
}
