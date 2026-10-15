package com.compunex.b2b.modules.catalogo.service;

import org.springframework.data.domain.Pageable;

import com.compunex.b2b.modules.catalogo.dto.request.ActualizarProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CrearProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO;

public interface ProductoService {

    ProductoPaginadoResponseDTO listarMisProductos(String correoAutenticado, String search, Pageable pageable);

    default ProductoPaginadoResponseDTO listarMisProductos(String correoAutenticado, Pageable pageable) {
        return listarMisProductos(correoAutenticado, null, pageable);
    }

    ProductoDetalleResponseDTO obtenerFichaTecnica(String correoAutenticado, Long productoId);

    CrearProductoResponseDTO crearProducto(String correoAutenticado, CrearProductoRequestDTO request);

    ActualizarProductoResponseDTO actualizarProducto(String correoAutenticado, Long productoId, ActualizarProductoRequestDTO request);

    CambioEstadoResponseDTO cambiarEstadoPublicacion(String correoAutenticado, Long productoId,
            CambiarEstadoProductoDTO request);

    void eliminarMiProducto(String correoAutenticado, Long productoId);

    // Métodos para rol ADMIN
    ProductoPaginadoResponseDTO listarProductosAdmin(String search, Pageable pageable);

    CambioEstadoResponseDTO cambiarEstadoPublicacionAdmin(Long productoId, CambiarEstadoProductoDTO request);
}
