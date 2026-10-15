# 16. API de Gestión de Usuarios (Solo Administrador)

> Plataforma COMPUNEX B2B — Endpoints de Administración Global de Usuarios — PostgreSQL
> Fuente única: `git/diseno_administrador/src/components/views/AdminUsersView.jsx` + `git/diseno_administrador/src/components/views/AdminClientsView.jsx` + `git/diseno_administrador/src/components/views/AdminProvidersView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:61` `toggleUserBlock/toggleUserStatus/updateUser` + `git/diseno_administrador/src/data/adminMockData.js:14` `INITIAL_USERS`
> No se inventan endpoints fuera de esas 3 vistas + context.

---

## Modelo PostgreSQL

**Tablas principales:** `usuarios` (id, uuid, nombre, correo, hash_contrasena, rol, estado, motivo_estado, fecha_registro, ultimo_acceso) + `perfiles_cliente` (nombre_tienda, ciudad...) + `perfiles_proveedor` (razon_social, nombre_comercial, ciudad...) — join según `rol`.

**Demo 8 usuarios (INITIAL_USERS:14):**
- `usr_101` Carlos Mendoza — `TecnoStore Express` — CLIENTE — Arequipa — ACTIVO
- `usr_102` Marco Valdivia — `PC Gamer Trujillo` — CLIENTE — Trujillo — ACTIVO
- `usr_103` Lucia Ramos — `CyberHard Cusco` — CLIENTE — Cusco — ACTIVO
- `usr_104` Fernando Soto — `ElectroTienda Import (Inactivo)` — CLIENTE — Lima — BLOQUEADO (nota: bloqueada temporalmente por disputa)
- `usr_201` TechNova — PROVEEDOR — Lima — ACTIVO — `Importador Kingston...`
- `usr_202` SiliconByte — PROVEEDOR — Lima — ACTIVO
- `usr_203` Nexus Hardware — PROVEEDOR — Callao — ACTIVO
- `usr_204` CompuCore — PROVEEDOR — Lima — ACTIVO

**Vista global:** `AdminUsersView.jsx` muestra tabla con TODOS (clientes+proveedores) — columnas: `Usuario / Razón Social` (nombre + entityName + correo), `Rol` badge `Cliente / Proveedor`, `Ciudad`, `Estado` badge `Activo/Inactivo/Bloqueado`, `Fecha Registro`, `Acciones` (Ver, Editar, Power activar/desactivar, Lock desbloquear/bloquear con motivo).

---

## Endpoints (todos ADMIN)

### GET /api/admin/usuarios

**Vista:** `AdminUsersView.jsx` — tabla con búsqueda y filtros `rol` y `estado`.

**Query Params:**
- `search` (string, opcional) — `ILIKE` en `usuarios.nombre`, `usuarios.correo`, `perfiles_cliente.nombre_tienda`, `perfiles_proveedor.razon_social`/`nombre_comercial`
- `rol` (ALL | CLIENTE | PROVEEDOR) — `AdminUsersView` select `Todos los Roles / Solo Clientes / Solo Proveedores`
- `estado` (ALL | ACTIVO | INACTIVO | BLOQUEADO) — select `Todos los Estados`
- `page`, `size`

**Response HTTP 200:** `Page<UsuarioAdminDTO>`
```json
{
  "content": [
    {
      "id": "usr_101",
      "uuid": "uuid-101",
      "nombre": "Carlos Mendoza R.",
      "correo": "compras@tecnostore-express.com",
      "rol": "CLIENTE",
      "rolLabel": "Cliente / Comprador",
      "entidad": "TecnoStore Express",
      "ciudad": "Arequipa",
      "estado": "ACTIVO",
      "fechaRegistro": "12 de Enero, 2026"
    }
  ],
  "totalElements": 8,
  "totalPages": 1
}
```

**SQL:** `SELECT usuarios.*, COALESCE(perfiles_cliente.nombre_tienda, perfiles_proveedor.razon_social) AS entidad, COALESCE(perfiles_cliente.ciudad, perfiles_proveedor.ciudad) AS ciudad FROM usuarios LEFT JOIN perfiles_cliente ON perfiles_cliente.usuario_id=usuarios.id LEFT JOIN perfiles_proveedor ON perfiles_proveedor.usuario_id=usuarios.id WHERE (rol=:rol OR :rol='ALL') AND (estado=:estado OR :estado='ALL') AND (nombre ILIKE '%search%' OR correo ILIKE '%search%' OR entidad ILIKE '%search%') ORDER BY fecha_registro DESC`

### GET /api/admin/usuarios/{usuarioId}

**Vista:** Modal `Ver Ficha` en `AdminUsersView` — muestra `nombre, entityName, ciudad, estado, notas/motivo_estado`.

**Response HTTP 200:** `UsuarioDetalleDTO` con join a perfil según rol.

### PUT /api/admin/usuarios/{usuarioId}

**Vista:** Modal `Editar Datos Administrativos` en `AdminUsersView` — campos: `nombre`, `entityName` (razon_social/nombre_tienda), `correo`, `ciudad`, `notas` (motivo_estado).

**Request Body:**
```json
{
  "nombre": "Carlos Mendoza R.",
  "entityName": "TecnoStore Express",
  "correo": "compras@tecnostore-express.com",
  "ciudad": "Arequipa",
  "notas": "Comprador mayorista verificado..."
}
```

**Validaciones:**
- `correo` formato válido y UNIQUE en `usuarios.correo` (excluyendo el propio `id`)
- `nombre` no vacío

**Response HTTP 200:** usuario actualizado — `updateUser` en `AdminContext.jsx:89`.

### PATCH /api/admin/usuarios/{usuarioId}/estado

**Vista:** Botones `Power` (Activar/Desactivar `ACTIVO ↔ INACTIVO`) y `Lock/Unlock` (Bloquear/Desbloquear) en tabla.

**Descripción:** Dos acciones distintas en UI pero un solo endpoint con `estado`.

**Request Body:**
```json
{
  "estado": "ACTIVO" | "INACTIVO" | "BLOQUEADO",
  "motivo": "Incumplimiento de políticas de plataforma" // requerido si BLOQUEADO
}
```

**Lógica:**
- Si `estado='BLOQUEADO'`: `UPDATE usuarios SET estado='BLOQUEADO', motivo_estado=:motivo WHERE id=:usuarioId` — `toggleUserBlock` con `reason`.
- Si `estado='ACTIVO'|'INACTIVO'`: `UPDATE usuarios SET estado=:estado, motivo_estado=:motivo WHERE id=:usuarioId` — `toggleUserStatus`.
- `BLOCKED` (en demo inglés) mapea a `BLOQUEADO` (español DB).

**Response HTTP 200**

---

## Endpoints Especializados (vistas separadas pero mismo modelo)

### GET /api/admin/clientes

**Vista:** `AdminClientsView.jsx` — tabla solo `rol=CLIENTE` — columnas: `Tienda / Titular`, `Correo`, `Ciudad Sede`, `Estado`, `Registro`. Misma lógica que `GET /admin/usuarios?rol=CLIENTE` pero endpoint dedicado para esa vista.

**Query Params:** `search` (tienda, titular, ciudad), `estado`, `page`, `size`

### GET /api/admin/proveedores

**Vista:** `AdminProvidersView.jsx` — tabla solo `rol=PROVEEDOR` — columnas: `Empresa Mayorista / Razón Social` (nombre + trade), `Correo`, `Sede Fiscal` (ciudad), `Productos en Catálogo` (COUNT productos), `Estado`.

**Query Params:** `search` (razón social, nombre_comercial, ciudad), `page`, `size`

**Nota:** `AdminClientsView` y `AdminProvidersView` son wrappers filtrados de `AdminUsersView`; backend puede implementarlos como `GET /admin/usuarios?rol=...` o como endpoints dedicados que hacen el mismo `JOIN`. En demo, `AdminContext.jsx` filtra `users.filter(u => u.role === 'CLIENT'/'PROVIDER')` client-side, pero backend debe paginar server-side.

---

## Reglas Observadas

- **Búsqueda global:** `AdminUsersView` placeholder original `Buscar por nombre, empresa o email...` (sin RUC) — `search` cruza 3 campos.
- **Motivo de bloqueo obligatorio:** Modal `Bloquear Cuenta` exige `motivo` textarea (`blockReason` state) — se guarda en `usuarios.motivo_estado` y visible en ficha.
- **Edición no cambia rol:** `rol` no es editable por admin en demo (solo `nombre`, `entityName`, `correo`, `ciudad`, `notas`).
- **Estado visual:** `ACTIVO` (verde), `INACTIVO` (gris), `BLOQUEADO` (rojo con Lock) — mapeo directo a `usuarios.estado`.
