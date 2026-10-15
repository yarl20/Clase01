package com.compunex.b2b.modules.catalogo.dto.response;

import com.compunex.b2b.modules.catalogo.entity.Producto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CrearProductoResponseDTO(
        Long id,
        UUID uuid,
        Long proveedorId,
        String categoriaId,
        String titulo,
        BigDecimal precioUnitarioRef,
        BigDecimal precioTotalRef,
        String moneda,
        String estado,
        Instant fechaPublicacion
) {
    public static CrearProductoResponseDTO from(Producto producto) {
        Long proveedorId = producto.getProveedor() != null ? producto.getProveedor().getId() : null;
        String categoriaId = producto.getCategoria() != null ? producto.getCategoria().getId() : null;
        String estado = producto.getEstado() != null ? producto.getEstado().name() : null;

        return new CrearProductoResponseDTO(
                producto.getId(),
                producto.getUuid(),
                proveedorId,
                categoriaId,
                producto.getTitulo(),
                producto.getPrecioUnitarioRef(),
                producto.getPrecioTotalRef(),
                producto.getMoneda(),
                estado,
                producto.getFechaPublicacion()
        );
    }
}
