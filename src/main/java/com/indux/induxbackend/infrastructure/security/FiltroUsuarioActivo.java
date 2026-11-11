package com.indux.induxbackend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FiltroUsuarioActivo extends OncePerRequestFilter {

    private final VerificadorUsuarioActivo verificador;

    public FiltroUsuarioActivo(VerificadorUsuarioActivo verificador) {
        this.verificador = verificador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            if (!verificador.estaActivo(usuario.id())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/problem+json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"title\":\"No autenticado\",\"status\":401,\"detail\":\"Tu usuario o tu empresa fue desactivado\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
