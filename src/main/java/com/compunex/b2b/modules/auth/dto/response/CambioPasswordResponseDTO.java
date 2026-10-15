package com.compunex.b2b.modules.auth.dto.response;

import java.time.Instant;

public record CambioPasswordResponseDTO(String mensaje, Instant fechaActualizacion) {
}
