package com.compunex.b2b.modules.catalogo.controller;

import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO;
import com.compunex.b2b.modules.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "08. API de Productos - Administrador", description = "Endpoints de administración global de productos (Requiere rol ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductoController {

    private final ProductoService productoService;

    public AdminProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(
            summary = "[Admin A] Consultar productos de todos los proveedores",
            description = "Permite al administrador consultar productos de cualquier proveedor con paginación y búsqueda opcional por nombre."
    )
    public ResponseEntity<ProductoPaginadoResponseDTO> listarProductos(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        ProductoPaginadoResponseDTO response = productoService.listarProductosAdmin(search, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "[Admin B] Cambiar estado de publicación de cualquier producto",
            description = "Permite al administrador activar o desactivar cualquier producto sin limitación de propiedad."
    )
    public ResponseEntity<CambioEstadoResponseDTO> cambiarEstadoPublicacion(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoProductoDTO request) {

        CambioEstadoResponseDTO response = productoService.cambiarEstadoPublicacionAdmin(id, request);
        return ResponseEntity.ok(response);
    }
}
