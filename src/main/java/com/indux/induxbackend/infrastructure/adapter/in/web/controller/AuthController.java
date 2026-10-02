package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.AutenticacionUseCase;
import com.indux.induxbackend.application.port.in.AutenticacionUseCase.LoginComando;
import com.indux.induxbackend.application.port.in.AutenticacionUseCase.SesionIniciada;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.LoginRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.LoginResponse;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacionUseCase autenticacion;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        SesionIniciada sesion = autenticacion.iniciarSesion(
                new LoginComando(request.email(), request.password()));
        return new LoginResponse(sesion.token(), "Bearer", sesion.expiraEnSegundos(),
                UsuarioResponse.desde(sesion.usuario()));
    }
}