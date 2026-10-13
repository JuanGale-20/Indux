package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;

import java.util.List;

public interface UsuarioUseCase {

    Usuario registrar(RegistrarUsuarioComando comando);

    List<Usuario> listar();

    Usuario actualizar(Long id, ActualizarUsuarioComando comando);

    void desactivar(Long id);

    record RegistrarUsuarioComando(Long empresaId, String nombre, String email,
                                   String password, Rol rol) {

        @Override
        public String toString() {
            return "RegistrarUsuarioComando[email=" + email + ", rol=" + rol + "]";
        }
    }

    /** Si password es nulo o vacío, se conserva la contraseña actual. */
    record ActualizarUsuarioComando(Long empresaId, String nombre, String email,
                                    String password, Rol rol) {

        @Override
        public String toString() {
            return "ActualizarUsuarioComando[email=" + email + ", rol=" + rol + "]";
        }
    }
}