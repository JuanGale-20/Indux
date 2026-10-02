package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Usuario;

public interface AutenticacionUseCase {

    SesionIniciada iniciarSesion(LoginComando comando);

    record LoginComando(String email, String password) {
        @Override
        public String toString() {
            return "LoginComando[email=" + email + "]";
        }
    }

    record SesionIniciada(String token, long expiraEnSegundos, Usuario usuario) {
    }
}
