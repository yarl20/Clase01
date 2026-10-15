package com.compunex.b2b.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordDTO(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String contrasenaActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String nuevaContrasena) {
}
