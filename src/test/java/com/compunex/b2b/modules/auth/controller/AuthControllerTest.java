package com.compunex.b2b.modules.auth.controller;

import com.compunex.b2b.config.SecurityConfig;
import com.compunex.b2b.modules.auth.dto.request.LoginRequestDTO;
import com.compunex.b2b.modules.auth.dto.response.AuthResponseDTO;
import com.compunex.b2b.modules.auth.service.AuthService;
import com.compunex.b2b.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * =========================================================================
 * 🔵 TDD FASE 3: REFACTOR
 * =========================================================================
 * En la Fase 3, se verifica la capa web del controlador con @WebMvcTest y MockMvc:
 * - Validación del contrato JSON de entrada (LoginRequestDTO)
 * - Verificación de código de estado HTTP 200 OK
 * - Formato del contrato JSON de salida (AuthResponseDTO: token, tipoToken, expiraEnSegundos)
 * - Manejo de validación de campos obligatorios (HTTP 400 Bad Request)
 * =========================================================================
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("🔵 FASE 3 REFACTOR: Debe retornar HTTP 200 OK y AuthResponseDTO cuando el login es exitoso")
    void debeRetornarHttp200YTokenCuandoLoginEsExitoso() throws Exception {
        // Arrange (Preparación del payload y simulación del servicio)
        LoginRequestDTO request = new LoginRequestDTO(
            "ventas.mayoristas@technova.com",
            "PasswordMayorista123!"
        );

        AuthResponseDTO responseSimulado = new AuthResponseDTO(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2ZW50YXMubWF5b3Jpc3Rhc0B0ZWNobm92YS5jb20iLCJyb2wiOiJQUk9WRUVET1IiLCJpYXQiOjE3MjUyODAwMDAsImV4cCI6MTcyNTM2NjQwMH0.simulatedTokenPayload",
            "Bearer",
            86400L
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(responseSimulado);

        // Act & Assert (Llamada HTTP POST y validación del contrato JSON)
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(responseSimulado.token()))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(86400));
    }

    @Test
    @DisplayName("🔵 FASE 3 REFACTOR: Debe rechazar con HTTP 400 cuando faltan campos obligatorios")
    void debeRetornarHttp400CuandoPayloadEsInvalido() throws Exception {
        // Arrange: Correo con formato inválido y contraseña vacía
        LoginRequestDTO requestInvalido = new LoginRequestDTO("correo-invalido", "");

        // Act & Assert (Verificación de validación Jakarta Bean Validation)
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[0.2] Debe retornar HTTP 200 y UsuarioSesionDTO en GET /me")
    void debeRetornarSesionEnGetMe() throws Exception {
        var sesion = new com.compunex.b2b.modules.auth.dto.response.UsuarioSesionDTO(
                "ventas.mayoristas@technova.com", "TechNova Mayorista S.A.C.", "PROVEEDOR", true);
        when(authService.obtenerSesion()).thenReturn(sesion);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ventas.mayoristas@technova.com"))
                .andExpect(jsonPath("$.nombre").value("TechNova Mayorista S.A.C."))
                .andExpect(jsonPath("$.rol").value("PROVEEDOR"))
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    @DisplayName("[0.3] Debe retornar HTTP 200 en PATCH /password")
    void debeRetornarHttp200EnPatchPassword() throws Exception {
        var request = new com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO(
                "PasswordMayorista123!", "NuevaClaveCorporativa2026*");
        var response = new com.compunex.b2b.modules.auth.dto.response.CambioPasswordResponseDTO(
                "Contraseña actualizada exitosamente", java.time.Instant.parse("2026-09-02T16:00:00Z"));
        when(authService.cambiarPassword(any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada exitosamente"));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la sesión no encuentra al usuario")
    void debeRetornar404CuandoSesionNoEncuentraUsuario() throws Exception {
        when(authService.obtenerSesion())
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la contraseña actual es incorrecta")
    void debeRetornar400CuandoClaveActualEsIncorrecta() throws Exception {
        var request = new com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO(
                "clave-mala", "NuevaClaveCorporativa2026*");
        when(authService.cambiarPassword(any()))
                .thenThrow(new com.compunex.b2b.modules.auth.exception.ContrasenaActualIncorrectaException(
                        "La contraseña actual es incorrecta"));

        mockMvc.perform(patch("/api/v1/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[0.4] Debe retornar HTTP 201 Created cuando el registro de proveedor es exitoso")
    void debeRetornarHttp201CuandoRegistroEsExitoso() throws Exception {
        var request = new com.compunex.b2b.modules.auth.dto.request.RegistroProveedorRequestDTO(
                "Juan Carlos Rivera",
                "contacto@innovapc.com",
                "InnovaPass2026!",
                "20712345678",
                "Innova PC Mayoristas S.A.C.",
                "Innova PC Wholesale",
                "+51 987 111 222",
                "Lima",
                "Av. Garcilaso de la Vega 1348, Int. 201",
                "5 años",
                "Especialistas en hardware."
        );

        var response = new com.compunex.b2b.modules.auth.dto.response.RegistroProveedorResponseDTO(
                2L,
                3L,
                java.util.UUID.randomUUID(),
                "Juan Carlos Rivera",
                "contacto@innovapc.com",
                "PROVEEDOR",
                "20712345678",
                "Innova PC Mayoristas S.A.C.",
                "jwt.token.simulado",
                "Bearer",
                86400L,
                "Proveedor registrado exitosamente en la plataforma COMPUNEX."
        );

        when(authService.registrarProveedor(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.proveedorId").value(3))
                .andExpect(jsonPath("$.correo").value("contacto@innovapc.com"))
                .andExpect(jsonPath("$.rol").value("PROVEEDOR"))
                .andExpect(jsonPath("$.ruc").value("20712345678"))
                .andExpect(jsonPath("$.token").value("jwt.token.simulado"))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"));
    }

    @Test
    @DisplayName("[0.4] Debe retornar HTTP 400 Bad Request cuando el registro tiene datos inválidos (RUC menor a 11 dígitos)")
    void debeRetornarHttp400CuandoRegistroTieneDatosInvalidos() throws Exception {
        var requestInvalido = new com.compunex.b2b.modules.auth.dto.request.RegistroProveedorRequestDTO(
                "", // nombre vacío
                "correo-invalido",
                "123", // password corto
                "123", // RUC inválido
                "", // razon social vacía
                null,
                "", // telefono vacío
                "", // ciudad vacía
                "", // direccion vacía
                null,
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
