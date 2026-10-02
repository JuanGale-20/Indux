package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.infrastructure.adapter.out.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            jwt.leer(cabecera.substring(7)).ifPresent(usuario -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}