# 📖 Guía de Endpoints, Roles y Pruebas — COMPUNEX B2B

Este documento detalla los endpoints REST, roles de acceso, credenciales de desarrollo y el procedimiento para probar la API desde **Swagger UI** o **Postman**.

---

## 🔐 1. Credenciales de Prueba (Desarrollo)

Ambas cuentas son sembradas automáticamente por `DataInitializer.java` al iniciar la aplicación si no existen en la base de datos:

| Rol | Correo Electrónico | Contraseña | Propósito |
|---|---|---|---|
| **`PROVEEDOR`** | `ventas.mayoristas@technova.com` | `PasswordMayorista123!` | Probar CRUD de productos propios, ficha técnica y filtros |
| **`ADMIN`** | `admin@compunex.com` | `AdminPassword2026!` | Probar endpoints administrativos globales y cambio de estado |

*(Las contraseñas se almacenan con hash criptográfico BCrypt).*

---

## 🚦 2. Clasificación de Endpoints y Matriz de Acceso

### A. Endpoints Públicos (Sin Token)
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Inicio de sesión. Emite token JWT Bearer (24h de vigencia). |
| `GET` | `/api/v1/categories` | Listado general de categorías activas con total de productos. |
| `GET` | `/api/v1/categories/{id}` | Detalle de categoría con subcategorías técnicas. |
| `GET` | `/swagger-ui/**`, `/swagger-ui.html`, `/api-docs/**` | Documentación interactiva Swagger / OpenAPI. |

### B. Endpoints para Cualquier Usuario Autenticado (Cualquier Rol)
| Método | Endpoint | Cabecera Requerida | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/auth/me` | `Authorization: Bearer <token>` | Rehidrata la sesión activa (correo, nombre, rol). |
| `PATCH` | `/api/v1/auth/password` | `Authorization: Bearer <token>` | Cambio de contraseña validando la clave anterior con BCrypt. |

### C. Endpoints Exclusivos del Proveedor (Rol `PROVEEDOR`)
| Método | Endpoint | Cabecera Requerida | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/providers/me/products` | `Authorization: Bearer <token>` | Listado paginado de productos propios. Soporta `?search=texto&page=0&size=10`. |
| `GET` | `/api/v1/providers/me/products/{id}` | `Authorization: Bearer <token>` | Ficha técnica y detalle completo de un producto propio. |
| `POST` | `/api/v1/providers/me/products` | `Authorization: Bearer <token>` | Creación atómica de producto con especificaciones e imágenes (`201 Created`). |
| `PUT` | `/api/v1/providers/me/products/{id}` | `Authorization: Bearer <token>` | Actualización completa de producto y relaciones en cascada. |
| `PATCH` | `/api/v1/providers/me/products/{id}/status` | `Authorization: Bearer <token>` | Activar / desactivar estado de publicación de producto propio. |
| `DELETE` | `/api/v1/providers/me/products/{id}` | `Authorization: Bearer <token>` | Eliminación lógica de producto propio (`204 No Content`). |

> 🔒 **Seguridad Multi-Tenant:** La capa de servicio verifica mediante `findByIdAndProveedorId` que el producto pertenezca al proveedor autenticado. Si un proveedor intenta acceder o modificar el ID de otro proveedor, recibe `404 Not Found`.

### D. Endpoints Exclusivos del Administrador (Rol `ADMIN`)
| Método | Endpoint | Cabecera Requerida | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/admin/products` | `Authorization: Bearer <token>` | Consulta productos de todos los proveedores con paginación y `?search=texto`. |
| `PATCH` | `/api/v1/admin/products/{id}/status` | `Authorization: Bearer <token>` | Activa, desactiva u oculta cualquier producto sin restricción de propietario. |

---

## 🔍 3. Consultas JPQL y Búsqueda por Nombre (Sesión 7)

Se implementó la búsqueda personalizada con JPQL y `@Query`:

* **Proveedor:** `GET /api/v1/providers/me/products?search=Kingston&page=0&size=10`
  * Ejecuta la consulta JPQL:
    ```sql
    SELECT p FROM Producto p 
    WHERE p.proveedor.id = :proveedorId 
      AND p.estado <> :estado 
      AND LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%'))
    ```
* **Administrador:** `GET /api/v1/admin/products?search=RAM&page=0&size=10`
  * Ejecuta la consulta JPQL:
    ```sql
    SELECT p FROM Producto p 
    WHERE p.estado <> :estado 
      AND LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%'))
    ```

---

## 🔄 4. Transaccionalidad y Atomicidad (`@Transactional`)

* **`crearProducto`**: Si ocurre un error al persistir especificaciones o actualizar el contador de categoría, la transacción realiza Rollback y no deja registros parciales.
* **`actualizarProducto`**: La limpieza (`orphanRemoval`) y sustitución de especificaciones e imágenes se ejecuta dentro de una transacción atómica.
* **`cambiarEstadoPublicacionAdmin`**: Se ejecuta bajo `@Transactional` garantizando consistencia.

---

## 🧪 5. Cómo Probar el Flujo Completo

### A. Desde Swagger UI
1. Iniciar la aplicación (`CompunexBackendApplication.java` o `mvn spring-boot:run`).
2. Abrir en el navegador: `http://localhost:8080/swagger-ui.html`.
3. Ejecutar `POST /api/v1/auth/login` con credenciales de Proveedor o Administrador.
4. Copiar el valor del atributo `token` de la respuesta JSON (sin comillas ni prefijo Bearer).
5. Hacer clic en el botón verde **Authorize** en la parte superior derecha de Swagger UI.
6. Pegar el token en el campo `Value` y hacer clic en **Authorize**.
7. Ahora puedes ejecutar los endpoints protegidos correspondientes al rol de tu token.

### B. Desde Postman
1. Abrir Postman e importar la colección:  
   `compunex-backend/docs/02_contratos_api/01_apis_por_rol/001-Apis-Formalizado-En-desarrollo/COMPUNEX_B2B_Coleccion_Postman.json`
2. Ejecutar la petición `[0.1] Login y Emisión de Token JWT` (Proveedor) o `[0.1b] Login Administrador`.  
   *(El script de test guarda automáticamente la variable `token`).*
3. Ejecutar cualquiera de las peticiones protegidas; Postman enviará automáticamente la cabecera `Authorization: Bearer {{token}}`.

---

## 📊 6. Ejecución de Pruebas Automatizadas

Para ejecutar la batería completa de pruebas unitarias y de integración (usando base de datos en memoria H2):

```bash
cd compunex-backend
.\mvnw test
```

**Resultado:** 53 pruebas ejecutadas, 0 fallos, 0 errores.
