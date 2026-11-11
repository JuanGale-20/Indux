package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class VerificadorUsuarioActivo {

    private final UsuarioRepositoryPort usuarios;
    private final EmpresaRepositoryPort empresas;

    private static final long TTL_MILLIS = 15_000L; // ~15 s

    private record CacheEntrada(boolean activo, long expiraEn) {}

    private final Map<Long, CacheEntrada> cache = new ConcurrentHashMap<>();

    public boolean estaActivo(Long usuarioId) {
        if (usuarioId == null) {
            return false;
        }

        long ahora = System.currentTimeMillis();
        CacheEntrada entrada = cache.get(usuarioId);
        if (entrada != null && entrada.expiraEn > ahora) {
            return entrada.activo;
        }

        boolean activo = consultarEnBase(usuarioId);
        cache.put(usuarioId, new CacheEntrada(activo, ahora + TTL_MILLIS));
        return activo;
    }

    private boolean consultarEnBase(Long usuarioId) {
        Optional<Usuario> uOpt = usuarios.buscarPorId(usuarioId);
        if (uOpt.isEmpty() || !uOpt.get().activo()) {
            return false;
        }

        Usuario usuario = uOpt.get();
        if (usuario.empresaId() != null) {
            Optional<Empresa> empOpt = empresas.buscarPorId(usuario.empresaId());
            if (empOpt.isEmpty() || !empOpt.get().activa()) {
                return false;
            }
        }

        return true;
    }

    public void invalidar(Long usuarioId) {
        if (usuarioId != null) {
            cache.remove(usuarioId);
        }
    }

    public void limpiarCache() {
        cache.clear();
    }
}
