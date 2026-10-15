package com.compunex.b2b.modules.catalogo.service.impl;

import com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.SubcategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.entity.Categoria;
import com.compunex.b2b.modules.catalogo.repository.CategoriaRepository;
import com.compunex.b2b.modules.catalogo.service.CategoriaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDetalleResponseDTO obtenerDetalleCategoria(String id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con id: " + id));

        return mapToDetalleResponseDTO(categoria);
    }

    private CategoriaResponseDTO mapToResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getIcono(),
                categoria.getDescripcion(),
                categoria.getEstado().name(),
                categoria.getTotalProductos() != null ? categoria.getTotalProductos() : 0
        );
    }

    private CategoriaDetalleResponseDTO mapToDetalleResponseDTO(Categoria categoria) {
        List<SubcategoriaResponseDTO> subcategorias = categoria.getSubcategorias().stream()
                .map(s -> new SubcategoriaResponseDTO(s.getId(), s.getNombre()))
                .toList();

        return new CategoriaDetalleResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getIcono(),
                categoria.getDescripcion(),
                categoria.getEstado().name(),
                categoria.getTotalProductos() != null ? categoria.getTotalProductos() : 0,
                subcategorias
        );
    }
}
