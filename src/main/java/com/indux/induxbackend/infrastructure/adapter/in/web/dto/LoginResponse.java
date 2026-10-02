package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

public record LoginResponse(String token, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {
}