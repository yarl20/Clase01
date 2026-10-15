package com.compunex.b2b.modules.auth.dto.response;

public record UsuarioSesionDTO(String correo, String nombre, String rol, boolean valido) {
}
