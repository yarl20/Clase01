package com.compunex.b2b.modules.catalogo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CambioEstadoResponseDTO(
        Long id,
        UUID uuid,
        String estadoAnterior,
        String nuevoEstado,
        String mensaje,
        Instant fechaActualizacion
) {
}
