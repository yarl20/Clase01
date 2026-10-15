package com.compunex.b2b.modules.catalogo.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.compunex.b2b.modules.catalogo.entity.Producto;

public record ActualizarProductoResponseDTO(
        Long id,
        UUID uuid,
        String titulo,
        BigDecimal precioUnitarioRef,
        BigDecimal precioTotalRef,
        String estado,
        Instant fechaActualizacion
        ) {

    public static ActualizarProductoResponseDTO from(Producto producto) {
        String estado = producto.getEstado() != null ? producto.getEstado().name() : null;

        return new ActualizarProductoResponseDTO(
                producto.getId(),
                producto.getUuid(),
                producto.getTitulo(),
                producto.getPrecioUnitarioRef(),
                producto.getPrecioTotalRef(),
                estado,
                producto.getFechaActualizacion()
        );
    }
}
