package com.indux.induxbackend.domain.model;

public record Empresa(Long id, String nombre, String sector, boolean activa) {
    public Empresa {
        nombre = Reglas.textoObligatorio(nombre, "El nombre de la empresa");
        // El sector es categórico: si es inválido, SteelForce solo exige una advertencia
    }
}
