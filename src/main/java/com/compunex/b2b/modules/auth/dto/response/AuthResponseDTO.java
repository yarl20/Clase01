package com.compunex.b2b.modules.auth.dto.response;

public record AuthResponseDTO(
    String token,
    String tipoToken,
    Long expiraEnSegundos
) {}
