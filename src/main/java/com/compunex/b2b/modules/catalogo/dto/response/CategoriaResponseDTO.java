package com.compunex.b2b.modules.catalogo.dto.response;

public record CategoriaResponseDTO(
        String id,
        String nombre,
        String icono,
        String descripcion,
        String estado,
        Integer totalProductos
        ) {

}
