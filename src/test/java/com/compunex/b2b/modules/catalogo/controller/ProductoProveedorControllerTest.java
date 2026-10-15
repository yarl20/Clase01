package com.compunex.b2b.modules.catalogo.controller;

import com.compunex.b2b.config.SecurityConfig;
import com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductoProveedorController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class ProductoProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    @DisplayName("[2.6] Debe retornar HTTP 204 al eliminar producto propio")
    void debeRetornar204AlEliminarProductoPropio() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());
        doNothing().when(productoService).eliminarMiProducto("ventas.mayoristas@technova.com", 1L);

        mockMvc.perform(delete("/api/v1/providers/me/products/1").principal(auth))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminarMiProducto("ventas.mayoristas@technova.com", 1L);
    }

    @Test
    @DisplayName("[2.6] Debe retornar 404 cuando el producto no es del proveedor")
    void debeRetornar404CuandoProductoNoEsDelProveedor() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());
        doThrow(new jakarta.persistence.EntityNotFoundException("Producto no encontrado: 99"))
                .when(productoService).eliminarMiProducto("ventas.mayoristas@technova.com", 99L);

        mockMvc.perform(delete("/api/v1/providers/me/products/99").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[2.2] Debe retornar HTTP 200 con la ficha técnica completa")
    void debeRetornar200ConFichaTecnicaCompleta() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        var dto = new ProductoDetalleResponseDTO(
                1L, UUID.randomUUID(), 10L, "TechNova Mayorista S.A.C.", "ram",
                "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
                "Módulo de memoria DDR5 de alto rendimiento.",
                "DISTRIBUIDOR_OFICIAL", "Caja Master x10 Blísteres", "10 unidades",
                "1 caja (10 unidades)", new BigDecimal("52.50"), new BigDecimal("525.00"),
                "USD", "Garantía de 3 años directa con fabricante.", "ACTIVO", null,
                true, false, false,
                List.of(new ProductoDetalleResponseDTO.EspecificacionDTO("Capacidad", "16 GB")),
                List.of(new ProductoDetalleResponseDTO.ImagenDTO("https://img.compunex.com/ram.jpg", (short) 1)),
                Instant.now(), Instant.now()
        );

        when(productoService.obtenerFichaTecnica("ventas.mayoristas@technova.com", 1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/providers/me/products/1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz"))
                .andExpect(jsonPath("$.especificaciones[0].clave").value("Capacidad"))
                .andExpect(jsonPath("$.imagenes[0].urlImagen").value("https://img.compunex.com/ram.jpg"));
    }

    @Test
    @DisplayName("[2.2] Debe retornar 404 cuando el producto no existe")
    void debeRetornar404CuandoProductoNoExisteAlConsultarFichaTecnica() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());
        when(productoService.obtenerFichaTecnica("ventas.mayoristas@technova.com", 99L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Producto no encontrado: 99"));

        mockMvc.perform(get("/api/v1/providers/me/products/99").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[2.5] Debe retornar 200 al cambiar el estado de publicación")
    void debeRetornar200AlCambiarEstadoDePublicacion() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        var responseDto = new CambioEstadoResponseDTO(
                1L, UUID.randomUUID(), "ACTIVO", "DESHABILITADO_POR_PROVEEDOR",
                "El producto ha sido deshabilitado del catálogo mayorista temporalmente",
                Instant.now()
        );

        when(productoService.cambiarEstadoPublicacion(eq("ventas.mayoristas@technova.com"), eq(1L), any()))
                .thenReturn(responseDto);

        String body = "{\"estado\":\"DESHABILITADO_POR_PROVEEDOR\"}";

        mockMvc.perform(patch("/api/v1/providers/me/products/1/status")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuevoEstado").value("DESHABILITADO_POR_PROVEEDOR"));
    }

    @Test
    @DisplayName("[2.5] Debe retornar 404 cuando el producto no es del proveedor al cambiar estado")
    void debeRetornar404CuandoProductoNoEsDelProveedorAlCambiarEstado() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        when(productoService.cambiarEstadoPublicacion(eq("ventas.mayoristas@technova.com"), eq(99L), any()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Producto no encontrado: 99"));

        String body = "{\"estado\":\"ACTIVO\"}";

        mockMvc.perform(patch("/api/v1/providers/me/products/99/status")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[2.3] Debe retornar HTTP 201 y el contrato esperado al crear un producto")
    void debeCrearProductoRetornando201YContratoEsperado() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        UUID fixedUuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        Instant fixedDate = Instant.parse("2026-09-02T10:30:00Z");

        var responseDto = new CrearProductoResponseDTO(
                1L,
                fixedUuid,
                1L,
                "ram",
                "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
                new BigDecimal("52.50"),
                new BigDecimal("525.00"),
                "USD",
                "ACTIVO",
                fixedDate
        );

        when(productoService.crearProducto(eq("ventas.mayoristas@technova.com"), any()))
                .thenReturn(responseDto);

        String jsonEntrada = """
                {
                  "categoriaId": "ram",
                  "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
                  "descripcion": "Módulo de memoria DDR5 de alto rendimiento...",
                  "modeloComercial": "DISTRIBUIDOR_OFICIAL",
                  "tipoFormato": "Caja Master x10 Blísteres",
                  "unidadesPorPaquete": "10 unidades",
                  "pedidoMinimo": "1 caja (10 unidades)",
                  "precioUnitarioRef": 52.50,
                  "precioTotalRef": 525.00,
                  "moneda": "USD",
                  "terminosComerciales": "Garantía de 3 años directa con fabricante.",
                  "especificaciones": [
                    { "clave": "Capacidad", "valor": "16 GB" },
                    { "clave": "Frecuencia", "valor": "5600 MHz" },
                    { "clave": "Latencia CAS", "valor": "CL36" },
                    { "clave": "Voltaje", "valor": "1.25V" }
                  ],
                  "imagenes": [
                    { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-front.jpg", "orden": 1 },
                    { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-angle.jpg", "orden": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/providers/me/products")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntrada))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.uuid").value("7c9e6679-7425-40de-944b-e07fc1f90ae7"))
                .andExpect(jsonPath("$.proveedorId").value(1))
                .andExpect(jsonPath("$.categoriaId").value("ram"))
                .andExpect(jsonPath("$.titulo").value("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz"))
                .andExpect(jsonPath("$.precioUnitarioRef").value(52.50))
                .andExpect(jsonPath("$.precioTotalRef").value(525.00))
                .andExpect(jsonPath("$.moneda").value("USD"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.fechaPublicacion").value("2026-09-02T10:30:00Z"))
                .andExpect(jsonPath("$.descripcion").doesNotExist())
                .andExpect(jsonPath("$.especificaciones").doesNotExist())
                .andExpect(jsonPath("$.imagenes").doesNotExist());
    }

    @Test
    @DisplayName("[2.4] Debe retornar HTTP 200 y el contrato esperado al actualizar un producto")
    void debeActualizarProductoRetornando200YContratoEsperado() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        UUID fixedUuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        Instant fixedDate = Instant.parse("2026-09-02T11:00:00Z");

        var responseDto = new ActualizarProductoResponseDTO(
                1L,
                fixedUuid,
                "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)",
                new BigDecimal("49.90"),
                new BigDecimal("249.50"),
                "ACTIVO",
                fixedDate
        );

        when(productoService.actualizarProducto(eq("ventas.mayoristas@technova.com"), eq(1L), any()))
                .thenReturn(responseDto);

        String jsonEntrada = """
                {
                  "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)",
                  "descripcion": "Descripción actualizada con nuevo stock certificado y empaque sellado.",
                  "pedidoMinimo": "5 unidades",
                  "precioUnitarioRef": 49.90,
                  "precioTotalRef": 249.50,
                  "terminosComerciales": "Precio rebajado por compra directa a partir de 5 piezas.",
                  "especificaciones": [
                    { "clave": "Capacidad", "valor": "16 GB" },
                    { "clave": "Frecuencia", "valor": "5600 MHz" },
                    { "clave": "Disipador", "valor": "Aluminio Negro Anodizado" }
                  ],
                  "imagenes": [
                    { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-new.jpg", "orden": 1 }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/providers/me/products/1")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntrada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.uuid").value("7c9e6679-7425-40de-944b-e07fc1f90ae7"))
                .andExpect(jsonPath("$.titulo").value("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)"))
                .andExpect(jsonPath("$.precioUnitarioRef").value(49.90))
                .andExpect(jsonPath("$.precioTotalRef").value(249.50))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.fechaActualizacion").value("2026-09-02T11:00:00Z"))
                .andExpect(jsonPath("$.descripcion").doesNotExist())
                .andExpect(jsonPath("$.especificaciones").doesNotExist())
                .andExpect(jsonPath("$.imagenes").doesNotExist())
                .andExpect(jsonPath("$.pedidoMinimo").doesNotExist());
    }

    @Test
    @DisplayName("[2.3] Debe retornar HTTP 400 Bad Request cuando el payload de creación tiene datos inválidos")
    void debeRechazarCreacionConDatosInvalidos() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        // Payload con título vacío, precio negativo y sin imágenes
        String jsonInvalido = """
                {
                  "categoriaId": "ram",
                  "titulo": "",
                  "descripcion": "Sin titulo",
                  "modeloComercial": "DISTRIBUIDOR_OFICIAL",
                  "tipoFormato": "Caja",
                  "unidadesPorPaquete": "10",
                  "pedidoMinimo": "1",
                  "precioUnitarioRef": -10.00,
                  "imagenes": []
                }
                """;

        mockMvc.perform(post("/api/v1/providers/me/products")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[2.1] Debe filtrar productos propios con parámetro search")
    void debeFiltrarProductosConParametroSearch() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "ventas.mayoristas@technova.com", null, List.of());

        var paginado = new com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO(
                List.of(), 0, 10, 0, 0, true);

        when(productoService.listarMisProductos(eq("ventas.mayoristas@technova.com"), eq("Kingston"), any()))
                .thenReturn(paginado);

        mockMvc.perform(get("/api/v1/providers/me/products?search=Kingston")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0));

        verify(productoService, times(1)).listarMisProductos(eq("ventas.mayoristas@technova.com"), eq("Kingston"), any());
    }
}
