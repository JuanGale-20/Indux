package com.indux.induxbackend.infrastructure.adapter.out.security;

import com.indux.induxbackend.application.port.out.TokenProviderPort;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService implements TokenProviderPort {

    private final SecretKey clave;
    private final long minutos;

    public JwtService(@Value("${indux.jwt.secret}") String secreto,
                      @Value("${indux.jwt.expiracion-minutos}") long minutos) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("El secreto JWT debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.minutos = minutos;
    }

    @Override
    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtBuilder token = Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim("email", usuario.email())
                .claim("rol", usuario.rol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(minutos * 60)));
        if (usuario.empresaId() != null) {
            token.claim("empresaId", usuario.empresaId());
        }
        return token.signWith(clave).compact();
    }

    @Override
    public long segundosDeVigencia() {
        return minutos * 60;
    }

    /** Devuelve el usuario del token, o vacío si es inválido, alterado o expirado. */
    public Optional<UsuarioAutenticado> leer(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload();
            Number empresa = c.get("empresaId", Number.class);
            return Optional.of(new UsuarioAutenticado(
                    Long.valueOf(c.getSubject()),
                    empresa == null ? null : empresa.longValue(),
                    c.get("email", String.class),
                    Rol.valueOf(c.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
