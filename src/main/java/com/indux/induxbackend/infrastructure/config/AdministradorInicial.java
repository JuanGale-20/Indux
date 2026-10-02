package com.indux.induxbackend.infrastructure.config;

import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.RegistrarUsuarioComando;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.model.Rol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioRepositoryPort usuarios;
    private final UsuarioUseCase registro;
    private final String email;
    private final String password;

    public AdministradorInicial(UsuarioRepositoryPort usuarios, UsuarioUseCase registro,
                                @Value("${indux.admin.email}") String email,
                                @Value("${indux.admin.password}") String password) {
        this.usuarios = usuarios;
        this.registro = registro;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.existePorEmail(email.trim().toLowerCase())) {
            return;
        }
        registro.registrar(new RegistrarUsuarioComando(null, "Administrador", email, password, Rol.ADMIN));
        log.info("Administrador inicial creado: {}", email);
    }
}