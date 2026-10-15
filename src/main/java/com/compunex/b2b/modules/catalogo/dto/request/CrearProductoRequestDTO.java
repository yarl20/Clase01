package com.compunex.b2b.modules.catalogo.dto.request;

import com.compunex.b2b.modules.catalogo.entity.ModeloComercial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CrearProductoRequestDTO(
    @NotBlank(message = "El ID de categoría es obligatorio")
    String categoriaId,

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 500, message = "El título no puede superar los 500 caracteres")
    String titulo,

    @NotBlank(message = "La descripción es obligatoria")
    String descripcion,

    @NotNull(message = "El modelo comercial es obligatorio")
    ModeloComercial modeloComercial,

    @NotBlank(message = "El tipo de formato es obligatorio")
    String tipoFormato,

    @NotBlank(message = "Las unidades por paquete son obligatorias")
    String unidadesPorPaquete,

    @NotBlank(message = "El pedido mínimo es obligatorio")
    String pedidoMinimo,

    @NotNull(message = "El precio unitario de referencia es obligatorio")
    @Positive(message = "El precio unitario de referencia debe ser mayor a 0")
    BigDecimal precioUnitarioRef,

    @Positive(message = "El precio total de referencia debe ser mayor a 0")
    BigDecimal precioTotalRef,

    String moneda,

    String terminosComerciales,

    @Valid
    List<EspecificacionItemDTO> especificaciones,

    @NotEmpty(message = "Debe enviar al menos una imagen")
    @Valid
    List<ImagenItemDTO> imagenes
) {
    public record EspecificacionItemDTO(
        @NotBlank(message = "La clave de la especificación es obligatoria")
        String clave,

        @NotBlank(message = "El valor de la especificación es obligatorio")
        String valor
    ) {}

    public record ImagenItemDTO(
        @NotBlank(message = "La URL de la imagen es obligatoria")
        String urlImagen,

        Short orden
    ) {}
}
