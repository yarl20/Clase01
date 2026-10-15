package com.compunex.b2b.modules.catalogo.controller;

import com.compunex.b2b.config.SecurityConfig;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.SubcategoriaResponseDTO;
import com.compunex.b2b.modules.catalogo.service.CategoriaService;
import com.compunex.b2b.security.JwtAuthenticationFilter;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CategoriaController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    @DisplayName("[1.1] GET /api/v1/categories - Debe retornar HTTP 200 y la lista de categorías según contrato")
    void debeRetornar200YListaCategorias() throws Exception {
        // Arrange
        var categorias = List.of(
                new CategoriaResponseDTO("ram", "Memorias RAM", "memory", "Módulos de memoria DDR4 y DDR5 para PC y Servidores", "ACTIVA", 45),
                new CategoriaResponseDTO("ssd", "Almacenamiento SSD", "storage", "Unidades de estado sólido NVMe M.2 y SATA III", "ACTIVA", 38)
        );
        when(categoriaService.listarCategorias()).thenReturn(categorias);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("ram"))
                .andExpect(jsonPath("$[0].nombre").value("Memorias RAM"))
                .andExpect(jsonPath("$[0].icono").value("memory"))
                .andExpect(jsonPath("$[0].descripcion").value("Módulos de memoria DDR4 y DDR5 para PC y Servidores"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$[0].totalProductos").value(45))
                .andExpect(jsonPath("$[1].id").value("ssd"))
                .andExpect(jsonPath("$[1].nombre").value("Almacenamiento SSD"))
                .andExpect(jsonPath("$[1].totalProductos").value(38));
    }

    @Test
    @DisplayName("[1.2] GET /api/v1/categories/{id} - Debe retornar HTTP 200 y el detalle con subcategorías técnicas")
    void debeRetornar200YDetalleCategoriaConSubcategorias() throws Exception {
        // Arrange
        var subcategorias = List.of(
                new SubcategoriaResponseDTO(1L, "DDR5 Desktop"),
                new SubcategoriaResponseDTO(2L, "DDR4 Desktop"),
                new SubcategoriaResponseDTO(3L, "DDR5 SO-DIMM Laptop")
        );
        var detalle = new CategoriaDetalleResponseDTO(
                "ram", "Memorias RAM", "memory",
                "Módulos de memoria DDR4 y DDR5 para PC y Servidores",
                "ACTIVA", 45, subcategorias
        );
        when(categoriaService.obtenerDetalleCategoria("ram")).thenReturn(detalle);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/ram")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("ram"))
                .andExpect(jsonPath("$.nombre").value("Memorias RAM"))
                .andExpect(jsonPath("$.icono").value("memory"))
                .andExpect(jsonPath("$.descripcion").value("Módulos de memoria DDR4 y DDR5 para PC y Servidores"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.totalProductos").value(45))
                .andExpect(jsonPath("$.subcategorias.length()").value(3))
                .andExpect(jsonPath("$.subcategorias[0].id").value(1))
                .andExpect(jsonPath("$.subcategorias[0].nombre").value("DDR5 Desktop"))
                .andExpect(jsonPath("$.subcategorias[1].id").value(2))
                .andExpect(jsonPath("$.subcategorias[1].nombre").value("DDR4 Desktop"))
                .andExpect(jsonPath("$.subcategorias[2].id").value(3))
                .andExpect(jsonPath("$.subcategorias[2].nombre").value("DDR5 SO-DIMM Laptop"));
    }

    @Test
    @DisplayName("[1.2] GET /api/v1/categories/{id} - Debe retornar HTTP 404 cuando la categoría no existe")
    void debeRetornar404CuandoCategoriaNoExiste() throws Exception {
        // Arrange
        when(categoriaService.obtenerDetalleCategoria("no-existe"))
                .thenThrow(new EntityNotFoundException("Categoría no encontrada con id: no-existe"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/no-existe")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Categoría no encontrada con id: no-existe"));
    }
}

