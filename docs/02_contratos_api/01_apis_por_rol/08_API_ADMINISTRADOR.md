# 08. API del Administrador

> Plataforma COMPUNEX B2B — Endpoints del Rol ADMIN — PostgreSQL
> Fuente única: `git/diseno_administrador/src/components/views/*` + `git/diseno_administrador/src/context/AdminContext.jsx` + `git/diseno_administrador/src/data/adminMockData.js`
> No se inventan endpoints fuera de las vistas existentes. Todo endpoint corresponde a una acción visible en la demo del Administrador.

---

## Convenciones

- **Base:** `/api/admin` — Requiere `Authorization: Bearer <JWT>` con `rol=ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
- **Paginación:** `?page=0&size=12` (Spring Data Page). `?search=` hace `ILIKE` en PostgreSQL (`to_tsvector` + `ILIKE`).
- **Tablas PostgreSQL spanish:** `usuarios`, `perfiles_cliente`, `perfiles_proveedor`, `solicitudes_proveedor`, `productos`, `ofertas`, `categorias`, `subcategorias`, `promociones`, `planes_publicidad`, `contenido_plataforma`, `registros_auditoria`.

---

## C-01. Dashboard Principal

**Vista:** `AdminDashboardView.jsx:23`

### GET /api/admin/dashboard/stats

**Descripción:** KPIs del panel central. Lo que muestra `AdminDashboardView` en 4 cards + widget de solicitudes pendientes + distribución por categorías.

**Acceso:** ADMIN

**Response HTTP 200:**
```json
{
  "totalUsuarios": 12,
  "totalClientes": 4,
  "totalProveedores": 4,
  "solicitudesPendientes": 2,
  "totalProductos": 9,
  "productosActivos": 6,
  "productosOcultosPorAdmin": 1,
  "ofertasActivas": 2,
  "promocionesActivas": 3,
  "productosPorCategoria": [
    { "categoriaId": "ram", "nombre": "Memorias RAM", "total": 2 },
    { "categoriaId": "ssd", "nombre": "Discos SSD & NVMe", "total": 3 }
  ]
}
```

**Lógica PostgreSQL (solo datos existentes):**
```sql
SELECT COUNT(*) FROM usuarios;
SELECT COUNT(*) FROM usuarios WHERE rol='CLIENTE';
SELECT COUNT(*) FROM usuarios WHERE rol='PROVEEDOR';
SELECT COUNT(*) FROM solicitudes_proveedor WHERE estado='PENDIENTE';
SELECT COUNT(*) FROM productos; -- incluye ACTIVO, DESHABILITADO_POR_PROVEEDOR, OCULTO_POR_ADMIN (excluye ELIMINADO_LOGICO si se filtra)
SELECT COUNT(*) FROM ofertas WHERE estado='ACTIVA';
SELECT COUNT(*) FROM promociones WHERE estado='ACTIVA';
SELECT categoria_id, COUNT(*) FROM productos WHERE estado != 'ELIMINADO_LOGICO' GROUP BY categoria_id;
```

**Existencia en diseño:** `AdminContext.jsx:24` `users`, `applications`, `products`, `offers`, `promotions`, `categories`, `auditLogs` + `AdminDashboardView.jsx` KPIs. No se inventa métrica fuera de esas.

---

## C-02. Gestión Global de Usuarios

**Vistas:** `AdminUsersView.jsx` + `AdminContext.jsx:62` `toggleUserBlock`, `toggleUserStatus`, `updateUser`

### GET /api/admin/usuarios

**Descripción:** Tabla de TODOS los usuarios (clientes y proveedores) con búsqueda y filtros. Corresponde a `AdminUsersView` — columnas: nombre, correo, rol, entidad/empresa, ciudad, estado, fecha_registro.

**Query Params:**
- `search` (string, opcional) — busca en `usuarios.nombre ILIKE`, `usuarios.correo ILIKE`, `perfiles_cliente.nombre_tienda ILIKE`, `perfiles_proveedor.razon_social ILIKE`
- `rol` (CLIENTE | PROVEEDOR | ALL)
- `estado` (ACTIVO | INACTIVO | BLOQUEADO | ALL)
- `page`, `size`

**Response HTTP 200:** `Page<UsuarioAdminDTO>` — mapeo directo de `INITIAL_USERS:14` en `adminMockData.js` (ej: `id: usr_101`, `nombre: Carlos Mendoza R.`, `correo: compras@tecnostore-express.com`, `rolLabel: Cliente / Comprador`, `entityName: TecnoStore Express`, `ciudad: Arequipa`, `estado: ACTIVO`).

### GET /api/admin/usuarios/{usuarioId}

Retorna ficha completa (`AdminUsersView` modal Ver). Incluye `motivo_estado`, `fecha_registro`.

### PUT /api/admin/usuarios/{usuarioId}

**Body:** `{ nombre, entityName, correo, ciudad, motivo_estado/notas }` — lo que edita el modal Editar en `AdminUsersView`. **No incluye `taxId`** (eliminado). Solo `ciudad` y `correo` además de nombres.

**Response HTTP 200:** `UsuarioAdminDTO` actualizado.

### PATCH /api/admin/usuarios/{usuarioId}/estado

**Body:** `{ estado: "ACTIVO" | "INACTIVO" | "BLOQUEADO", motivo?: string }` — corresponde a botones Power (Activar/Desactivar) y Lock/Unlock (Bloquear/Desbloquear con motivo). Mapea a `usuarios.estado` y `motivo_estado`.

---

## C-03. Gestión de Clientes

**Vista:** `AdminClientsView.jsx`

### GET /api/admin/clientes

**Descripción:** Tabla solo CLIENTE. Columnas: `nombre_tienda`, `nombre` (titular), `correo`, `ciudad`, `estado`, `fecha_registro`. Búsqueda por `nombre_tienda`, `nombre`, `ciudad`.

**Query Params:** `search`, `estado`, `page`, `size`

### GET /api/admin/clientes/{clienteId}

Ficha `perfiles_cliente` + `usuarios` join.

### PUT /api/admin/clientes/{clienteId}

**Body:** `{ nombre, entityName (nombre_tienda), correo, ciudad, motivo_estado }` — modal Editar en `AdminClientsView`. No `taxId`.

### PATCH /api/admin/clientes/{clienteId}/bloqueo

**Body:** `{ bloqueado: boolean, motivo: string }` — `toggleUserBlock` con motivo exigido.

---

## C-04. Gestión de Proveedores Aprobados

**Vista:** `AdminProvidersView.jsx` + `AdminFeaturedProvidersView.jsx` (parte) + `AdminContext.jsx:265` `toggleFeaturedProvider`

### GET /api/admin/proveedores

Columnas: `razon_social`, `nombre_comercial`, `correo`, `ciudad`, `productos_en_catalogo (COUNT productos)`, `estado`. Búsqueda por `razon_social`, `nombre_comercial`, `ciudad`.

### GET /api/admin/proveedores/{proveedorId}

Ficha corporativa + catálogo: `SELECT * FROM perfiles_proveedor WHERE id=?` + `SELECT * FROM productos WHERE proveedor_id=?` (excluye ELIMINADO_LOGICO).

### PUT /api/admin/proveedores/{proveedorId}

**Body:** `{ razon_social, nombre_comercial, correo, ciudad, motivo_estado }` — modal Editar. No `taxId`.

### PATCH /api/admin/proveedores/{proveedorId}/estado

**Body:** `{ estado: "ACTIVO" | "INACTIVO" }` — Suspender/Reactivar.

### PATCH /api/admin/proveedores/{proveedorId}/destacado

**Descripción:** Toggle de `perfiles_proveedor.destacado` — lo que hace `toggleFeaturedProvider` y muestra `AdminFeaturedProvidersView` con badge `Mayorista Destacado en Inicio`.

**Body:** `{ destacado: boolean }`

**Efecto:** Aparece en `GET /api/home/featured-content` → `featuredProviders` donde `destacado=true` y `usuarios.estado='ACTIVO'`.

---

## C-05. Solicitudes de Proveedores

**Vista:** `AdminApplicationsView.jsx:18` + `AdminContext.jsx:95` `approveApplication` / `rejectApplication` + `adminMockData.js:121` `INITIAL_PROVIDER_APPLICATIONS`

### GET /api/admin/solicitudes

**Query Params:** `estado` (PENDIENTE | APROBADA | RECHAZADA), `search` (razon_social, nombre_contacto, marcas_texto), `page`, `size`

**Response:** lista de `solicitudes_proveedor` (ej: `app_01 ByteMax Mayoristas`, `app_02 MegaChip`, `app_03 ElectroHogar` rechazada por fuera de rubro).

### PATCH /api/admin/solicitudes/{solicitudId}/aprobar

**Descripción:** Acción `Aprobar y Dar de Alta` — Transacción que existe literal en `AdminContext.jsx:95`:

1. `UPDATE solicitudes_proveedor SET estado='APROBADA', fecha_revision=NOW(), admin_revisor_id=auth.id, proveedor_creado_id=nuevo_id`
2. `INSERT INTO usuarios (uuid, nombre, correo, hash_contrasena, rol='PROVEEDOR', estado='ACTIVO')` — correo viene de `solicitudes_proveedor.correo`, hash es placeholder temporal (admin debe comunicar credenciales)
3. `INSERT INTO perfiles_proveedor (usuario_id, razon_social, nombre_comercial, ciudad, telefono_contacto, direccion)` — copia de solicitud
4. `INSERT INTO proveedor_marcas` si `marcas_texto` contiene lista

**Response HTTP 200:** `{ solicitud, nuevoProveedor }`
**HTTP 409:** ya aprobada.

### PATCH /api/admin/solicitudes/{solicitudId}/rechazar

**Body:** `{ motivo: string }` — `motivo_rechazo` exigido en modal Rechazar. Ej: `Rubro fuera del alcance... Solo componentes de computación` (`app_03`).

**Response HTTP 200:** solicitud con `estado='RECHAZADA'`.

---

## C-06. Moderación de Productos

**Vista:** `AdminProductsModerationView.jsx:21` — catálogo global de TODOS los productos + filtros categoría/proveedor/estado/búsqueda + Toggle Recomendado + Ocultar/Restaurar + Eliminar lógica.

### GET /api/admin/productos

**Query Params:** `categoria`, `proveedorId`, `estado` (ACTIVO | DESHABILITADO_POR_PROVEEDOR | OCULTO_POR_ADMIN | ELIMINADO_LOGICO), `search` (titulo ILIKE), `page`, `size`

**SQL:** `SELECT productos.* FROM productos JOIN perfiles_proveedor ON productos.proveedor_id = perfiles_proveedor.id WHERE ...` — permite `DELETED_LOGICAL` que cliente/proveedor no ven.

### PUT /api/admin/productos/{productoId}

**Descripción:** `moderateProduct` — Edición por moderación con mismo formulario que proveedor (`title`, `descripcion`, `specs` → `producto_especificaciones`, `categoria_id`, etc.).

### PATCH /api/admin/productos/{productoId}/visibilidad

**Body:** `{ estado: "ACTIVO" | "OCULTO_POR_ADMIN", motivo?: string }` — `toggleProductVisibilityAdmin` con motivo `motivo_moderacion`. Ejemplo `prod_99_violating` refrigeradora → `OCULTO_POR_ADMIN` con `motivo: Publicación fuera de rubro`.

### DELETE /api/admin/productos/{productoId}

**Body:** `{ motivo: string }` — `deleteProductAdmin:156` — `UPDATE productos SET estado='ELIMINADO_LOGICO', fecha_eliminacion=NOW(), eliminado_por=auth.id` + `INSERT INTO registros_auditoria (tipo_entidad='PRODUCTO', datos_snapshot=JSONB del producto)`. Decrementa `categorias.total_productos` si estaba ACTIVO.

### PATCH /api/admin/productos/{productoId}/recomendacion

**Body:** `{ es_recomendado: boolean }` — `toggleProductRecommendation:265` — Marca `productos.es_recomendado`. Aparece en `GET /api/home/featured-content` → `recommendedProducts`.

---

## C-07. Moderación de Ofertas

**Vista:** `AdminOffersModerationView.jsx`

### GET /api/admin/ofertas

**Query Params:** `proveedorId`, `estado` (ACTIVA | DESHABILITADA | OCULTA_POR_ADMIN | ELIMINADA_LOGICA), `page`, `size`

### PUT /api/admin/ofertas/{ofertaId}

`moderateOffer` — edita `ofertas.titulo_oferta`, `porcentaje_descuento`, etc.

### PATCH /api/admin/ofertas/{ofertaId}/visibilidad

**Body:** `{ estado: "ACTIVA" | "OCULTA_POR_ADMIN", motivo?: string }` — `toggleOfferStatusAdmin`

### DELETE /api/admin/ofertas/{ofertaId}

**Body:** `{ motivo: string }` — `deleteOfferAdmin:210` — `estado='ELIMINADA_LOGICA'` + `registros_auditoria` tipo OFERTA + `productos.tiene_oferta = false` si no quedan activas.

---

## C-08. Categorías y Tipos

**Vista:** `AdminCategoriesView.jsx:14` + `adminMockData.js:183` `INITIAL_ADMIN_CATEGORIES`

### GET /api/categorias

Público — lista de `categorias` con `subcategorias` join. Usado por `diseno_cliente` y `diseno_proveedor` dropdowns.

### POST /api/admin/categorias

**Body:** `{ nombre, icono, descripcion, subcategorias: ["DDR4","DDR5"] }` — `createCategory` → `INSERT INTO categorias (id, nombre...)` (id es slug lower, ej: `ram`) + `INSERT INTO subcategorias`.

### PUT /api/admin/categorias/{categoriaId}

`updateCategory` — edita nombre/subcategorías.

### PATCH /api/admin/categorias/{categoriaId}/estado

**Body:** `{ estado: "ACTIVA" | "INACTIVA" }` — `toggleCategoryStatus`.

**Contador:** `categorias.total_productos` se actualiza vía trigger o `UPDATE` al crear/eliminar `productos` (no es input del admin).

---

## C-09. Productos Recomendados

**Vista:** `AdminRecommendationsView.jsx` — Reusa `PATCH /admin/productos/{id}/recomendacion` de C-06. No endpoint separado. Solo vista de curaduría que lista `productos WHERE estado='ACTIVO'` con toggle.

**Endpoint:** `PATCH /api/admin/productos/{productoId}/recomendacion` ya documentado en C-06.

---

## C-10. Proveedores Destacados

**Vista:** `AdminFeaturedProvidersView.jsx` — Reusa `PATCH /admin/proveedores/{id}/destacado` de C-04.

**Endpoint:** `PATCH /api/admin/proveedores/{proveedorId}/destacado` ya documentado.

---

## C-11. Publicidad y Promociones

**Vista:** `AdminPromotionsView.jsx:17` + `AdminContext.jsx:293` `togglePromotionStatusAdmin`

### GET /api/admin/promociones

**Query Params:** `estado` (ACTIVA | EXPIRADA | CANCELADA), `proveedorId`, `page`, `size`

**Response:** lista `promociones` join `productos` y `planes_publicidad` (ej: `prom_101 SSD Kingston NV2 Plan Pro 15 días $89`).

### PATCH /api/admin/promociones/{promocionId}/estado

**Body:** `{ estado: "ACTIVA" | "CANCELADA", motivo?: string }` — `togglePromotionStatusAdmin` escribe `nota_moderacion` y `fecha_cancelacion`. Pausa campaña aunque `fecha_fin` no haya llegado.

**Nota de negocio existente:** `AdminPromotionsView` muestra `Política: la publicidad otorga mayor prioridad sin garantizar posición fija` — no es endpoint, es regla.

---

## C-12. Contenido "Nosotros"

**Vista:** `AdminContentAboutView.jsx` + `adminMockData.js:415` `INITIAL_ABOUT_CONTENT`

### GET /api/contenido/nosotros

Público — `SELECT contenido_json FROM contenido_plataforma WHERE clave_contenido='nosotros'`

### PUT /api/admin/contenido/nosotros

**Body:** `{ titulo, subtitulo, mision, pilares: [{titulo, descripcion}] }` — `updateAboutContent` → `UPDATE contenido_plataforma SET contenido_json=:jsonb, admin_actualizador_id=auth.id WHERE clave_contenido='nosotros'`

---

## C-13. Contenido "Contacto"

**Vista:** `AdminContentContactView.jsx` + `adminMockData.js:426` `INITIAL_CONTACT_INFO`

### GET /api/contenido/contacto

Público.

### PUT /api/admin/contenido/contacto

**Body:** `{ correo_soporte, correo_proveedores, telefono_central, whatsapp_soporte, direccion_oficina, horario_atencion }` — `updateContactInfo` → `clave='contacto'`.

---

## C-14. Historial de Auditoría

**Vista:** `AdminAuditHistoryView.jsx` + `adminMockData.js:435` `INITIAL_AUDIT_LOGS` (`hist_01` producto, `hist_02` smartphone, `hist_03` oferta)

### GET /api/admin/auditoria

**Query Params:** `tipo_entidad` (PRODUCTO | OFERTA), `rol_eliminador` (PROVEEDOR | ADMIN), `page`, `size`

**Response:** `Page<RegistroAuditoriaDTO>` — `SELECT * FROM registros_auditoria ORDER BY fecha_eliminacion DESC`

### GET /api/admin/auditoria/{registroId}

**Response:** `RegistroAuditoriaDetalleDTO` con `datos_snapshot` JSONB completo (copia del producto/oferta antes de eliminar).

**Propiedad:** solo lectura, inmutable, `INSERT` únicamente desde eliminaciones lógicas.

---

## C-15. Configuración del Administrador

**Vista:** `AdminSettingsView.jsx` + `ADMIN_PROFILE:4` (`Ing. Roberto Santana V., Super Administrador, admin@compunexb2b.com, Gobernanza & Operaciones`)

### GET /api/admin/yo/perfil

**Response:** `{ id, nombre, correo, rol, departamento, nivel_acceso }` — `ADMIN_PROFILE` en `adminMockData.js` tiene `department` y `accessLevel` que no existen en `usuarios`; en PostgreSQL se mapean a `usuarios.motivo_estado` o tabla opcional `perfiles_admin` (no diseñada en demo, se deja como campo `departamento` en `usuarios` extendido o solo lectura).

### PUT /api/admin/yo/perfil

**Body:** `{ nombre, correo, departamento }` — `updateAdminProfile`.

### PUT /api/admin/yo/contrasena

**Body:** `{ contrasenaActual, contrasenaNueva }` — mismo que `05_API_AUTENTICACION.md` pero scope ADMIN.

---

## Resumen de Endpoints ADMIN (solo los que existen en las vistas)

| Vista | Endpoint | Método |
|---|---|---|
| Dashboard | /api/admin/dashboard/stats | GET |
| Usuarios | /api/admin/usuarios | GET |
| Usuarios | /api/admin/usuarios/{id} | GET, PUT, PATCH /estado |
| Clientes | /api/admin/clientes | GET |
| Proveedores | /api/admin/proveedores | GET, PUT, PATCH /estado, PATCH /destacado |
| Solicitudes | /api/admin/solicitudes | GET |
| Solicitudes | /api/admin/solicitudes/{id}/aprobar | PATCH |
| Solicitudes | /api/admin/solicitudes/{id}/rechazar | PATCH |
| Productos | /api/admin/productos | GET, PUT, PATCH /visibilidad, PATCH /recomendacion, DELETE |
| Ofertas | /api/admin/ofertas | GET, PUT, PATCH /visibilidad, DELETE |
| Categorías | /api/categorias | GET (público) |
| Categorías | /api/admin/categorias | POST, PUT, PATCH /estado |
| Promociones | /api/admin/promociones | GET, PATCH /estado |
| Contenido | /api/contenido/nosotros|contacto | GET (público), PUT (admin) |
| Auditoría | /api/admin/auditoria | GET |
| Admin Yo | /api/admin/yo/perfil | GET, PUT, PUT /contrasena |

Todo endpoint aquí corresponde 1-1 con una acción de `AdminContext.jsx` o `adminMockData.js`; no se inventa ninguno fuera de esas fuentes.
