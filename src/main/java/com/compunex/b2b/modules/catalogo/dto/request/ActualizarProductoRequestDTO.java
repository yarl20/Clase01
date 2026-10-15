package com.compunex.b2b.modules.catalogo.dto.request;

import com.compunex.b2b.modules.catalogo.entity.ModeloComercial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ActualizarProductoRequestDTO(
    String categoriaId,

    @Size(max = 500, message = "El título no puede superar los 500 caracteres")
    String titulo,

    String descripcion,

    ModeloComercial modeloComercial,

    String tipoFormato,

    String unidadesPorPaquete,

    String pedidoMinimo,

    @Positive(message = "El precio unitario de referencia debe ser mayor a 0")
    BigDecimal precioUnitarioRef,

    @Positive(message = "El precio total de referencia debe ser mayor a 0")
    BigDecimal precioTotalRef,

    String moneda,

    String terminosComerciales,

    @Valid
    List<CrearProductoRequestDTO.EspecificacionItemDTO> especificaciones,

    @Valid
    List<CrearProductoRequestDTO.ImagenItemDTO> imagenes
) {}
