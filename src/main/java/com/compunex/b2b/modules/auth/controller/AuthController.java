package com.compunex.b2b.modules.auth.controller;

import com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO;
import com.compunex.b2b.modules.auth.dto.request.LoginRequestDTO;
import com.compunex.b2b.modules.auth.dto.response.AuthResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.CambioPasswordResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.UsuarioSesionDTO;
import com.compunex.b2b.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "00. Autenticación y Sesión", description = "Endpoints de autenticación, sesión activa y cambio de credenciales")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "[0.1] Inicio de sesión y emisión de token JWT",
        description = "Valida credenciales corporativas del proveedor y emite un token Bearer con 24 horas de vigencia."
    )
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
        summary = "[0.2] Rehidratar sesión de usuario",
        description = "Valida el token y retorna los datos de la sesión activa."
    )
    public ResponseEntity<UsuarioSesionDTO> me() {
        return ResponseEntity.ok(authService.obtenerSesion());
    }

    @PatchMapping("/password")
    @Operation(
        summary = "[0.3] Cambio de contraseña corporativa",
        description = "Valida la contraseña actual con BCrypt y guarda el nuevo hash."
    )
    public ResponseEntity<CambioPasswordResponseDTO> cambiarPassword(@Valid @RequestBody CambiarPasswordDTO request) {
        return ResponseEntity.ok(authService.cambiarPassword(request));
    }

    @PostMapping({"/register", "/registro-proveedor"})
    @Operation(
        summary = "[0.4] Registro corporativo de nuevo proveedor mayorista",
        description = "Registra una nueva cuenta de usuario con rol PROVEEDOR, persiste su perfil fiscal de empresa en relación de clave foránea y emite un token JWT inicial."
    )
    public ResponseEntity<com.compunex.b2b.modules.auth.dto.response.RegistroProveedorResponseDTO> registrarProveedor(
            @Valid @RequestBody com.compunex.b2b.modules.auth.dto.request.RegistroProveedorRequestDTO request) {
        com.compunex.b2b.modules.auth.dto.response.RegistroProveedorResponseDTO response = authService.registrarProveedor(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
}
