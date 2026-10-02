package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;

public interface UsuarioUseCase {

    Usuario registrar(RegistrarUsuarioComando comando);

    record RegistrarUsuarioComando(Long empresaId, String nombre, String email,
                                   String password, Rol rol) {

        @Override
        public String toString() {
            return "RegistrarUsuarioComando[email=" + email + ", rol=" + rol + "]";
        }
    }
}
