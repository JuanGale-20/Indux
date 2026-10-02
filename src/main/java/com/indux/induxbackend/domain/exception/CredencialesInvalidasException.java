package com.indux.induxbackend.domain.exception;

public class CredencialesInvalidasException extends DominioException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
