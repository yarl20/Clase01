package com.compunex.b2b.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo es inválido")
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    String contrasena
) {}
