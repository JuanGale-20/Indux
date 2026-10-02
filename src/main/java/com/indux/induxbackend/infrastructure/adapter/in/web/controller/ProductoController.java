package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ProductoUseCase;
import com.indux.induxbackend.application.port.in.ProductoUseCase.CrearProductoComando;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.CrearProductoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ProductoResponse;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoUseCase productos;
    private final AccesoPorEmpresa acceso;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest r) {
        Producto creado = productos.crear(
                new CrearProductoComando(r.empresaId(), r.codigo(), r.nombre(), r.unidadMedida()));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id()))
                .body(ProductoResponse.desde(creado));
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        return ProductoResponse.desde(acceso.producto(autenticacion, id));
    }

    @GetMapping
    public List<ProductoResponse> listarPorEmpresa(@RequestParam Long empresaId,
                                                   Authentication autenticacion) {
        acceso.verificarEmpresa(autenticacion, empresaId);
        return productos.listarPorEmpresa(empresaId).stream().map(ProductoResponse::desde).toList();
    }
}