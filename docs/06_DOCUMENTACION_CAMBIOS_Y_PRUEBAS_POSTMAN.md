# Documentación Técnica de Cambios, Arquitectura y Guía de Pruebas en Postman — COMPUNEX B2B

Este documento detalla exhaustivamente la implementación técnica, la arquitectura de seguridad y datos, y la guía paso a paso para la exposición y captura de evidencias en **Postman**, alineada al 100% con la **Lista de Cotejo del Avance de Trabajo Final 2 (ATF2)** del curso de **Desarrollo Web Integrado**.

---

## Tabla de Contenidos
1. [Matriz de Correspondencia con la Lista de Cotejo (Criterios 1 al 10)](#1-matriz-de-correspondencia-con-la-lista-de-cotejo-criterios-1-al-10)
2. [Arquitectura de Seguridad: Spring Security 6 y JWT](#2-arquitectura-de-seguridad-spring-security-6-y-jwt)
3. [Arquitectura de Persistencia: JPA, Relaciones y Transacciones](#3-arquitectura-de-persistencia-jpa-relaciones-y-transacciones)
4. [Consultas Personalizadas JPQL](#4-consultas-personalizadas-jpql)
5. [Estructura de la Carpeta 06 en Postman](#5-estructura-de-la-carpeta-06-en-postman)
6. [Guía Paso a Paso de Ejecución de los 18 Casos de Prueba](#6-guía-paso-a-paso-de-ejecución-de-los-18-casos-de-prueba)
   - [6.1 Subcarpeta 06.1: Seguridad, Roles y JWT](#61-subcarpeta-061-seguridad-roles-y-jwt-criterios-5-6-7-8-9)
   - [6.2 Subcarpeta 06.2: JPA y Tablas Relacionadas](#62-subcarpeta-062-jpa-y-tablas-relacionadas-criterios-1-2-3-4)
   - [6.3 Subcarpeta 06.3: Consultas JPQL y Transición de Estados](#63-subcarpeta-063-consultas-jpql-y-transición-de-estados-criterios-1-4)
7. [Batería de Pruebas Automatizadas (Maven - 59 Tests)](#7-batería-de-pruebas-automatizadas-maven---59-tests)

---

## 1. Matriz de Correspondencia con la Lista de Cotejo (Criterios 1 al 10)

| Ítem | Criterio de Evaluación | Pts | Implementación en COMPUNEX B2B | Casos en Carpeta 06 de Postman |
|---|---|:---:|---|---|
| **1** | **Lectura en tablas Relacionadas** (mínimo 4 casos) | 2 | Relaciones `Categoria` -> `Subcategoria`, `Categoria` -> `Producto`, `Producto` -> `PerfilProveedor`, y `Producto` -> `Especificaciones` / `Imagenes`. | Casos 8, 9, 10 y 11 |
| **2** | **Escritura en tablas Relacionadas** (mínimo 2 casos) | 4 | **Caso A:** Creación de producto vinculándolo a categoría existente y guardando especificaciones e imágenes hijas en cascada.<br>**Caso B:** Registro de proveedor vinculando `Usuario` y `PerfilProveedor`. | Casos 12 y 13 |
| **3** | **Actualización en tablas Relacionadas** (mínimo 1 caso) | 2 | Actualización de producto permitiendo la modificación de su relación con la categoría y actualización de especificaciones. | Caso 14 |
| **4** | **Transacciones (`@Transactional`)** | 2 | Anotación `@Transactional` en métodos de servicio multientidad (`ProductoServiceImpl.crearProducto`, `ProductoServiceImpl.actualizarProducto`, `AuthServiceImpl.registrarProveedor`). | Casos 12, 13 y 14 |
| **5** | **Spring Security con usuarios en BD** | 1 | `SecurityConfig`, `CustomUserDetailsService` que carga usuarios desde PostgreSQL y contraseñas cifradas con `BCryptPasswordEncoder`. | Casos 1, 2, 3 y 4 |
| **6** | **Registro e inicio de sesión** | 2 | `AuthController`: `POST /api/v1/auth/register` (registro corporativo) y `POST /api/v1/auth/login` (login con validación de credenciales). | Casos 1 y 2 |
| **7** | **Roles Definidos** (mínimo 2 roles) | 1 | Enumeración `RolUsuario`: `ROLE_ADMIN`, `ROLE_PROVEEDOR`, `ROLE_COMPRADOR`. Control de acceso basado en roles (RBAC) en `SecurityConfig`. | Casos 1, 2, 3, 6 y 7 |
| **8** | **Generación de JWT** | 1 | `JwtTokenProvider` genera tokens JWT válidos firmados con algoritmo HMAC-SHA512 con tiempo de vida de 24 horas y claim personalizado `rol`. | Casos 1, 2 y 3 |
| **9** | **Validación y autenticación con JWT** | 3 | `JwtAuthenticationFilter` intercepta cada solicitud, valida firma y expiración, y establece el contexto. Retorna `401 Unauthorized` si falta token y `403 Forbidden` si el rol es insuficiente. | Casos 4, 5, 6 y 7 |
| **10** | **Documento de Plan de Desarrollo / Evidencias** | 2 | Colección exportada en JSON, guía técnica detallada paso a paso y capturas de funcionamiento para el informe final ATF2. | Todo el documento |

---

## 2. Arquitectura de Seguridad: Spring Security 6 y JWT

La arquitectura de seguridad opera bajo el principio de arquitectura sin estado (**Stateless**):

1. **Configuración Perimetral (`SecurityConfig.java`):**
   - Modo de sesión: `SessionCreationPolicy.STATELESS` (sin sesiones HTTP en memoria).
   - Manejo de excepciones: `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` para garantizar que peticiones sin token respondan estrictamente `401 Unauthorized`.
   - Control RBAC:
     - Endpoints de administración: `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")`
     - Endpoints de proveedor: `.requestMatchers("/api/v1/providers/**").hasRole("PROVEEDOR")`
     - Perfil autenticado: `.requestMatchers("/api/v1/auth/me", "/api/v1/auth/password").authenticated()`
     - Endpoints públicos: `.requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/registro-proveedor").permitAll()`, catálogo de categorías y OpenAPI Swagger UI.
2. **Cifrado de Contraseñas:**
   - Implementado mediante `BCryptPasswordEncoder` con costo de cómputo estándar (10 rounds).
3. **Filtro de Intercepción (`JwtAuthenticationFilter.java`):**
   - Registrado antes de `UsernamePasswordAuthenticationFilter`.
   - Extrae la cabecera `Authorization: Bearer <token>`, valida la firma criptográfica con la clave secreta y carga el `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder`.

---

## 3. Arquitectura de Persistencia: JPA, Relaciones y Transacciones

El modelo relacional mapea las siguientes entidades:

- **`Usuario` (1) <---> (1) `PerfilProveedor`:** Relación uno a uno vinculada mediante clave foránea `usuario_id` en `perfiles_proveedores`.
- **`Categoria` (1) <---> (N) `Producto`:** Relación `@ManyToOne` en `Producto` hacia `Categoria` (`categoria_id`).
- **`PerfilProveedor` (1) <---> (N) `Producto`:** Relación `@ManyToOne` en `Producto` hacia `PerfilProveedor` (`proveedor_id`).
- **`Producto` (1) <---> (N) `EspecificacionProducto`:** Relación `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` que almacena la ficha técnica dinámica.
- **`Producto` (1) <---> (N) `ImagenProducto`:** Relación `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` para galería multimedia.

### Transaccionalidad Atómica (`@Transactional`)
Los métodos críticos están anotados con `@Transactional` de `org.springframework.transaction.annotation`:
- `AuthServiceImpl.registrarProveedor`: Garantiza que si la inserción del `PerfilProveedor` falla, el `Usuario` hace rollback automático.
- `ProductoServiceImpl.crearProducto`: Persiste el producto y todas sus entidades hijas (`especificaciones`, `imagenes`) en una sola unidad de trabajo atómica.
- `ProductoServiceImpl.actualizarProducto`: Actualiza campos propios y sincroniza las colecciones hijas bajo aislamiento de transacción.

---

## 4. Consultas Personalizadas JPQL

En `ProductoRepository.java`, se implementan consultas JPQL mediante la anotación `@Query`:

```java
// Búsqueda insensible a mayúsculas por proveedor con exclusión de eliminados
@Query("SELECT p FROM Producto p WHERE p.proveedor = :proveedor AND p.eliminado = false AND " +
       "(LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       " LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))")
Page<Producto> buscarPorProveedorConFiltro(@Param("proveedor") PerfilProveedor proveedor,
                                          @Param("search") String search,
                                          Pageable pageable);

// Supervisión global administrativa con búsqueda dinámica
@Query("SELECT p FROM Producto p WHERE p.eliminado = false AND " +
       "(LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       " LOWER(p.proveedor.razonSocial) LIKE LOWER(CONCAT('%', :search, '%')))")
Page<Producto> buscarTodosConFiltroAdmin(@Param("search") String search, Pageable pageable);
```

---

## 5. Estructura de la Carpeta 06 en Postman

Dentro de la colección oficial `COMPUNEX_B2B_Coleccion_Postman.json`, la carpeta principal **`06. Pruebas y Exposición - Lista de Cotejo`** se divide en 3 subcarpetas temáticas:

```
06. Pruebas y Exposición - Lista de Cotejo (Seguridad, JWT, JPA, JPQL y Roles)
├── 06.1 - Seguridad, Roles y JWT (Criterios 5, 6, 7, 8, 9)
│   ├── [Caso 1 - Criterios 6, 7, 8] Registro de Proveedor en BD (BCrypt + Rol PROVEEDOR + JWT)
│   ├── [Caso 2 - Criterios 6, 8] Login de Proveedor Mayorista (Autenticación BD + Emisión JWT)
│   ├── [Caso 3 - Criterios 7, 8] Login de Administrador (Autenticación BD + Rol ADMIN + Emisión JWT)
│   ├── [Caso 4 - Criterios 5, 9] Validación Filtro JWT - Perfil /me con Token Bearer Válido (200 OK)
│   ├── [Caso 5 - Criterio 9] Validación Filtro JWT - Acceso Rechazado Sin Token (401 Unauthorized)
│   ├── [Caso 6 - Criterios 7, 9] Control de Roles RBAC - Acceso Rechazado por Rol Insuficiente (403 Forbidden)
│   └── [Caso 7 - Criterios 7, 9] Control de Roles RBAC - Acceso Autorizado como Administrador (200 OK)
├── 06.2 - JPA y Tablas Relacionadas (Criterios 1, 2, 3, 4)
│   ├── [Caso 8 - Criterio 1 / Lectura 1 de 4] Categorías con Subcategorías y Contador de Productos
│   ├── [Caso 9 - Criterio 1 / Lectura 2 de 4] Detalle de Categoría por Slug con Subcategorías Técnicas
│   ├── [Caso 10 - Criterio 1 / Lectura 3 de 4] Catálogo de Productos del Proveedor con Categoría y Stock
│   ├── [Caso 11 - Criterio 1 / Lectura 4 de 4] Ficha Técnica Completa con Imágenes y Especificaciones (OneToMany)
│   ├── [Caso 12 - Criterios 2, 4 / Escritura 1 de 2] Transacción @Transactional: Crear Producto con Categoría, Specs e Imágenes
│   ├── [Caso 13 - Criterios 2, 4 / Escritura 2 de 2] Transacción @Transactional: Registro Proveedor Vinculando Usuario y Perfil
│   └── [Caso 14 - Criterios 3, 4 / Actualización 1 de 1] Transacción @Transactional: Actualizar Producto y Reasignar Categoría
└── 06.3 - Consultas JPQL y Transición de Estados (Criterios 1, 4)
    ├── [Caso 15 - JPQL 1] Búsqueda Case-Insensitive JPQL por Proveedor (LIKE LOWER)
    ├── [Caso 16 - JPQL 2] Supervisión Global Paginada JPQL para Administrador
    ├── [Caso 17 - Estado 1] Cambio de Estado de Publicación por Proveedor
    └── [Caso 18 - Estado 2] Supervisión y Cambio de Estado Global por Administrador
```

---

## 6. Guía Paso a Paso de Ejecución de los 18 Casos de Prueba

### Variables de Colección Utilizadas
Las peticiones están parametrizadas con variables automáticas:
- `{{baseUrl}}`: `http://localhost:8080`
- `{{token}}`: Almacena automáticamente el token JWT emitido para el rol `PROVEEDOR`.
- `{{adminToken}}`: Almacena automáticamente el token JWT emitido para el rol `ADMIN`.

---

### 6.1 Subcarpeta 06.1: Seguridad, Roles y JWT (Criterios 5, 6, 7, 8, 9)

#### Caso 1: Registro de Proveedor en BD (BCrypt + Rol PROVEEDOR + JWT)
- **Criterio que demuestra:** 6 (Registro), 7 (Rol PROVEEDOR asignado), 8 (Emisión de JWT) y 4 (`@Transactional`).
- **Método y URL:** `POST {{baseUrl}}/api/v1/auth/register`
- **Cabeceras:** `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "nombre": "Juan Carlos Rivera",
    "correo": "proveedor.nuevo.postman@innovapc.com",
    "contrasena": "InnovaPass2026!",
    "ruc": "20712345678",
    "razonSocial": "Innova PC Mayoristas S.A.C.",
    "nombreComercial": "Innova PC Wholesale",
    "telefonoContacto": "+51 987 111 222",
    "ciudad": "Lima",
    "direccion": "Av. Garcilaso de la Vega 1348, Int. 201",
    "anosMercado": "5 años",
    "descripcion": "Especialistas en componentes, placas base y procesadores para ensamblaje."
  }
  ```
- **Resultado Esperado:** Código `201 Created`. Retorna el usuario y proveedor creados con su token JWT. El script de prueba guarda automáticamente el token en `{{token}}`.

#### Caso 2: Login de Proveedor Mayorista (Autenticación BD + Emisión JWT)
- **Criterio que demuestra:** 5 (Spring Security con usuarios en BD), 6 (Inicio de sesión) y 8 (Generación de JWT).
- **Método y URL:** `POST {{baseUrl}}/api/v1/auth/login`
- **Body (JSON):**
  ```json
  {
    "correo": "ventas.mayoristas@technova.com",
    "contrasena": "PasswordMayorista123!"
  }
  ```
- **Resultado Esperado:** Código `200 OK`. Retorna `{ "token": "...", "tipoToken": "Bearer", "expiraEnSegundos": 86400 }`.

#### Caso 3: Login de Administrador (Autenticación BD + Rol ADMIN + Emisión JWT)
- **Criterio que demuestra:** 7 (Segundo rol `ADMIN`) y 8 (Emisión JWT).
- **Método y URL:** `POST {{baseUrl}}/api/v1/auth/login`
- **Body (JSON):**
  ```json
  {
    "correo": "admin@compunex.com",
    "contrasena": "AdminPassword2026!"
  }
  ```
- **Resultado Esperado:** Código `200 OK`. Guarda el token en la variable `{{adminToken}}`.

#### Caso 4: Validación Filtro JWT - Perfil /me con Token Bearer Válido (200 OK)
- **Criterio que demuestra:** 9 (Validación y filtro JWT) y 5 (Contexto de seguridad).
- **Método y URL:** `GET {{baseUrl}}/api/v1/auth/me`
- **Cabeceras:** `Authorization: Bearer {{token}}`
- **Resultado Esperado:** Código `200 OK`. Retorna:
  ```json
  {
    "correo": "ventas.mayoristas@technova.com",
    "nombre": "TechNova Mayorista S.A.C.",
    "rol": "PROVEEDOR",
    "valido": true
  }
  ```

#### Caso 5: Validación Filtro JWT - Acceso Rechazado Sin Token (401 Unauthorized)
- **Criterio que demuestra:** 9 (Protección de endpoints privados y rechazo ante token ausente).
- **Método y URL:** `GET {{baseUrl}}/api/v1/auth/me`
- **Cabeceras:** Ninguna (sin cabecera Authorization).
- **Resultado Esperado:** Código `401 Unauthorized`.

#### Caso 6: Control de Roles RBAC - Acceso Rechazado por Rol Insuficiente (403 Forbidden)
- **Criterio que demuestra:** 7 (Diferenciación de roles) y 9 (Autorización RBAC).
- **Método y URL:** `GET {{baseUrl}}/api/v1/admin/products`
- **Cabeceras:** `Authorization: Bearer {{token}}` (Token de Proveedor).
- **Resultado Esperado:** Código `403 Forbidden`. El usuario está autenticado pero carece del rol `ADMIN`.

#### Caso 7: Control de Roles RBAC - Acceso Autorizado como Administrador (200 OK)
- **Criterio que demuestra:** 7 (Rol ADMIN) y 9 (Acceso autorizado según rol).
- **Método y URL:** `GET {{baseUrl}}/api/v1/admin/products`
- **Cabeceras:** `Authorization: Bearer {{adminToken}}` (Token de Administrador).
- **Resultado Esperado:** Código `200 OK`. Retorna el catálogo global en formato Page.

---

### 6.2 Subcarpeta 06.2: JPA y Tablas Relacionadas (Criterios 1, 2, 3, 4)

#### Caso 8: [Lectura 1 de 4] Categorías con Subcategorías y Contador de Productos
- **Criterio que demuestra:** 1 (Lectura en tablas relacionadas 1/4).
- **Método y URL:** `GET {{baseUrl}}/api/v1/categories` (Público).
- **Resultado Esperado:** Código `200 OK`. Array con categorías (`ram`, `almacenamiento`, etc.) y número de productos asociados.

#### Caso 9: [Lectura 2 de 4] Detalle de Categoría por Slug con Subcategorías Técnicas
- **Criterio que demuestra:** 1 (Lectura en tablas relacionadas 2/4).
- **Método y URL:** `GET {{baseUrl}}/api/v1/categories/ram` (Público).
- **Resultado Esperado:** Código `200 OK`. Detalle de la categoría con sus subcategorías técnicas asociadas.

#### Caso 10: [Lectura 3 de 4] Catálogo de Productos del Proveedor con Categoría y Stock
- **Criterio que demuestra:** 1 (Lectura en tablas relacionadas 3/4).
- **Método y URL:** `GET {{baseUrl}}/api/v1/providers/me/products?page=0&size=10`
- **Cabeceras:** `Authorization: Bearer {{token}}`
- **Resultado Esperado:** Código `200 OK`. Productos relacionados con su respectiva categoría.

#### Caso 11: [Lectura 4 de 4] Ficha Técnica Completa con Imágenes y Especificaciones (OneToMany)
- **Criterio que demuestra:** 1 (Lectura en tablas relacionadas 4/4).
- **Método y URL:** `GET {{baseUrl}}/api/v1/providers/me/products/1`
- **Cabeceras:** `Authorization: Bearer {{token}}`
- **Resultado Esperado:** Código `200 OK`. Producto con sus listas hijas `especificaciones` e `imagenes` completamente pobladas.

#### Caso 12: [Escritura 1 de 2] Transacción @Transactional: Crear Producto con Categoría, Specs e Imágenes
- **Criterio que demuestra:** 2 (Escritura en tablas relacionadas 1/2) y 4 (Transacciones atómicas con `@Transactional`).
- **Método y URL:** `POST {{baseUrl}}/api/v1/providers/me/products`
- **Cabeceras:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "categoriaId": "ram",
    "titulo": "Memoria RAM Corsair Vengeance 32GB DDR5 6000MHz",
    "descripcion": "Kit de memoria de alta velocidad optimizado para plataformas AMD EXPO e Intel XMP 3.0.",
    "modeloComercial": "DISTRIBUIDOR_OFICIAL",
    "tipoFormato": "Caja Master x5 Blísteres",
    "unidadesPorPaquete": "5 unidades",
    "pedidoMinimo": "1 caja (5 unidades)",
    "precioUnitarioRef": 84.00,
    "precioTotalRef": 420.00,
    "moneda": "USD",
    "terminosComerciales": "Garantía de por vida directa con el fabricante. Factura electrónica.",
    "especificaciones": [
      { "clave": "Capacidad", "valor": "32 GB (2x16GB)" },
      { "clave": "Frecuencia", "valor": "6000 MHz" },
      { "clave": "Latencia CAS", "valor": "CL30" },
      { "clave": "Voltaje", "valor": "1.35V" }
    ],
    "imagenes": [
      { "urlImagen": "https://img.compunex.com/products/corsair-ddr5-32gb-front.jpg", "orden": 1 },
      { "urlImagen": "https://img.compunex.com/products/corsair-ddr5-32gb-box.jpg", "orden": 2 }
    ]
  }
  ```
- **Resultado Esperado:** Código `201 Created`. Persiste la entidad padre `Producto` vinculada a `Categoria` y almacena en cascada las especificaciones e imágenes.

#### Caso 13: [Escritura 2 de 2] Transacción @Transactional: Registro Proveedor Vinculando Usuario y Perfil
- **Criterio que demuestra:** 2 (Escritura en tablas relacionadas 2/2) y 4 (Transacciones atómicas con `@Transactional`).
- **Método y URL:** `POST {{baseUrl}}/api/v1/auth/register`
- **Cabeceras:** `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "nombre": "Carlos Gomez",
    "correo": "carlos.gomez.ti@compunex.com",
    "contrasena": "Password123!",
    "ruc": "20601234567",
    "razonSocial": "Comercializadora TI Global S.A.C.",
    "nombreComercial": "TI Global Wholesale",
    "telefonoContacto": "+51 912 345 678",
    "ciudad": "Arequipa",
    "direccion": "Calle Mercaderes 405",
    "anosMercado": "4 años",
    "descripcion": "Distribuidor mayorista de periféricos y componentes gamer."
  }
  ```
- **Resultado Esperado:** Código `201 Created`. Vincula `Usuario` y `PerfilProveedor` en una sola transacción atómica.

#### Caso 14: [Actualización 1 de 1] Transacción @Transactional: Actualizar Producto y Reasignar Categoría
- **Criterio que demuestra:** 3 (Actualización en tablas relacionadas) y 4 (Transacciones con `@Transactional`).
- **Método y URL:** `PUT {{baseUrl}}/api/v1/providers/me/products/1`
- **Cabeceras:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "categoriaId": "ram",
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR4 3200MHz Edición Especial",
    "descripcion": "Módulo de memoria RAM DDR4 de alto rendimiento con disipador térmico negro mejorado.",
    "modeloComercial": "DISTRIBUIDOR_OFICIAL",
    "tipoFormato": "Caja Master x10 Blísteres",
    "unidadesPorPaquete": "10 unidades",
    "pedidoMinimo": "1 caja (10 unidades)",
    "precioUnitarioRef": 13.95,
    "precioTotalRef": 139.50,
    "moneda": "USD",
    "terminosComerciales": "Garantía extendida de 3 años directa con fabricante.",
    "especificaciones": [
      { "clave": "Capacidad", "valor": "16 GB" },
      { "clave": "Frecuencia", "valor": "3200 MHz" },
      { "clave": "Tipo", "valor": "DDR4" }
    ],
    "imagenes": [
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-special.jpg", "orden": 1 }
    ]
  }
  ```
- **Resultado Esperado:** Código `200 OK`. Actualiza el producto y reasigna sus datos y relaciones transaccionalmente.

---

### 6.3 Subcarpeta 06.3: Consultas JPQL y Transición de Estados (Criterios 1, 4)

#### Caso 15: Búsqueda Case-Insensitive JPQL por Proveedor (LIKE LOWER)
- **Criterio que demuestra:** 1 y 4 (Consultas avanzadas JPQL).
- **Método y URL:** `GET {{baseUrl}}/api/v1/providers/me/products?page=0&size=10&search=Kingston`
- **Cabeceras:** `Authorization: Bearer {{token}}`
- **Resultado Esperado:** Código `200 OK`. Retorna únicamente los productos que coinciden con el término `Kingston` ignorando mayúsculas/minúsculas y pertenecientes al proveedor autenticado.

#### Caso 16: Supervisión Global Paginada JPQL para Administrador
- **Criterio que demuestra:** 1 y 7 (Consultas administrativas globales JPQL).
- **Método y URL:** `GET {{baseUrl}}/api/v1/admin/products?page=0&size=10&search=RAM`
- **Cabeceras:** `Authorization: Bearer {{adminToken}}`
- **Resultado Esperado:** Código `200 OK`. Retorna el catálogo consolidado de todos los proveedores.

#### Caso 17: Cambio de Estado de Publicación por Proveedor
- **Criterio que demuestra:** Control de estado de ciclo de vida por el proveedor.
- **Método y URL:** `PATCH {{baseUrl}}/api/v1/providers/me/products/1/status`
- **Cabeceras:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "estado": "INACTIVO"
  }
  ```
- **Resultado Esperado:** Código `200 OK`. Modifica el estado respetando la titularidad del producto.

#### Caso 18: Supervisión y Cambio de Estado Global por Administrador
- **Criterio que demuestra:** Privilegios de supervisión administrativa.
- **Método y URL:** `PATCH {{baseUrl}}/api/v1/admin/products/1/status`
- **Cabeceras:** `Authorization: Bearer {{adminToken}}`, `Content-Type: application/json`
- **Body (JSON):**
  ```json
  {
    "estado": "PENDIENTE_REVISION"
  }
  ```
- **Resultado Esperado:** Código `200 OK`. El Administrador puede cambiar el estado de cualquier producto sin restricción de dueño.

---

## 7. Batería de Pruebas Automatizadas (Maven - 59 Tests)

El proyecto cuenta con 59 pruebas automatizadas unitarias, de integración y de capa web que se ejecutan sobre base de datos en memoria H2 aislada:

```powershell
# Desde el directorio compunex-backend:
.\mvnw.cmd test
```

### Resultados de Ejecución
- **Total de pruebas:** 59
- **Fallos (Failures):** 0
- **Errores (Errors):** 0
- **Omitidas (Skipped):** 0
- **Resultado:** **`BUILD SUCCESS`**

### Clases de Prueba Ejecutadas
1. `CompunexBackendApplicationTests`: Carga del ApplicationContext de Spring Boot 3.5.16.
2. `AuthServiceTest`: Pruebas de servicio unitarias para registro y login con PasswordEncoder y emisión de JWT.
3. `AuthControllerTest`: Pruebas de capa web (`@WebMvcTest`) para login y registro con validación de Bean Validation.
4. `CategoriaServiceTest`: Pruebas de servicio para listado y detalle de categorías.
5. `CategoriaControllerTest`: Pruebas del controlador público de categorías.
6. `ProductoServiceTest`: 13 pruebas unitarias para creación, edición, borrado lógico y estados bajo `@Transactional`.
7. `ProductoProveedorControllerTest`: Pruebas web del CRUD del proveedor.
8. `AdminProductoControllerTest`: Pruebas web del controlador de administración.
9. `ProductoRepositoryTest`: Pruebas de integración de persistencia con `@DataJpaTest` validando consultas personalizadas `@Query` en JPQL.
10. `SecurityAuthorizationTest`: 8 pruebas de integración de seguridad validando respuestas 200, 401 Unauthorized y 403 Forbidden según token y roles.
