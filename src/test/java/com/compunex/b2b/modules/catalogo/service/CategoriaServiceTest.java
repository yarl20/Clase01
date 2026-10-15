package com.compunex.b2b.modules.catalogo.service;

import com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.entity.Categoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoCategoria;
import com.compunex.b2b.modules.catalogo.entity.Subcategoria;
import com.compunex.b2b.modules.catalogo.repository.CategoriaRepository;
import com.compunex.b2b.modules.catalogo.service.impl.CategoriaServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @Test
    @DisplayName("[1.1] listarCategorias debe retornar la lista de categorías mapeadas a DTO")
    void debeListarCategoriasCorrectamente() {
        // Arrange
        Categoria catRam = new Categoria("ram", "Memorias RAM", "memory", "Módulos DDR4 y DDR5", EstadoCategoria.ACTIVA, 45);
        Categoria catSsd = new Categoria("ssd", "Almacenamiento SSD", "storage", "Discos M.2 y SATA", EstadoCategoria.ACTIVA, 38);
        when(categoriaRepository.findAll()).thenReturn(List.of(catRam, catSsd));

        // Act
        List<CategoriaResponseDTO> resultado = categoriaService.listarCategorias();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("ram", resultado.get(0).id());
        assertEquals("Memorias RAM", resultado.get(0).nombre());
        assertEquals("memory", resultado.get(0).icono());
        assertEquals(45, resultado.get(0).totalProductos());

        assertEquals("ssd", resultado.get(1).id());
        assertEquals("Almacenamiento SSD", resultado.get(1).nombre());
        assertEquals(38, resultado.get(1).totalProductos());

        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("[1.2] obtenerDetalleCategoria debe retornar el detalle con sus subcategorías")
    void debeObtenerDetalleCategoriaConSubcategorias() {
        // Arrange
        Categoria catRam = new Categoria("ram", "Memorias RAM", "memory", "Módulos DDR4 y DDR5", EstadoCategoria.ACTIVA, 45);
        Subcategoria sub1 = new Subcategoria(catRam, "DDR5 Desktop");
        sub1.setId(1L);
        Subcategoria sub2 = new Subcategoria(catRam, "DDR4 Desktop");
        sub2.setId(2L);
        catRam.getSubcategorias().addAll(List.of(sub1, sub2));

        when(categoriaRepository.findById("ram")).thenReturn(Optional.of(catRam));

        // Act
        CategoriaDetalleResponseDTO detalle = categoriaService.obtenerDetalleCategoria("ram");

        // Assert
        assertNotNull(detalle);
        assertEquals("ram", detalle.id());
        assertEquals("Memorias RAM", detalle.nombre());
        assertEquals("memory", detalle.icono());
        assertEquals(45, detalle.totalProductos());
        assertEquals(2, detalle.subcategorias().size());
        assertEquals(1L, detalle.subcategorias().get(0).id());
        assertEquals("DDR5 Desktop", detalle.subcategorias().get(0).nombre());
        assertEquals(2L, detalle.subcategorias().get(1).id());
        assertEquals("DDR4 Desktop", detalle.subcategorias().get(1).nombre());

        verify(categoriaRepository, times(1)).findById("ram");
    }

    @Test
    @DisplayName("[1.2] obtenerDetalleCategoria debe lanzar EntityNotFoundException si la categoría no existe")
    void debeLanzarExcepcionCuandoCategoriaNoExiste() {
        // Arrange
        when(categoriaRepository.findById("inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, ()
                -> categoriaService.obtenerDetalleCategoria("inexistente")
        );
        assertTrue(ex.getMessage().contains("inexistente"));
        verify(categoriaRepository, times(1)).findById("inexistente");
    }
}
