package com.compunex.b2b.modules.catalogo.controller;

import com.compunex.b2b.config.SecurityConfig;
import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO;
import com.compunex.b2b.modules.catalogo.service.ProductoService;
import com.compunex.b2b.security.JwtAuthenticationFilter;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminProductoController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    @DisplayName("[Admin A] GET /api/v1/admin/products debe retornar 200 con paginación")
    void debeListarProductosParaAdmin() throws Exception {
        var paginado = new ProductoPaginadoResponseDTO(List.of(), 0, 10, 0, 0, true);
        when(productoService.listarProductosAdmin(any(), any())).thenReturn(paginado);

        mockMvc.perform(get("/api/v1/admin/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamano").value(10));
    }

    @Test
    @DisplayName("[Admin B] PATCH /api/v1/admin/products/{id}/status debe retornar 200")
    void debeCambiarEstadoComoAdmin() throws Exception {
        var response = new CambioEstadoResponseDTO(
                1L, UUID.randomUUID(), "ACTIVO", "OCULTO_POR_ADMIN",
                "El producto ha sido ocultado por un administrador", Instant.now()
        );

        when(productoService.cambiarEstadoPublicacionAdmin(eq(1L), any(CambiarEstadoProductoDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/products/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"OCULTO_POR_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuevoEstado").value("OCULTO_POR_ADMIN"));
    }

    @Test
    @DisplayName("[Admin B] PATCH /api/v1/admin/products/{id}/status con body vacío debe retornar 400 Bad Request")
    void debeRechazarCambioEstadoConBodyInvalido() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/products/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
