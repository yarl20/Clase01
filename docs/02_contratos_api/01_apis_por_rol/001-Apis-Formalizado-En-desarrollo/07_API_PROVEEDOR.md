# 07. Contrato Oficial de APIs — Módulo Proveedor & Catálogo B2B (Primer Avance)

> **Casa de Software:** NexusCore Software Studios S.A.C.  
> **Proyecto:** COMPUNEX B2B — Wholesale Catalog API (Primer Avance)  
> **Framework Base:** Java 21 + Spring Boot 3.3+ + Spring Security 6 + Spring Data JPA + Hibernate  
> **Persistencia:** PostgreSQL 15+ administrado 100% mediante ORM / Entidades JPA (Cero SQL manual)  
> **Metodología de Desarrollo:** **TDD Estricto (Fase 1 RED ➔ Fase 2 GREEN ➔ Fase 3 REFACTOR) + Verificación en Postman**  
> **Alcance del Primer Avance:** Autenticación básica y CRUD de Catálogo de Productos y Categorías con sus 7 tablas JPA relacionadas (`usuarios`, `perfiles_proveedor`, `categorias`, `subcategorias`, `productos`, `producto_especificaciones`, `imagenes_producto`).

---

## ⚡ ÍNDICE DE ACCESO RÁPIDO (Haz clic para saltar al endpoint)

<a id="indice-rapido"></a>

| Nº | Método | Endpoint REST | Descripción Rápida | Salto Directo |
|:---:|:---:|---|---|:---:|
| **0.1** | `POST` | `/api/v1/auth/login` | Login y emisión de token JWT | 🚀 [Ir al Endpoint](#endpoint-0-1) |
| **0.2** | `GET` | `/api/v1/auth/me` | Rehidratar sesión de usuario (F5) | 🚀 [Ir al Endpoint](#endpoint-0-2) |
| **0.3** | `PATCH` | `/api/v1/auth/password` | Cambio de contraseña corporativa | 🚀 [Ir al Endpoint](#endpoint-0-3) |
| **1.1** | `GET` | `/api/v1/categories` | Listado de categorías de hardware activas | 🚀 [Ir al Endpoint](#endpoint-1-1) |
| **1.2** | `GET` | `/api/v1/categories/{id}` | Detalle de categoría y subcategorías | 🚀 [Ir al Endpoint](#endpoint-1-2) |
| **2.1** | `GET` | `/api/v1/providers/me/products` | Catálogo de productos propio con paginación | 🚀 [Ir al Endpoint](#endpoint-2-1) |
| **2.2** | `GET` | `/api/v1/providers/me/products/{id}` | Ficha técnica de producto (specs + fotos) | 🚀 [Ir al Endpoint](#endpoint-2-2) |
| **2.3** | `POST` | `/api/v1/providers/me/products` | **Crear nuevo producto (CRUD Core)** | 🚀 [Ir al Endpoint](#endpoint-2-3) |
| **2.4** | `PUT` | `/api/v1/providers/me/products/{id}` | **Actualizar producto comercial/técnico** | 🚀 [Ir al Endpoint](#endpoint-2-4) |
| **2.5** | `PATCH` | `/api/v1/providers/me/products/{id}/status` | Activar / Deshabilitar producto | 🚀 [Ir al Endpoint](#endpoint-2-5) |
| **2.6** | `DELETE` | `/api/v1/providers/me/products/{id}` | **Eliminación lógica de producto** | 🚀 [Ir al Endpoint](#endpoint-2-6) |

---

## 📋 Resumen de Endpoints del Primer Avance

### 🔐 0. Autenticación, Sesión y Seguridad del Proveedor
| Nº | Método | Endpoint RESTful | Descripción del Endpoint | Operación | Vista Frontend Origen |
|:---:|:---:|---|---|:---:|---|
| **0.1** | `POST` | [`/api/v1/auth/login`](#endpoint-0-1) | Autenticación con credenciales, emite token JWT | **AUTH** | `LoginView` (Acceso al Portal) |
| **0.2** | `GET` | [`/api/v1/auth/me`](#endpoint-0-2) | Validar token y rehidratar sesión activa al recargar (F5) | **READ** | `AuthContext` (Persistencia) |
| **0.3** | `PATCH` | [`/api/v1/auth/password`](#endpoint-0-3) | Cambio de contraseña corporativa de la cuenta | **UPDATE** | `ProviderAccountView` |

### 📦 1. Categorías del Sistema
| Nº | Método | Endpoint RESTful | Descripción del Endpoint | Operación | Vista Frontend Origen |
|:---:|:---:|---|---|:---:|---|
| **1.1** | `GET` | [`/api/v1/categories`](#endpoint-1-1) | Listar familias de hardware activas para selectores | **READ** | `CreateEditProductView` |
| **1.2** | `GET` | [`/api/v1/categories/{id}`](#endpoint-1-2) | Detalle de categoría con sus subcategorías técnicas | **READ** | `ProductManagementView` |

### ⚙️ 2. Catálogo de Productos y Hardware B2B (CRUD Core)
| Nº | Método | Endpoint RESTful | Descripción del Endpoint | Operación | Vista Frontend Origen |
|:---:|:---:|---|---|:---:|---|
| **2.1** | `GET` | [`/api/v1/providers/me/products`](#endpoint-2-1) | Catálogo propio con filtros (`search`, `category`, `status`) | **READ** | `ProductManagementView` |
| **2.2** | `GET` | [`/api/v1/providers/me/products/{id}`](#endpoint-2-2) | Ficha técnica completa con especificaciones e imágenes | **READ** | `CreateEditProductView` |
| **2.3** | `POST` | [`/api/v1/providers/me/products`](#endpoint-2-3) | **Crear nuevo producto** (con specs e imágenes en cascada) | **CREATE** | `CreateEditProductView` |
| **2.4** | `PUT` | [`/api/v1/providers/me/products/{id}`](#endpoint-2-4) | **Actualizar datos comerciales y técnicos** | **UPDATE** | `CreateEditProductView` |
| **2.5** | `PATCH` | [`/api/v1/providers/me/products/{id}/status`](#endpoint-2-5) | Deshabilitar o activar publicación en catálogo público | **UPDATE** | `ProductManagementView` |
| **2.6** | `DELETE` | [`/api/v1/providers/me/products/{id}`](#endpoint-2-6) | **Eliminación lógica** del producto del catálogo | **DELETE** | `ProductManagementView` |

---

## 🏛️ ESPECIFICACIÓN DETALLADA TRILATERAL & CICLO TDD

---

### SECCIÓN 0: AUTENTICACIÓN, SESIÓN Y SEGURIDAD

---

<a id="endpoint-0-1"></a>
### [0.1] Inicio de Sesión y Emisión de Token JWT (Login)

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `LoginView.jsx` (Formulario de acceso para Proveedores).
* **Endpoint REST al que dispara:** `POST http://localhost:8080/api/v1/auth/login`
* **Headers Requeridos:** `Content-Type: application/json`
* **JSON a Enviar (Request Body):**
  ```json
  {
    "correo": "ventas.mayoristas@technova.com",
    "contrasena": "PasswordMayorista123!"
  }
  ```
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2ZW50YXMubWF5b3Jpc3Rhc0B0ZWNobm92YS5jb20iLCJyb2wiOiJQUk9WRUVET1IiLCJpYXQiOjE3MjUyODAwMDAsImV4cCI6MTcyNTM2NjQwMH0...",
    "tipoToken": "Bearer",
    "expiraEnSegundos": 86400
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.auth.controller.AuthController`
  * 📥 **DTO de Entrada:** `com.compunex.b2b.modules.auth.dto.request.LoginRequestDTO`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.auth.dto.response.AuthResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.auth.service.AuthService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.auth.service.impl.AuthServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.auth.repository.UsuarioRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.auth.entity.Usuario`
    * `com.compunex.b2b.modules.auth.entity.RolUsuario`
    * `com.compunex.b2b.modules.auth.entity.EstadoUsuario`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.auth.service.AuthServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.auth.controller.AuthControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `AuthServiceTest.java`: `@Test void debeEmitirTokenJwtCuandoCredencialesSonValidas();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `AuthServiceImpl.java` se codifica la autenticación con `AuthenticationManager` y emisión de token con `JwtTokenProvider`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `AuthControllerTest.java` se verifica retorno HTTP 200 y formato de `AuthResponseDTO`.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `POST` | **URL:** `http://localhost:8080/api/v1/auth/login`
* **Headers:** `Content-Type: application/json`
* **Body:** JSON con `correo` y `contrasena`.
* **Response (200 OK):** JSON con `token`, `tipoToken` y `expiraEnSegundos`.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y token JWT generado.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-0-2"></a>
### [0.2] Rehidratación de Sesión del Proveedor (Validar Token)

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `AuthContext.jsx` (disparado automáticamente al recargar con F5).
* **Endpoint REST al que dispara:** `GET http://localhost:8080/api/v1/auth/me`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`
* **JSON a Enviar (Request Body):** *No aplica (Petición GET sin cuerpo).*
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "correo": "ventas.mayoristas@technova.com",
    "nombre": "TechNova Mayorista S.A.C.",
    "rol": "PROVEEDOR",
    "valido": true
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.auth.controller.AuthController`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.auth.dto.response.UsuarioSesionDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.auth.service.AuthService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.auth.service.impl.AuthServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.auth.repository.UsuarioRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.auth.entity.Usuario`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.auth.service.AuthServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.auth.controller.AuthControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `AuthServiceTest.java`: `@Test void debeRetornarDatosSesionCuandoTokenEsValido();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `AuthServiceImpl.java` se resuelve el usuario activo desde el `SecurityContextHolder`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `AuthControllerTest.java` se valida retorno HTTP 200 con payload `UsuarioSesionDTO`.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `GET` | **URL:** `http://localhost:8080/api/v1/auth/me`
* **Headers:** `Authorization: Bearer {{token}}`
* **Response (200 OK):** JSON con datos de la sesión rehidratada.
* 📸 **EVIDENCIA WORD:** *Captura de Postman con status 200 OK y correo del usuario.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-0-3"></a>
### [0.3] Cambio de Contraseña Corporativa

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `ProviderAccountView.jsx` (Pestaña "Seguridad y Contraseña").
* **Endpoint REST al que dispara:** `PATCH http://localhost:8080/api/v1/auth/password`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **JSON a Enviar (Request Body):**
  ```json
  {
    "contrasenaActual": "PasswordMayorista123!",
    "nuevaContrasena": "NuevaClaveCorporativa2026*"
  }
  ```
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "mensaje": "Contraseña actualizada exitosamente",
    "fechaActualizacion": "2026-09-02T16:00:00Z"
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.auth.controller.AuthController`
  * 📥 **DTO de Entrada:** `com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.auth.service.AuthService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.auth.service.impl.AuthServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.auth.repository.UsuarioRepository`
  * 🏛️ **Entidad JPA:** `com.compunex.b2b.modules.auth.entity.Usuario`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.auth.service.AuthServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.auth.controller.AuthControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `AuthServiceTest.java`: `@Test void debeActualizarPasswordCuandoClaveActualEsCorrecta();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `AuthServiceImpl.java` se verifica la clave actual con BCryptPasswordEncoder y se guarda el nuevo hash.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `AuthControllerTest.java` se verifica retorno HTTP 200 con mensaje de éxito.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `PATCH` | **URL:** `http://localhost:8080/api/v1/auth/password`
* **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **Body:** JSON con `contrasenaActual` y `nuevaContrasena`.
* **Response (200 OK):** JSON con confirmación de actualización.
* 📸 **EVIDENCIA WORD:** *Captura de Postman con status 200 OK y mensaje de éxito.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

### SECCIÓN 1: CATEGORÍAS DEL SISTEMA

---

<a id="endpoint-1-1"></a>
### [1.1] Listar Categorías Activas

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `CreateEditProductView.jsx` (Selector de Categoría).
* **Endpoint REST al que dispara:** `GET http://localhost:8080/api/v1/categories`
* **Headers Requeridos:** Ninguno (Endpoint público de lectura).
* **JSON a Enviar (Request Body):** *No aplica (Petición GET sin cuerpo).*
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  [
    {
      "id": "ram",
      "nombre": "Memorias RAM",
      "icono": "memory",
      "descripcion": "Módulos de memoria DDR4 y DDR5 para PC y Servidores",
      "estado": "ACTIVA",
      "totalProductos": 45
    },
    {
      "id": "ssd",
      "nombre": "Almacenamiento SSD",
      "icono": "storage",
      "descripcion": "Unidades de estado sólido NVMe M.2 y SATA III",
      "estado": "ACTIVA",
      "totalProductos": 38
    }
  ]
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.CategoriaController`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.CategoriaResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.CategoriaService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.CategoriaServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.CategoriaRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Categoria`
    * `com.compunex.b2b.modules.catalogo.entity.EstadoCategoria`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeListarTodasLasCategoriasActivas();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `CategoriaServiceImpl.java` se consulta `categoriaRepository.findByEstado(EstadoCategoria.ACTIVA)` y se mapea a DTOs.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se valida retorno HTTP 200 y arreglo JSON.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `GET` | **URL:** `http://localhost:8080/api/v1/categories`
* **Response (200 OK):** Arreglo JSON de categorías activas.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y listado de familias de hardware.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-1-2"></a>
### [1.2] Detalle de Categoría y Subcategorías

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `ProductManagementView.jsx` (Filtro desplegable de subcategorías técnicas).
* **Endpoint REST al que dispara:** `GET http://localhost:8080/api/v1/categories/{id}`
* **Headers Requeridos:** Ninguno (Endpoint público de lectura).
* **JSON a Enviar (Request Body):** *No aplica (Petición GET sin cuerpo, ID en PathVariable).*
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "id": "ram",
    "nombre": "Memorias RAM",
    "icono": "memory",
    "descripcion": "Módulos de memoria DDR4 y DDR5 para PC y Servidores",
    "estado": "ACTIVA",
    "totalProductos": 45,
    "subcategorias": [
      { "id": 1, "nombre": "DDR5 Desktop" },
      { "id": 2, "nombre": "DDR4 Desktop" },
      { "id": 3, "nombre": "DDR5 SO-DIMM Laptop" }
    ]
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.CategoriaController`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.CategoriaDetalleResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.CategoriaService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.CategoriaServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.CategoriaRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Categoria`
    * `com.compunex.b2b.modules.catalogo.entity.Subcategoria`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeObtenerCategoriaConSubcategoriasPorId();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `CategoriaServiceImpl.java` se obtiene la categoría y se cargan sus subcategorías asociadas.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se verifica retorno HTTP 200 con subcategorías.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `GET` | **URL:** `http://localhost:8080/api/v1/categories/ram`
* **Response (200 OK):** Objeto JSON con detalle y subcategorías.
* 📸 **EVIDENCIA WORD:** *Captura de Postman con status 200 OK y las subcategorías DDR4/DDR5.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

### SECCIÓN 2: GESTIÓN DE PRODUCTOS Y HARDWARE (CRUD CORE)

---

<a id="endpoint-2-1"></a>
### [2.1] Listar Catálogo Propio con Filtros y Paginación

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `ProductManagementView.jsx` (Tabla principal de inventario mayorista).
* **Endpoint REST al que dispara:** `GET http://localhost:8080/api/v1/providers/me/products?page=0&size=10&search=Kingston&category=ram`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`
* **JSON a Enviar (Request Body):** *No aplica (Petición GET con parámetros de consulta: `page`, `size`, `search`, `category`, `status`).*
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "contenido": [
      {
        "id": 1,
        "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
        "categoriaId": "ram",
        "categoriaNombre": "Memorias RAM",
        "modeloComercial": "DISTRIBUIDOR_OFICIAL",
        "pedidoMinimo": "10 unidades",
        "precioUnitarioRef": 52.50,
        "precioTotalRef": 525.00,
        "moneda": "USD",
        "estado": "ACTIVO",
        "esRecomendado": true,
        "esPromocionado": false,
        "urlImagenPrincipal": "https://img.compunex.com/products/ram-kingston-16gb.jpg",
        "fechaPublicacion": "2026-09-02T10:30:00Z"
      }
    ],
    "pagina": 0,
    "tamano": 10,
    "totalElementos": 1,
    "totalPaginas": 1,
    "esUltima": true
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.ProductoResumenResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Producto`
    * `com.compunex.b2b.modules.catalogo.entity.EstadoProducto`
    * `com.compunex.b2b.modules.perfiles.entity.PerfilProveedor`
  * 🧪 **Pruebas TDD:**
    * `com.compunex.b2b.modules.catalogo.repository.ProductoRepositoryTest` (`@DataJpaTest`)
    * `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest` (Mockito)
    * `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest` (`@WebMvcTest`)

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoRepositoryTest.java`: `@Test void debeBuscarProductosPorProveedorYPaginacion();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se aplica `productoRepository.findAllByProveedorId(...)` con `Pageable`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se valida retorno HTTP 200 y campos paginados.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `GET` | **URL:** `http://localhost:8080/api/v1/providers/me/products?page=0&size=10`
* **Headers:** `Authorization: Bearer {{token}}`
* **Response (200 OK):** Paginación JSON con lista de productos.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y el catálogo paginado.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-2-2"></a>
### [2.2] Detalle Completo de Ficha Técnica de Producto

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `CreateEditProductView.jsx` (Modo Edición / Visualización Técnica).
* **Endpoint REST al que dispara:** `GET http://localhost:8080/api/v1/providers/me/products/{id}`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`
* **JSON a Enviar (Request Body):** *No aplica (Petición GET sin cuerpo, ID en PathVariable).*
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "id": 1,
    "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "proveedorId": 1,
    "proveedorRazonSocial": "TechNova Mayorista S.A.C.",
    "categoriaId": "ram",
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
    "descripcion": "Módulo de memoria DDR5 de alto rendimiento optimizado para plataformas Intel XMP 3.0 y AMD EXPO.",
    "modeloComercial": "DISTRIBUIDOR_OFICIAL",
    "tipoFormato": "Caja Master x10 Blísteres",
    "unidadesPorPaquete": "10 unidades",
    "pedidoMinimo": "1 caja (10 unidades)",
    "precioUnitarioRef": 52.50,
    "precioTotalRef": 525.00,
    "moneda": "USD",
    "terminosComerciales": "Garantía de 3 años directa con fabricante.",
    "estado": "ACTIVO",
    "motivoModeracion": null,
    "esRecomendado": true,
    "esPromocionado": false,
    "tieneOferta": false,
    "especificaciones": [
      { "clave": "Capacidad", "valor": "16 GB" },
      { "clave": "Frecuencia", "valor": "5600 MHz" },
      { "clave": "Latencia CAS", "valor": "CL36" },
      { "clave": "Voltaje", "valor": "1.25V" }
    ],
    "imagenes": [
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-front.jpg", "orden": 1 },
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-angle.jpg", "orden": 2 }
    ],
    "fechaPublicacion": "2026-09-02T10:30:00Z",
    "fechaActualizacion": "2026-09-02T10:30:00Z"
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Producto`
    * `com.compunex.b2b.modules.catalogo.entity.ProductoEspecificacion`
    * `com.compunex.b2b.modules.catalogo.entity.ImagenProducto`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeRetornarFichaTecnicaCompletaConEspecificacionesEImagenes();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se recupera la entidad `Producto` con sus colecciones hijas (`specs` y `fotos`) y se mapea al DTO.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se verifica retorno HTTP 200 y campos técnicos.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `GET` | **URL:** `http://localhost:8080/api/v1/providers/me/products/1`
* **Headers:** `Authorization: Bearer {{token}}`
* **Response (200 OK):** Ficha técnica en JSON con colecciones de fotos y specs.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y la ficha técnica.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-2-3"></a>
### [2.3] Crear Nuevo Producto en Catálogo (CRUD Core)

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `CreateEditProductView.jsx` (Botón "Publicar Producto").
* **Endpoint REST al que dispara:** `POST http://localhost:8080/api/v1/providers/me/products`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **JSON a Enviar (Request Body):**
  ```json
  {
    "categoriaId": "ram",
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
    "descripcion": "Módulo de memoria DDR5 de alto rendimiento optimizado para plataformas Intel XMP 3.0 y AMD EXPO. Ideal para ensamblajes corporativos y gaming de gama alta.",
    "modeloComercial": "DISTRIBUIDOR_OFICIAL",
    "tipoFormato": "Caja Master x10 Blísteres",
    "unidadesPorPaquete": "10 unidades",
    "pedidoMinimo": "1 caja (10 unidades)",
    "precioUnitarioRef": 52.50,
    "precioTotalRef": 525.00,
    "moneda": "USD",
    "terminosComerciales": "Garantía de 3 años directa con fabricante. Entrega en almacén Lima o envío a provincia.",
    "especificaciones": [
      { "clave": "Capacidad", "valor": "16 GB" },
      { "clave": "Frecuencia", "valor": "5600 MHz" },
      { "clave": "Latencia CAS", "valor": "CL36" },
      { "clave": "Voltaje", "valor": "1.25V" }
    ],
    "imagenes": [
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-front.jpg", "orden": 1 },
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-angle.jpg", "orden": 2 }
    ]
  }
  ```
* **JSON a Recibir (Response Body 201 Created):**
  ```json
  {
    "id": 1,
    "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "proveedorId": 1,
    "categoriaId": "ram",
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
    "precioUnitarioRef": 52.50,
    "precioTotalRef": 525.00,
    "moneda": "USD",
    "estado": "ACTIVO",
    "fechaPublicacion": "2026-09-02T10:30:00Z"
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * 📥 **DTO de Entrada:** `com.compunex.b2b.modules.catalogo.dto.request.CrearProductoRequestDTO`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorios JPA:** 
    * `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
    * `com.compunex.b2b.modules.catalogo.repository.CategoriaRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Producto`
    * `com.compunex.b2b.modules.catalogo.entity.Categoria`
    * `com.compunex.b2b.modules.catalogo.entity.ProductoEspecificacion`
    * `com.compunex.b2b.modules.catalogo.entity.ImagenProducto`
    * `com.compunex.b2b.modules.catalogo.entity.ModeloComercial`
    * `com.compunex.b2b.modules.catalogo.entity.EstadoProducto`
    * `com.compunex.b2b.modules.perfiles.entity.PerfilProveedor`
  * 🧪 **Pruebas TDD:**
    * `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest` (Mockito)
    * `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest` (`@WebMvcTest`)

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeGuardarProductoConRelacionesEnCascada();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se ensambla la entidad `Producto` con sus entidades hijas (`ProductoEspecificacion` e `ImagenProducto`) asociando `producto_id` en cascada (`CascadeType.ALL`) y persistiendo en `productoRepository.save()`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se verifica retorno HTTP 201 Created y presencia de `id` autogenerado.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `POST` | **URL:** `http://localhost:8080/api/v1/providers/me/products`
* **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **Body:** JSON con payload completo (título, precio, especificaciones e imágenes).
* **Response (201 Created):** Objeto JSON del producto guardado con sus tablas hijas asociadas.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 201 Created.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-2-4"></a>
### [2.4] Actualizar Producto Existente

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `CreateEditProductView.jsx` (Botón "Guardar Cambios").
* **Endpoint REST al que dispara:** `PUT http://localhost:8080/api/v1/providers/me/products/{id}`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **JSON a Enviar (Request Body):**
  ```json
  {
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)",
    "descripcion": "Descripción actualizada con nuevo stock certificado y empaque sellado.",
    "pedidoMinimo": "5 unidades",
    "precioUnitarioRef": 49.90,
    "precioTotalRef": 249.50,
    "terminosComerciales": "Precio rebajado por compra directa a partir de 5 piezas.",
    "especificaciones": [
      { "clave": "Capacidad", "valor": "16 GB" },
      { "clave": "Frecuencia", "valor": "5600 MHz" },
      { "clave": "Disipador", "valor": "Aluminio Negro Anodizado" }
    ],
    "imagenes": [
      { "urlImagen": "https://img.compunex.com/products/ram-kingston-16gb-new.jpg", "orden": 1 }
    ]
  }
  ```
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "id": 1,
    "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "titulo": "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)",
    "precioUnitarioRef": 49.90,
    "precioTotalRef": 249.50,
    "estado": "ACTIVO",
    "fechaActualizacion": "2026-09-02T11:00:00Z"
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * 📥 **DTO de Entrada:** `com.compunex.b2b.modules.catalogo.dto.request.ActualizarProductoRequestDTO`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
  * 🏛️ **Entidad JPA:** `com.compunex.b2b.modules.catalogo.entity.Producto`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeActualizarDatosComercialesDelProducto();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se localiza el producto existente, se actualizan sus atributos y listas hijas, y se guarda en base de datos.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se verifica retorno HTTP 200 y datos modificados.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `PUT` | **URL:** `http://localhost:8080/api/v1/providers/me/products/1`
* **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **Body:** JSON con datos comerciales y técnicos actualizados.
* **Response (200 OK):** Objeto JSON reflejando los cambios.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y precios actualizados.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-2-5"></a>
### [2.5] Cambiar Estado de Publicación (Desactivar / Activar)

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `ProductManagementView.jsx` (Switch / Toggle Activar/Desactivar en la tabla).
* **Endpoint REST al que dispara:** `PATCH http://localhost:8080/api/v1/providers/me/products/{id}/status`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **JSON a Enviar (Request Body):**
  ```json
  {
    "estado": "DESHABILITADO_POR_PROVEEDOR"
  }
  ```
* **JSON a Recibir (Response Body 200 OK):**
  ```json
  {
    "id": 1,
    "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "estadoAnterior": "ACTIVO",
    "nuevoEstado": "DESHABILITADO_POR_PROVEEDOR",
    "mensaje": "El producto ha sido deshabilitado del catálogo mayorista temporalmente",
    "fechaActualizacion": "2026-09-02T11:15:00Z"
  }
  ```

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * 📥 **DTO de Entrada:** `com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO`
  * 📤 **DTO de Salida:** `com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Producto`
    * `com.compunex.b2b.modules.catalogo.entity.EstadoProducto`
  * 🧪 **Prueba Unitaria (TDD Service):** `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
  * 🧪 **Prueba Web (TDD Controller):** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeCambiarEstadoDePublicacionDelProducto();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se actualiza únicamente el campo `estado` y se registra `fechaActualizacion`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se valida retorno HTTP 200 y mensaje en `CambioEstadoResponseDTO`.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `PATCH` | **URL:** `http://localhost:8080/api/v1/providers/me/products/1/status`
* **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
* **Body:** JSON con el nuevo estado (`DESHABILITADO_POR_PROVEEDOR` o `ACTIVO`).
* **Response (200 OK):** JSON confirmando el cambio de estado.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 200 OK y el nuevo estado reflejado.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

<a id="endpoint-2-6"></a>
### [2.6] Eliminación Lógica de Producto (Soft Delete)

#### 💻 1. CONTRATO FRONTEND
* **Vista / Componente:** `ProductManagementView.jsx` (Modal de Confirmación "Eliminar Producto").
* **Endpoint REST al que dispara:** `DELETE http://localhost:8080/api/v1/providers/me/products/{id}`
* **Headers Requeridos:** `Authorization: Bearer {{token}}`
* **JSON a Enviar (Request Body):** *No aplica (Petición DELETE sin cuerpo, ID en PathVariable).*
* **JSON a Recibir (Response Body 204 No Content):** *No aplica (Cuerpo vacío con HTTP Status 204).*

#### ☕ 2. CONTRATO BACKEND ORQUESTADOR
* **Clases Involucradas y Paquetes:**
  * 🎮 **Controlador:** `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorController`
  * ⚙️ **Servicio (Interfaz):** `com.compunex.b2b.modules.catalogo.service.ProductoService`
  * 🔧 **Servicio (Implementación):** `com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl`
  * 🗄️ **Repositorio JPA:** `com.compunex.b2b.modules.catalogo.repository.ProductoRepository`
  * 🏛️ **Entidades JPA:** 
    * `com.compunex.b2b.modules.catalogo.entity.Producto`
    * `com.compunex.b2b.modules.catalogo.entity.EstadoProducto`
  * 🧪 **Pruebas TDD:**
    * `com.compunex.b2b.modules.catalogo.service.ProductoServiceTest`
    * `com.compunex.b2b.modules.catalogo.controller.ProductoProveedorControllerTest`

#### 🧪 3. CICLO TDD & EVIDENCIAS PARA EL WORD
* 🔴 **Fase 1: RED:** En `ProductoServiceTest.java`: `@Test void debeEjecutarEliminacionLogicaSinBorrarRegistroFisico();` (falla en rojo).
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **ROJO**.*
* 🟢 **Fase 2: GREEN:** En `ProductoServiceImpl.java` se establece `estado = EstadoProducto.ELIMINADO_LOGICO` y se sella `fechaEliminacion = Instant.now()`.
  * 📸 **EVIDENCIA WORD:** *Captura de pantalla de JUnit en **VERDE**.*
* 🔵 **Fase 3: REFACTOR:** En `ProductoProveedorControllerTest.java` se verifica retorno HTTP 204 No Content.
  * 📸 **EVIDENCIA WORD:** *Captura del test de controlador en **VERDE**.*

#### 🚀 4. VERIFICACIÓN EN POSTMAN
* **Método:** `DELETE` | **URL:** `http://localhost:8080/api/v1/providers/me/products/1`
* **Headers:** `Authorization: Bearer {{token}}`
* **Response (204 No Content):** Sin cuerpo de respuesta.
* 📸 **EVIDENCIA WORD:** *Captura de Postman mostrando status 204 No Content.*

[⬆️ Volver al Índice de Endpoints](#indice-rapido)
---

## 🛡️ Checklist de Validación (Cumplimiento de la Rúbrica 20/20)

- [x] **Modelado JPA >= 6 tablas relacionadas:** `Usuario`, `PerfilProveedor`, `Categoria`, `Subcategoria`, `Producto`, `ProductoEspecificacion`, `ImagenProducto` (7 tablas).
- [x] **CRUD Completo:** Listar, Obtener detalle, Crear, Actualizar, Modificar Estado y Eliminación Lógica.
- [x] **Seguridad JWT:** Emisión y validación sin exponer `id` de usuario.
- [x] **TDD 3 Fases:** RED ➔ GREEN ➔ REFACTOR documentado con capturas para el Word en cada endpoint.
- [x] **Postman:** Request, Response y Status HTTP para el informe de laboratorio.
