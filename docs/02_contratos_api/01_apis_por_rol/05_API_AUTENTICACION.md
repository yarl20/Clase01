# 05. API de Autenticación y Autorización

> Plataforma COMPUNEX B2B — Endpoints de Seguridad, Sesiones y Roles — PostgreSQL

---

## Estrategia de Autenticación

- **Mecanismo:** JSON Web Token (JWT)
- **Tipo de token:** Bearer Token en Header `Authorization: Bearer <token>`
- **Expiración:** Configurable (recomendado: 24h para access token)
- **Algoritmo de cifrado de contraseñas:** BCrypt (factor 12)
- **Control de acceso:** RBAC — Role-Based Access Control con 3 roles: `ADMIN`, `PROVEEDOR`, `CLIENTE` (tipos PostgreSQL `rol_usuario`)

---

## Endpoints de Autenticación

---

### POST /api/auth/register

**Descripción:** Registro libre de nuevos Clientes. Solo disponible para el rol CLIENTE.

**Acceso:** Público (sin autenticación)

**Request Body:**
```json
{
  "nombre": "Carlos Mendoza R.",
  "correo": "compras@tecnostore.com",
  "contrasena": "MiPassword123!",
  "nombre_tienda": "TecnoStore Express",
  "telefono": "+51 987 654 321",
  "ciudad": "Arequipa",
  "direccion": "Av. Comercial 740, Tienda 12"
}
```

**Respuesta HTTP 201:**
```json
{
  "id": "uuid-del-usuario",
  "nombre": "Carlos Mendoza R.",
  "correo": "compras@tecnostore.com",
  "rol": "CLIENTE",
  "nombre_tienda": "TecnoStore Express",
  "mensaje": "Cuenta creada exitosamente."
}
```

**Respuesta HTTP 400:** Campos requeridos ausentes o inválidos
**Respuesta HTTP 409:** El correo ya está registrado

**Validaciones:**
- correo: formato válido, único en usuarios.correo
- contrasena: mínimo 8 caracteres
- nombre: no vacío, máximo 200 chars
- nombre_tienda: no vacío

**Efecto en PostgreSQL:**
- Inserta en `usuarios` (rol=CLIENTE, estado=ACTIVO)
- Inserta en `perfiles_cliente` (usuario_id)
- Inserta en `preferencias_cliente` (perfil_cliente_id) — vacía por defecto

---

### POST /api/auth/login

**Descripción:** Inicio de sesión para todos los roles. Retorna JWT.

**Acceso:** Público

**Request Body:**
```json
{
  "correo": "usuario@ejemplo.com",
  "contrasena": "miPassword123"
}
```

**Respuesta HTTP 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "usuario": {
    "id": "uuid",
    "nombre": "Nombre del Usuario",
    "correo": "usuario@ejemplo.com",
    "rol": "CLIENTE | PROVEEDOR | ADMIN"
  }
}
```

**Respuesta HTTP 401:** Credenciales inválidas
**Respuesta HTTP 403:** Cuenta bloqueada o inactiva

**Lógica PostgreSQL:**
1. Buscar en `usuarios` por correo WHERE estado='ACTIVO'
2. Comparar hash_contrasena con BCrypt
3. Generar JWT con rol, usuario_id, uuid
4. Actualizar `usuarios.ultimo_acceso = NOW()`

**Payload del JWT:**
```json
{
  "sub": "uuid-del-usuario",
  "usuarioId": 123,
  "rol": "CLIENTE",
  "correo": "usuario@ejemplo.com",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

### POST /api/auth/logout

**Descripción:** Cierre de sesión. Con JWT stateless, el frontend elimina el token. Si hay blacklist, se invalida en servidor (tabla opcional `tokens_invalidados`).

**Acceso:** Autenticado (cualquier rol)

**Response HTTP 204:** Sin contenido

---

### PUT /api/auth/cambiar-contrasena

**Descripción:** Cambio de contraseña para cualquier usuario autenticado.

**Acceso:** ADMIN | PROVEEDOR | CLIENTE

**Request Body:**
```json
{
  "contrasenaActual": "MiPasswordActual",
  "contrasenaNueva": "MiNuevaPassword456!"
}
```

**Response HTTP 204:** Contraseña actualizada

**Response HTTP 400:**
```json
{ "error": "La contraseña actual es incorrecta." }
```

---

### GET /api/auth/yo

**Descripción:** Obtiene los datos básicos del usuario actualmente autenticado.

**Acceso:** Autenticado (cualquier rol)

**Response HTTP 200:**
```json
{
  "id": "uuid",
  "nombre": "Nombre",
  "correo": "correo@ejemplo.com",
  "rol": "CLIENTE",
  "estado": "ACTIVO"
}
```

---

## Control de Acceso por Endpoint (PostgreSQL RBAC)

| Rol | Descripción |
|---|---|
| `PUBLIC` | No requiere autenticación |
| `CLIENTE` | Requiere JWT con rol=CLIENTE |
| `PROVEEDOR` | Requiere JWT con rol=PROVEEDOR |
| `ADMIN` | Requiere JWT con rol=ADMIN |
| `CLIENTE\|PROVEEDOR` | Requiere JWT con rol=CLIENTE o PROVEEDOR |

### Protección de Recursos Propios

- Un CLIENTE solo puede ver/editar SU propio perfil, SUS propias preferencias y SUS propias conversaciones (WHERE perfiles_cliente.usuario_id = auth.usuario_id).
- Un PROVEEDOR solo puede ver/editar SUS propios productos, SUS propias ofertas y SUS propias campañas (WHERE productos.proveedor_id = auth.proveedor_id).
- Un PROVEEDOR no puede acceder a recursos de otro proveedor.
- Un ADMIN puede acceder a todos los recursos.

**Mecanismo de verificación en backend (Spring Boot + PostgreSQL):**
```java
if (!producto.getProveedorId().equals(authProveedorId)) {
    throw new ForbiddenException("No tienes permisos sobre este producto.");
}
```

---

## Tabla Resumen de Autenticación de Endpoints

| Endpoint | Método | Acceso |
|---|---|---|
| /api/auth/register | POST | PUBLIC |
| /api/auth/login | POST | PUBLIC |
| /api/auth/logout | POST | Autenticado |
| /api/auth/cambiar-contrasena | PUT | Autenticado |
| /api/auth/yo | GET | Autenticado |
| /api/content/about | GET | PUBLIC |
| /api/content/contact | GET | PUBLIC |
| /api/categorias | GET | PUBLIC |
| /api/productos | GET | PUBLIC |
| /api/productos/{id} | GET | PUBLIC |
| /api/proveedores/{id}/perfil-publico | GET | PUBLIC |
| /api/solicitudes/proveedor | POST | PUBLIC |
| /api/home/contenido-destacado | GET | PUBLIC |
| /api/clientes/yo/** | GET/PUT | CLIENTE |
| /api/proveedores/yo/** | GET/PUT/POST/DELETE | PROVEEDOR |
| /api/chat/conversaciones | GET/POST | CLIENTE\|PROVEEDOR |
| /api/admin/** | ALL | ADMIN |

---

## Manejo de Errores de Autenticación

| Código HTTP | Situación |
|---|---|
| 401 Unauthorized | Token ausente, expirado o inválido |
| 403 Forbidden | Token válido pero sin permisos para esa operación |
| 400 Bad Request | Campos inválidos en login/registro |
| 409 Conflict | Correo duplicado en registro (usuarios.correo UNIQUE) |
