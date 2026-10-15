package com.compunex.b2b.modules.catalogo.dto.request;

import com.compunex.b2b.modules.catalogo.entity.EstadoProducto;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoProductoDTO(

        @NotNull(message = "El estado es obligatorio")
        EstadoProducto estado
) {
}
