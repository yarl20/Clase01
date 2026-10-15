package com.compunex.b2b.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de registro corporativo para nuevos proveedores mayoristas")
public record RegistroProveedorRequestDTO(

        @Schema(description = "Nombre completo del representante o contacto comercial", example = "Juan Carlos Rivera")
        @NotBlank(message = "El nombre del contacto o representante no puede estar vacío")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        @Schema(description = "Correo electrónico corporativo (usado para iniciar sesión)", example = "contacto@innovapc.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico es inválido")
        String correo,

        @Schema(description = "Contraseña segura (mínimo 8 caracteres)", example = "InnovaPass2026!")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,

        @Schema(description = "Registro Único de Contribuyentes (RUC) de 11 dígitos", example = "20712345678")
        @NotBlank(message = "El RUC de la empresa es obligatorio")
        @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos numéricos")
        String ruc,

        @Schema(description = "Razón social formal registrada ante la administración tributaria", example = "Innova PC Mayoristas S.A.C.")
        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
        String razonSocial,

        @Schema(description = "Nombre comercial o de marca de la empresa", example = "Innova PC Wholesale")
        @Size(max = 200, message = "El nombre comercial no puede exceder 200 caracteres")
        String nombreComercial,

        @Schema(description = "Teléfono móvil o de contacto comercial para compradores B2B", example = "+51 987 111 222")
        @NotBlank(message = "El teléfono de contacto es obligatorio")
        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefonoContacto,

        @Schema(description = "Ciudad donde opera la sede principal o almacén del proveedor", example = "Lima")
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        String ciudad,

        @Schema(description = "Dirección física fiscal de la empresa", example = "Av. Garcilaso de la Vega 1348, Int. 201")
        @NotBlank(message = "La dirección fiscal es obligatoria")
        @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
        String direccion,

        @Schema(description = "Tiempo de experiencia comercial en el mercado de hardware", example = "5 años")
        String anosMercado,

        @Schema(description = "Descripción breve del catálogo o especialidad mayorista", example = "Especialistas en componentes, placas base y procesadores para ensamblaje.")
        String descripcion
) {}

