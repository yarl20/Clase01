package com.compunex.b2b.modules.catalogo.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compunex.b2b.modules.catalogo.dto.request.ActualizarProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CrearProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO;
import com.compunex.b2b.modules.catalogo.service.ProductoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/providers/me/products", "/api/v1/proveedores/yo/productos", "/api/proveedores/yo/productos"})
@Tag(name = "09. API de Productos - Proveedor", description = "Endpoints de gestión de productos propios del proveedor (CRUD)")
@SecurityRequirement(name = "bearerAuth")
public class ProductoProveedorController {

    private final ProductoService productoService;

    public ProductoProveedorController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<ProductoPaginadoResponseDTO> listarMisProductos(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String correoAutenticado = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        ProductoPaginadoResponseDTO response = productoService.listarMisProductos(correoAutenticado, search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDetalleResponseDTO> obtenerFichaTecnica(
            Authentication authentication,
            @PathVariable Long id) {

        ProductoDetalleResponseDTO response = productoService.obtenerFichaTecnica(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CrearProductoResponseDTO> crearProducto(
            Authentication authentication,
            @Valid @RequestBody CrearProductoRequestDTO request) {

        CrearProductoResponseDTO response = productoService.crearProducto(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActualizarProductoResponseDTO> actualizarProducto(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProductoRequestDTO request) {

        ActualizarProductoResponseDTO response = productoService.actualizarProducto(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CambioEstadoResponseDTO> cambiarEstadoPublicacion(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoProductoDTO request) {

        CambioEstadoResponseDTO response = productoService.cambiarEstadoPublicacion(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMiProducto(Authentication authentication,
            @PathVariable Long id) {
        productoService.eliminarMiProducto(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
