package com.compunex.b2b.modules.catalogo.service;

import com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarCategorias();

    CategoriaDetalleResponseDTO obtenerDetalleCategoria(String id);
}
