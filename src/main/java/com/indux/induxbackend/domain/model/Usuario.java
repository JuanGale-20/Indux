package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.util.regex.Pattern;

public record Usuario(Long id, Long empresaId, String nombre, String email,
                      String passwordHash, Rol rol, boolean activo) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Usuario {
        nombre = Reglas.textoObligatorio(nombre, "El nombre del usuario");
        email = Reglas.textoObligatorio(email, "El correo").toLowerCase();
        passwordHash = Reglas.textoObligatorio(passwordHash, "La contraseña");

        if (!EMAIL.matcher(email).matches()) {
            throw new DominioException("El correo no tiene un formato válido");
        }
        if (rol == null) {
            throw new DominioException("El rol es obligatorio");
        }
        if (rol == Rol.ADMIN && empresaId != null) {
            throw new DominioException("El administrador de la plataforma no pertenece a una empresa");
        }
        if (rol != Rol.ADMIN && empresaId == null) {
            throw new DominioException("Los usuarios Líder y Analista deben pertenecer a una empresa");
        }
    }

    // Evita que el hash termine en los logs
    @Override
    public String toString() {
        return "Usuario[id=" + id + ", email=" + email + ", rol=" + rol + "]";
    }
}
