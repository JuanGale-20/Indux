package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

public record ValidarProcesoRequest(String codigo, String nombre, Integer secuencia, String unidadProcesada) {
}