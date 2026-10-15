package com.compunex.b2b.modules.catalogo.dto.response;

import com.compunex.b2b.modules.catalogo.entity.ImagenProducto;
import com.compunex.b2b.modules.catalogo.entity.Producto;
import com.compunex.b2b.modules.catalogo.entity.ProductoEspecificacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ProductoDetalleResponseDTO(
        Long id,
        UUID uuid,
        Long proveedorId,
        String proveedorRazonSocial,
        String categoriaId,
        String titulo,
        String descripcion,
        String modeloComercial,
        String tipoFormato,
        String unidadesPorPaquete,
        String pedidoMinimo,
        BigDecimal precioUnitarioRef,
        BigDecimal precioTotalRef,
        String moneda,
        String terminosComerciales,
        String estado,
        String motivoModeracion,
        Boolean esRecomendado,
        Boolean esPromocionado,
        Boolean tieneOferta,
        List<EspecificacionDTO> especificaciones,
        List<ImagenDTO> imagenes,
        Instant fechaPublicacion,
        Instant fechaActualizacion
) {

    public record EspecificacionDTO(String clave, String valor) {
        public static EspecificacionDTO from(ProductoEspecificacion e) {
            return new EspecificacionDTO(e.getClave(), e.getValor());
        }
    }

    public record ImagenDTO(String urlImagen, Short orden) {
        public static ImagenDTO from(ImagenProducto i) {
            return new ImagenDTO(i.getUrlImagen(), i.getOrden());
        }
    }

    public static ProductoDetalleResponseDTO from(Producto producto) {
        Long proveedorId = producto.getProveedor() != null ? producto.getProveedor().getId() : null;
        String proveedorRazonSocial = producto.getProveedor() != null
                ? producto.getProveedor().getRazonSocial()
                : null;
        String categoriaId = producto.getCategoria() != null ? producto.getCategoria().getId() : null;

        List<ProductoEspecificacion> specsEntidad = producto.getEspecificaciones();
        List<EspecificacionDTO> specsDto = (specsEntidad == null || specsEntidad.isEmpty())
                ? Collections.emptyList()
                : specsEntidad.stream().map(EspecificacionDTO::from).toList();

        List<ImagenProducto> imagenesEntidad = producto.getImagenes();
        List<ImagenDTO> imagenesDto = (imagenesEntidad == null || imagenesEntidad.isEmpty())
                ? Collections.emptyList()
                : imagenesEntidad.stream()
                    .sorted(Comparator.comparing(ImagenProducto::getOrden))
                    .map(ImagenDTO::from)
                    .toList();

        return new ProductoDetalleResponseDTO(
                producto.getId(),
                producto.getUuid(),
                proveedorId,
                proveedorRazonSocial,
                categoriaId,
                producto.getTitulo(),
                producto.getDescripcion(),
                producto.getModeloComercial() != null ? producto.getModeloComercial().name() : null,
                producto.getTipoFormato(),
                producto.getUnidadesPorPaquete(),
                producto.getPedidoMinimo(),
                producto.getPrecioUnitarioRef(),
                producto.getPrecioTotalRef(),
                producto.getMoneda(),
                producto.getTerminosComerciales(),
                producto.getEstado() != null ? producto.getEstado().name() : null,
                producto.getMotivoModeracion(),
                producto.getEsRecomendado(),
                producto.getEsPromocionado(),
                producto.getTieneOferta(),
                specsDto,
                imagenesDto,
                producto.getFechaPublicacion(),
                producto.getFechaActualizacion()
        );
    }
}
