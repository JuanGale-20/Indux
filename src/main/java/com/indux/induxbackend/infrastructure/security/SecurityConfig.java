package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.infrastructure.adapter.out.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtService jwt,
                                @Value("${indux.cors.origen-permitido}") String origen) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // API sin sesiones ni cookies: el token viaja en la cabecera
                .cors(cors -> cors.configurationSource(corsConfig(origen)))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/empresas", "/api/productos", "/api/procesos", "/api/usuarios")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribir(res, 401, "No autenticado", "Debes iniciar sesión"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribir(res, 403, "Sin permiso", "Tu rol no permite esta acción")))
                .build();
    }

    private CorsConfigurationSource corsConfig(String origen) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origen));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }

    private static void escribir(HttpServletResponse res, int estado, String titulo, String detalle)
            throws IOException {
        res.setStatus(estado);
        res.setContentType("application/problem+json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"title\":\"" + titulo + "\",\"status\":" + estado
                + ",\"detail\":\"" + detalle + "\"}");
    }
}
