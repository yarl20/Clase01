# 03. Arquitectura y Estructura del Backend (Spring Boot 3)

> **Plataforma:** COMPUNEX B2B — Ecosistema Mayorista de Componentes de Computación  
> **Framework:** Java 17 / 21 + Spring Boot 3.3+ + Spring Security 6 + Spring Data JPA + Hibernate  
> **Persistencia:** PostgreSQL 15+  
> **Patrón:** Monolito Modular por Dominios (*Vertical Slicing* + *Clean Architecture*)  
> **Estándar DTO:** Segregación estricta de responsabilidades en paquetes `dto/request/` y `dto/response/`  
> **Metodología de Calidad:** TDD Estricto (Pruebas de Repositorio `@DataJpaTest`, Pruebas de Servicio Mockito, Pruebas de Controlador `@WebMvcTest`)

---

## 1. Estructura Completa del Proyecto Maven

```text
compunex-backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/compunex/b2b/
    │   │   ├── CompunexApplication.java
    │   │   │
    │   │   ├── config/                          # Configuraciones de Infraestructura Spring
    │   │   │   ├── SecurityConfig.java          # Spring Security 6 + Filtros JWT sin estado
    │   │   │   ├── OpenApiConfig.java           # Documentación Swagger / OpenAPI 3.0
    │   │   │   ├── CorsConfig.java              # Políticas de orígenes cruzados para React SPA
    │   │   │   └── JpaAuditingConfig.java       # Auditoría JPA (@CreatedDate, @LastModifiedDate)
    │   │   │
    │   │   ├── security/                        # Componentes Transversales de Seguridad
    │   │   │   ├── JwtTokenProvider.java        # Firma, emisión y validación de tokens JWT
    │   │   │   ├── JwtAuthenticationFilter.java # Interceptor de Authorization: Bearer <TOKEN>
    │   │   │   ├── CustomUserDetails.java       # Implementación UserDetails de Spring Security
    │   │   │   └── SecurityUtils.java           # Acceso estático al usuario en SecurityContext
    │   │   │
    │   │   ├── modules/                         # Módulos de Negocio (Vertical Slices)
    │   │   │   │
    │   │   │   ├── auth/                        # 🔐 Módulo 0: Autenticación y Credenciales
    │   │   │   │   ├── controller/              # AuthController (/api/v1/auth)
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── request/             # 📥 DTOs de Entrada con @Valid
    │   │   │   │   │   │   ├── LoginRequestDTO.java
    │   │   │   │   │   │   └── CambiarPasswordDTO.java
    │   │   │   │   │   └── response/            # 📤 DTOs de Salida (Records inmutables)
    │   │   │   │   │       ├── AuthResponseDTO.java (Solo token JWT, cero ID)
    │   │   │   │   │       └── UsuarioSesionDTO.java
    │   │   │   │   ├── service/
    │   │   │   │   │   ├── AuthService.java     # Interfaz
    │   │   │   │   │   └── impl/
    │   │   │   │   │       └── AuthServiceImpl.java
    │   │   │   │   └── repository/              # UsuarioRepository
    │   │   │   │
    │   │   │   ├── catalogo/                    # 📦 Módulo 1 & 3: Categorías, Productos, Specs, Fotos
    │   │   │   │   ├── controller/              # ProductoProveedorController, CategoriaController
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── request/             # 📥 DTOs de Entrada
    │   │   │   │   │   │   ├── CrearProductoRequestDTO.java
    │   │   │   │   │   │   ├── ActualizarProductoRequestDTO.java
    │   │   │   │   │   │   └── CambiarEstadoProductoDTO.java
    │   │   │   │   │   └── response/            # 📤 DTOs de Salida
    │   │   │   │   │       ├── ProductoDetalleResponseDTO.java
    │   │   │   │   │       ├── ProductoResumenResponseDTO.java
    │   │   │   │   │       ├── CategoriaResponseDTO.java
    │   │   │   │   │       └── CambioEstadoResponseDTO.java
    │   │   │   │   ├── entity/                  # Entidades JPA (@Entity)
    │   │   │   │   │   ├── Categoria.java
    │   │   │   │   │   ├── Subcategoria.java
    │   │   │   │   │   ├── Producto.java
    │   │   │   │   │   ├── ProductoEspecificacion.java
    │   │   │   │   │   └── ImagenProducto.java
    │   │   │   │   ├── mapper/                  # Conversores Entity ⇄ DTO
    │   │   │   │   │   ├── ProductoMapper.java
    │   │   │   │   │   └── CategoriaMapper.java
    │   │   │   │   ├── repository/              # Spring Data JPA
    │   │   │   │   │   ├── ProductoRepository.java
    │   │   │   │   │   └── CategoriaRepository.java
    │   │   │   │   └── service/                 # Lógica transaccional
    │   │   │   │       ├── ProductoService.java
    │   │   │   │       ├── CategoriaService.java
    │   │   │   │       └── impl/
    │   │   │   │           ├── ProductoServiceImpl.java
    │   │   │   │           └── CategoriaServiceImpl.java
    │   │   │   │
    │   │   │   ├── ofertas/                     # 🏷️ Módulo 4: Ofertas y Descuentos Mayoristas
    │   │   │   │   ├── controller/              # OfertaProveedorController
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── request/             # 📥 DTOs de Entrada
    │   │   │   │   │   │   ├── CrearOfertaRequestDTO.java
    │   │   │   │   │   │   └── ActualizarOfertaRequestDTO.java
    │   │   │   │   │   └── response/            # 📤 DTOs de Salida
    │   │   │   │   │       └── OfertaResponseDTO.java
    │   │   │   │   ├── entity/                  # Entidad JPA
    │   │   │   │   │   └── Oferta.java
    │   │   │   │   ├── mapper/
    │   │   │   │   │   └── OfertaMapper.java
    │   │   │   │   ├── repository/              # OfertaRepository
    │   │   │   │   └── service/
    │   │   │   │       ├── OfertaService.java
    │   │   │   │       └── impl/
    │   │   │   │           └── OfertaServiceImpl.java
    │   │   │   │
    │   │   │   ├── perfiles/                    # 🏢 Módulo 2: Perfiles Corporativos Mayoristas
    │   │   │   │   ├── controller/              # PerfilProveedorController
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── request/
    │   │   │   │   │   │   └── ActualizarPerfilRequestDTO.java
    │   │   │   │   │   └── response/
    │   │   │   │   │       └── PerfilProveedorResponseDTO.java
    │   │   │   │   ├── entity/
    │   │   │   │   │   ├── PerfilProveedor.java
    │   │   │   │   │   ├── ProveedorMarca.java
    │   │   │   │   │   └── ProveedorEspecialidad.java
    │   │   │   │   ├── mapper/
    │   │   │   │   │   └── PerfilProveedorMapper.java
    │   │   │   │   ├── repository/              # PerfilProveedorRepository
    │   │   │   │   └── service/
    │   │   │   │       ├── ProveedorService.java
    │   │   │   │       └── impl/
    │   │   │   │           └── ProveedorServiceImpl.java
    │   │   │   │
    │   │   │   └── auditoria/                   # 🛡️ Historial de Bajas Lógicas (Audit Log)
    │   │   │       ├── entity/
    │   │   │       │   └── RegistroAuditoria.java
    │   │   │       ├── repository/
    │   │   │       │   └── RegistroAuditoriaRepository.java
    │   │   │       └── service/
    │   │   │           ├── AuditoriaService.java
    │   │   │           └── impl/
    │   │   │               └── AuditoriaServiceImpl.java
    │   │   │
    │   │   └── shared/                          # Componentes Transversales Compartidos
    │   │       ├── exception/                   # Manejo centralizado RFC-7807
    │   │       │   ├── GlobalExceptionHandler.java # @RestControllerAdvice
    │   │       │   ├── ResourceNotFoundException.java
    │   │       │   ├── BusinessRuleException.java
    │   │       │   └── UnauthorizedException.java
    │   │       ├── dto/
    │   │       │   ├── MensajeRespuestaDTO.java # Mensajes simples de confirmación
    │   │       │   ├── ErrorResponseDTO.java    # Formato RFC-7807 de error
    │   │       │   └── PageResponse.java        # Envoltorio desacoplado de paginación
    │   │       └── entity/
    │   │           └── BaseAuditableEntity.java # @MappedSuperclass (fechaCreacion, etc.)
    │   │
    │   └── resources/
    │       ├── application.yml                  # Configuración base de Spring Boot
    │       ├── application-dev.yml              # Conexión local PostgreSQL (puerto 5432)
    │       └── application-prod.yml             # Perfil cloud / variables de entorno
    │
    └── test/                                    # 🧪 Batería de Pruebas TDD (Fases 1, 2 y 3)
        └── java/com/compunex/b2b/
            ├── modules/
            │   ├── auth/
            │   │   ├── controller/
            │   │   │   └── AuthControllerTest.java           # Fase 3: @WebMvcTest
            │   │   └── service/
            │   │       └── AuthServiceTest.java              # Fases 1 & 2: Mockito
            │   ├── catalogo/
            │   │   ├── controller/
            │   │   │   └── ProductoProveedorControllerTest.java # Fase 3: @WebMvcTest
            │   │   ├── repository/
            │   │   │   └── ProductoRepositoryTest.java       # Fase 1: @DataJpaTest
            │   │   └── service/
            │   │       └── ProductoServiceTest.java          # Fases 1 & 2: Mockito
            │   ├── ofertas/
            │   │   ├── controller/
            │   │   │   └── OfertaProveedorControllerTest.java # Fase 3: @WebMvcTest
            │   │   ├── repository/
            │   │   │   └── OfertaRepositoryTest.java         # Fase 1: @DataJpaTest
            │   │   └── service/
            │   │       └── OfertaServiceTest.java            # Fases 1 & 2: Mockito
            │   └── perfiles/
            │       ├── controller/
            │       │   └── PerfilProveedorControllerTest.java# Fase 3: @WebMvcTest
            │       └── service/
            │           └── ProveedorServiceTest.java         # Fases 1 & 2: Mockito
            └── shared/
                └── GlobalExceptionHandlerTest.java
```

---

## 2. Convenciones de Segregación de DTOs (`request/` vs `response/`)

Para evitar que los programadores reutilicen el mismo DTO para entrada y salida, se aplican las siguientes reglas:

| Paquete | Rol Arquitectónico | Anotaciones Permitidas | Convención de Nombre | Ejemplo |
|---|---|---|---|---|
| **`dto/request/`** | Parsear el `@RequestBody` del cliente HTTP | `@Valid`, `@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Pattern`, `@Email` | `*RequestDTO.java` | `CrearProductoRequestDTO.java` |
| **`dto/response/`** | Serializar datos limpios hacia la UI React | `@JsonProperty`, `record` inmutables de Java 17+, proyecciones de vista | `*ResponseDTO.java` | `ProductoDetalleResponseDTO.java` |

> [!IMPORTANT]
> **Regla de Cero Exposición:** Ningún DTO en `dto/response/` debe incluir el `hashContrasena`, IDs internos de usuario o atributos de auditoría interna que no pertenezcan al contrato de la vista.

---

## 3. Responsabilidades por Capa Arquitectónica

### 🎮 1. Capa de Controladores (`controller/`) — *Thin Controllers*
- **Responsabilidad:** Exposición de endpoints REST, parsing de parámetros HTTP (`@PathVariable`, `@RequestParam`, `@RequestBody`).
- **Controlador Delgado:** Cero lógica de negocio. Solo valida el DTO de entrada con `@Valid` y delega la ejecución al método del servicio.
- **Seguridad:** Decorado con `@PreAuthorize("hasRole('PROVEEDOR')")` a nivel de clase o método.

### ⚙️ 2. Capa de Servicios (`service/` y `service/impl/`)
- **Interfaz (`service/`):** Define el contrato del caso de uso. El líder técnico la crea primero junto con los DTOs.
- **Implementación (`service/impl/`):** El programador implementa la lógica transaccional guiado por el ciclo TDD.
- **Transaccionalidad:** Todo método modificador de estado (`save`, `update`, `deleteLogico`) lleva `@Transactional(rollbackFor = Exception.class)`. Las consultas de solo lectura llevan `@Transactional(readOnly = true)`.

### 🔄 3. Capa de Mapeo (`mapper/`)
- **Responsabilidad:** Desacoplar las entidades de base de datos (`@Entity`) de los contratos HTTP (`DTO`).
- **Mecanismo:** Clases dedicadas con métodos estáticos o interfaces MapStruct (`toEntity()`, `toResponse()`).

### 🗄️ 4. Capa de Persistencia (`repository/` y `entity/`)
- **Entidades (`entity/`):** Modelos JPA que mapean fielmente al `01_DICCIONARIO_DE_DATOS.md`. Uso de cascadas `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` para persistencia atómica de tablas hijas.
- **Repositorios (`repository/`):** Interfaces que extienden `JpaRepository<T, ID>` para operaciones CRUD y consultas derivadas automáticas.

---

## 4. Formato Canónico de Errores (RFC-7807)

Todos los errores y violaciones de reglas de negocio capturados por `GlobalExceptionHandler` emiten la estructura RFC-7807:

```json
{
  "timestamp": "2026-09-02T15:30:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "codigo": "RN-PRECIO-OFERTA",
  "mensaje": "El precio promocional ($98.00) debe ser estrictamente menor al precio regular ($112.00).",
  "path": "/api/v1/providers/me/offers"
}
```

---

## 5. Alineación con las Rúbricas de Evaluación (20/20 Puntos)

| Criterio de la Rúbrica | Paquete / Componente que lo Satisface |
|---|---|
| **Ítem 1: Framework Spring Boot (2 pts)** | `pom.xml` con `spring-boot-starter-web`, `spring-boot-starter-data-jpa` |
| **Ítem 2: Conexión a Base de Datos (2 pts)** | `application.yml` con driver PostgreSQL y datasource HikariCP |
| **Ítem 3: Modelado de Datos >= 6 tablas (3 pts)** | Paquetes `entity/` con 8 entidades relacionadas (`Producto`, `Categoria`, `Oferta`, etc.) |
| **Ítem 4: Repositorios y Servicios (2 pts)** | `repository/` (`JpaRepository`) y `service/impl/` (`@Transactional`) |
| **Ítem 5: RestControllers para CRUD (4 pts)** | `controller/` con los 18 endpoints REST documentados en el contrato |
| **Ítem 9: TDD Repositorio @DataJpaTest (2 pts)** | Carpeta `src/test/.../repository/` con tests de persistencia y soft delete |
| **Ítem 10: TDD Servicio / Mockito (1 pt)** | Carpeta `src/test/.../service/` con tests unitarios aislados |
| **Ítem 11: Informe y Capturas (4 pts)** | Capturas de JUnit (Rojo, Verde) y Postman para el informe Word |
