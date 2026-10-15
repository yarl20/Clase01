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

public record ProductoResumenResponseDTO(
        Long id,
        UUID uuid,
        String titulo,
        String descripcion,
        String categoriaId,
        String categoriaNombre,
        String modeloComercial,
        String tipoFormato,
        String unidadesPorPaquete,
        String pedidoMinimo,
        BigDecimal precioUnitarioRef,
        BigDecimal precioTotalRef,
        String moneda,
        String terminosComerciales,
        String estado,
        Boolean esRecomendado,
        Boolean esPromocionado,
        Boolean tieneOferta,
        String urlImagenPrincipal,
        List<ImagenDTO> imagenes,
        List<EspecificacionDTO> especificaciones,
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

    public static ProductoResumenResponseDTO from(Producto producto) {
        String categoriaId = null;
        String categoriaNombre = null;
        if (producto.getCategoria() != null) {
            categoriaId = producto.getCategoria().getId();
            categoriaNombre = producto.getCategoria().getNombre();
        }

        List<ImagenProducto> imagenesEntidad = producto.getImagenes();
        List<ImagenDTO> imagenesDto;
        String urlImagenPrincipal = null;
        if (imagenesEntidad != null && !imagenesEntidad.isEmpty()) {
            imagenesDto = imagenesEntidad.stream()
                    .sorted(Comparator.comparing(ImagenProducto::getOrden))
                    .map(ImagenDTO::from)
                    .toList();
            urlImagenPrincipal = imagenesEntidad.stream()
                    .min(Comparator.comparing(ImagenProducto::getOrden))
                    .map(ImagenProducto::getUrlImagen)
                    .orElse(null);
        } else {
            imagenesDto = Collections.emptyList();
        }

        List<ProductoEspecificacion> specsEntidad = producto.getEspecificaciones();
        List<EspecificacionDTO> specsDto;
        if (specsEntidad != null && !specsEntidad.isEmpty()) {
            specsDto = specsEntidad.stream()
                    .map(EspecificacionDTO::from)
                    .toList();
        } else {
            specsDto = Collections.emptyList();
        }

        return new ProductoResumenResponseDTO(
                producto.getId(),
                producto.getUuid(),
                producto.getTitulo(),
                producto.getDescripcion(),
                categoriaId,
                categoriaNombre,
                producto.getModeloComercial() != null ? producto.getModeloComercial().name() : null,
                producto.getTipoFormato(),
                producto.getUnidadesPorPaquete(),
                producto.getPedidoMinimo(),
                producto.getPrecioUnitarioRef(),
                producto.getPrecioTotalRef(),
                producto.getMoneda(),
                producto.getTerminosComerciales(),
                producto.getEstado() != null ? producto.getEstado().name() : null,
                producto.getEsRecomendado(),
                producto.getEsPromocionado(),
                producto.getTieneOferta(),
                urlImagenPrincipal,
                imagenesDto,
                specsDto,
                producto.getFechaPublicacion(),
                producto.getFechaActualizacion()
        );
    }
}
