package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.application.port.in.ProductoUseCase;
import com.indux.induxbackend.domain.exception.SinPermisoException;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccesoPorEmpresa {

    private final ProductoUseCase productos;

    public void verificarEmpresa(Authentication autenticacion, Long empresaId) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        if (usuario.rol() != Rol.ADMIN && !Objects.equals(usuario.empresaId(), empresaId)) {
            throw new SinPermisoException("No tienes acceso a los datos de esta empresa");
        }
    }

    /** Carga el producto (404 si no existe) y verifica que sea de la empresa del usuario. */
    public Producto producto(Authentication autenticacion, Long productoId) {
        Producto producto = productos.obtener(productoId);
        verificarEmpresa(autenticacion, producto.empresaId());
        return producto;
    }
}
