package com.compunex.b2b.modules.catalogo.dto.response;

import java.util.List;

public record ProductoPaginadoResponseDTO(
        List<ProductoResumenResponseDTO> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean esUltima
) {
}
