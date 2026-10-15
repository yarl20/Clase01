package com.compunex.b2b.modules.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Respuesta de registro corporativo exitoso con emisión de credencial JWT")
public record RegistroProveedorResponseDTO(

        @Schema(description = "Identificador único secuencial del usuario registrado", example = "2")
        Long usuarioId,

        @Schema(description = "Identificador único secuencial del perfil de proveedor creado", example = "2")
        Long proveedorId,

        @Schema(description = "Identificador único universal (UUID) del usuario", example = "c89f56a1-3e4b-4b2a-8f9d-123456789abc")
        UUID uuid,

        @Schema(description = "Nombre completo del contacto o representante", example = "Juan Carlos Rivera")
        String nombre,

        @Schema(description = "Correo electrónico corporativo asignado a la cuenta", example = "contacto@innovapc.com")
        String correo,

        @Schema(description = "Rol del usuario en el sistema", example = "PROVEEDOR")
        String rol,

        @Schema(description = "RUC de la empresa vinculada al perfil", example = "20712345678")
        String ruc,

        @Schema(description = "Razón social de la empresa", example = "Innova PC Mayoristas S.A.C.")
        String razonSocial,

        @Schema(description = "Token de autenticación Bearer JWT generado para uso inmediato", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Esquema de autenticación HTTP", example = "Bearer")
        String tipoToken,

        @Schema(description = "Tiempo de vigencia del token en segundos (24 horas)", example = "86400")
        Long expiraEnSegundos,

        @Schema(description = "Mensaje informativo del resultado de la operación", example = "Proveedor registrado exitosamente en la plataforma COMPUNEX.")
        String mensaje
) {}

