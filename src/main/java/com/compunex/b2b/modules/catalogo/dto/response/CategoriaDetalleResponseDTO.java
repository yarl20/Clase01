package com.compunex.b2b.modules.catalogo.dto.response;

import java.util.List;

public record CategoriaDetalleResponseDTO(
        String id,
        String nombre,
        String icono,
        String descripcion,
        String estado,
        Integer totalProductos,
        List<SubcategoriaResponseDTO> subcategorias
        ) {

}
