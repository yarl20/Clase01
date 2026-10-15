package com.compunex.b2b.modules.catalogo.controller;

import com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "01. Catálogo y Categorías", description = "Endpoints públicos para listado y detalle de categorías y subcategorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(
            summary = "[1.1] Listar Categorías",
            description = "Retorna el listado público de categorías activas con total de productos asociados."
    )
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[1.2] Detalle de Categoría y Subcategorías Técnicas",
            description = "Retorna el detalle de una categoría específica y sus subcategorías técnicas asociadas."
    )
    public ResponseEntity<CategoriaDetalleResponseDTO> obtenerDetalleCategoria(@PathVariable String id) {
        return ResponseEntity.ok(categoriaService.obtenerDetalleCategoria(id));
    }
}
