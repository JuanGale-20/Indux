package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ProductoUseCase;
import com.indux.induxbackend.application.port.in.ProductoUseCase.ActualizarProductoComando;
import com.indux.induxbackend.application.port.in.ProductoUseCase.CrearProductoComando;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ActualizarProductoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.CrearProductoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ProductoResponse;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Productos", description = "Gestión de productos y catálogo")
public class ProductoController {

    private final ProductoUseCase productos;
    private final AccesoPorEmpresa acceso;

    @Operation(summary = "Crear un nuevo producto (ADMIN)")
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest r) {
        Producto creado = productos.crear(
                new CrearProductoComando(r.empresaId(), r.codigo(), r.nombre(), r.unidadMedida()));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id()))
                .body(ProductoResponse.desde(creado));
    }

    @Operation(summary = "Obtener un producto por ID")
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        return ProductoResponse.desde(acceso.producto(autenticacion, id));
    }

    @Operation(summary = "Listar productos de una empresa")
    @GetMapping
    public List<ProductoResponse> listarPorEmpresa(@RequestParam Long empresaId,
                                                   Authentication autenticacion) {
        acceso.verificarEmpresa(autenticacion, empresaId);
        return productos.listarPorEmpresa(empresaId).stream().map(ProductoResponse::desde).toList();
    }

    @Operation(summary = "Actualizar datos de un producto (ADMIN)")
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ActualizarProductoRequest r) {
        return ProductoResponse.desde(productos.actualizar(id,
                new ActualizarProductoComando(r.codigo(), r.nombre(), r.unidadMedida())));
    }

    @Operation(summary = "Eliminar un producto (ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
