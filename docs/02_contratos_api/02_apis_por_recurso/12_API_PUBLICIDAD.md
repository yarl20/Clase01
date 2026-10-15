# 12. API de Publicidad y Promociones

> Plataforma COMPUNEX B2B — Endpoints de Campañas Publicitarias y Planes — PostgreSQL
> Fuente única: `git/diseno_proveedor/src/components/views/PromotionsManagementView.jsx` + `git/diseno_proveedor/src/context/ProviderContext.jsx:171` `addPromotion` + `git/diseno_proveedor/src/data/providerMockData.js:242` `INITIAL_PROMOTIONS` + `git/diseno_administrador/src/components/views/AdminPromotionsView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:293` `togglePromotionStatusAdmin` + `git/diseno_administrador/src/data/adminMockData.js:371` `INITIAL_ADMIN_PROMOTIONS`
> No se inventan planes ni estados fuera de esas fuentes.

---

## Modelo PostgreSQL

**Tablas:** `planes_publicidad` (catálogo) + `promociones` (campañas contratadas) (`04_SQL_DATABASE.sql:planes_publicidad/promociones`)

- `planes_publicidad` (id BIGSERIAL PK, nombre VARCHAR(200), dias_duracion SMALLINT, costo_usd NUMERIC(10,2), descripcion TEXT, espacios_asignados VARCHAR(300), activo BOOLEAN DEFAULT true, popular BOOLEAN DEFAULT false)
- `promociones` (id BIGSERIAL PK, proveedor_id FK → `perfiles_proveedor.id`, producto_id FK → `productos.id`, plan_publicidad_id FK → `planes_publicidad.id`, nombre_plan VARCHAR(200) denormalizado, costo_usd, dias_duracion, espacios_asignados, fecha_inicio TIMESTAMPTZ, fecha_fin TIMESTAMPTZ CHECK fin>inicio, estado ENUM `estado_promocion` (`ACTIVA`,`EXPIRADA`,`CANCELADA`), nota_moderacion TEXT, fecha_creacion, fecha_cancelacion)

**Demo planes (3 fijos):**
- `Plan Impulso Básico` 7 días $49 — `Búsqueda de Categoría + Rotación en Inicio` (`INITIAL_PROMOTIONS` no lo usa, pero está en `planes_publicidad`)
- `Plan Pro Mayorista` 15 días $89 (popular) — `Bloque Promocionados en Inicio + Prioridad Máxima en Búsqueda` — usado en `prom_1` y `prom_101/102`
- `Plan Expansión Premium` 30 días $149 — `Bloque Promocionados en Inicio + Destacado en Categoría` — usado en `prom_2`

**Demo promociones:**
- Proveedor `INITIAL_PROMOTIONS:242` — `prom_1` `SSD Kingston NV2` Plan Pro 15 días `daysRemaining 10`, `prom_2` `RTX 4070 Ti` Plan Expansión 30 días `daysRemaining 24`
- Admin `INITIAL_ADMIN_PROMOTIONS:371` — `prom_101` `SSD Kingston NV2` Plan Pro, `prom_102` `RTX 4060` Plan Expansión, `prom_103` `WD Purple 4TB` Plan Impulso 7 días

---

## Endpoints Públicos / Proveedor (planes)

### GET /api/planes-publicidad

**Vista:** `PromotionsManagementView.jsx` — lista `Ver planes de visibilidad disponibles` con 3 cards (7/15/30 días, $49/$89/$149, badge `Recomendado` en Pro).

**Descripción:** Lista de planes activos para contratar.

**Acceso:** PUBLIC o PROVEEDOR (no requiere rol específico)

**Response HTTP 200:** `List<PlanPublicidadDTO>`
```json
[
  { "id": 1, "nombre": "Plan Impulso Básico", "diasDuracion": 7, "costoUsd": 49.00, "descripcion": "Mayor frecuencia...", "espaciosAsignados": "Búsqueda de Categoría + Rotación en Inicio", "popular": false, "activo": true },
  { "id": 2, "nombre": "Plan Pro Mayorista", "diasDuracion": 15, "costoUsd": 89.00, "descripcion": "Prioridad alta en Bloque Promocionados...", "espaciosAsignados": "Bloque Promocionados en Inicio + Prioridad Máxima en Búsqueda", "popular": true, "activo": true },
  { "id": 3, "nombre": "Plan Expansión Premium", "diasDuracion": 30, "costoUsd": 149.00, "descripcion": "Exposición continua 30 días...", "espaciosAsignados": "Bloque Promocionados en Inicio + Destacado en Categoría", "popular": false, "activo": true }
]
```

**SQL:** `SELECT * FROM planes_publicidad WHERE activo=true ORDER BY dias_duracion`

---

## Endpoints Proveedor (mis campañas)

### GET /api/proveedores/yo/promociones

**Vista:** `PromotionsManagementView.jsx` — `Ver campañas activas` con `producto, plan, fecha inicio/fin, días restantes, costo, espacios asignados`, badge `ACTIVA`.

**Acceso:** PROVEEDOR

**Response HTTP 200:** `List<PromocionDTO>` — `SELECT * FROM promociones WHERE proveedor_id=:auth.proveedorId ORDER BY fecha_fin DESC`

**DTO incluye cálculo:** `diasRestantes = CEIL(EXTRACT(EPOCH FROM (fecha_fin - NOW())) / 86400)` (en PostgreSQL, no MySQL `DATEDIFF`). Demo muestra `daysRemaining` estático; backend lo calcula dinámicamente. Si `diasRestantes <=0` y `estado='ACTIVA'` → job debe pasar a `EXPIRADA` (ver regla).

### POST /api/proveedores/yo/promociones

**Vista:** Modal `Contratar nueva campaña` en `PromotionsManagementView` — dropdown `Seleccionar producto` (solo `productos.estado='ACTIVO'` del proveedor) + `Seleccionar plan` + `Confirmar y activar`. Nota visible: `la publicidad otorga mayor prioridad sin garantía de posición fija` (`PromotionsManagementView.jsx`).

**Request Body:**
```json
{
  "productoId": "uuid-producto",
  "planId": 2
}
```

**Validaciones (observadas en `ProviderContext.jsx:172` y `07_API_PROVEEDOR.md:414`):**
- `productoId` debe existir, `productos.proveedor_id = auth.proveedorId` y `productos.estado='ACTIVO'`
- `planId` debe existir y `planes_publicidad.activo=true`
- **No debe existir `promociones` con `producto_id=:productoId AND estado='ACTIVA'`** — solo una campaña activa por producto a la vez. `409 Conflict` si ya existe.

**Lógica:**
1. `SELECT * FROM planes_publicidad WHERE id=:planId` → `dias_duracion, costo_usd, nombre, espacios_asignados`
2. `INSERT INTO promociones (proveedor_id, producto_id, plan_publicidad_id, nombre_plan=nombre, costo_usd, dias_duracion, espacios_asignados, fecha_inicio=NOW(), fecha_fin=NOW() + dias_duracion * INTERVAL '1 day', estado='ACTIVA')`
3. `UPDATE productos SET es_promocionado=true WHERE id=:productoId` — `ProviderContext.jsx:188` marca `isPromoted=true`.

**Response HTTP 201:** `PromocionDTO` con `fecha_inicio`, `fecha_fin`, `diasRestantes = dias_duracion`.

**Response HTTP 409:** `El producto ya tiene una campaña activa` (`PromotionsManagementView` debe mostrar error).

---

## Endpoints Administrador

### GET /api/admin/promociones

**Vista:** `AdminPromotionsView.jsx:17` — `Supervisión y moderación de campañas activas` — tabla con `producto, proveedor, plan, días restantes, costo, estado`, filtros `estado`, `proveedorId`, `search`.

**Acceso:** ADMIN

**Query Params:** `estado` (ACTIVA | EXPIRADA | CANCELADA), `proveedorId`, `page`, `size`

**SQL:** `SELECT promociones.* JOIN productos ON promociones.producto_id=productos.id JOIN perfiles_proveedor ON promociones.proveedor_id=perfiles_proveedor.id WHERE ... ORDER BY fecha_fin DESC`

### PATCH /api/admin/promociones/{promocionId}/estado

**Vista:** `AdminPromotionsView` — botones `Cancelar/Pausar campaña → requiere motivo` y `Reactivar`.

**Body:** `{ estado: "ACTIVA" | "CANCELADA", motivo?: string }` — `togglePromotionStatusAdmin:293` escribe `nota_moderacion` y `fecha_cancelacion`.

**Lógica:**
- Si `estado='CANCELADA'`: `UPDATE promociones SET estado='CANCELADA', nota_moderacion=:motivo, fecha_cancelacion=NOW() WHERE id=?` + `UPDATE productos SET es_promocionado=false WHERE id=promociones.producto_id AND NOT EXISTS (SELECT 1 FROM promociones WHERE producto_id=? AND estado='ACTIVA' AND id != ?)`
- Si `estado='ACTIVA'` (reactivar): `UPDATE promociones SET estado='ACTIVA', nota_moderacion=NULL, fecha_cancelacion=NULL` + `UPDATE productos SET es_promocionado=true`.

**Response HTTP 200**

---

## Reglas Observadas (teóricas, no inventadas)

- **Espacios no garantizan posición fija:** `PromotionsManagementView` nota `la publicidad otorga mayor prioridad sin garantizar posición fija` — `HomeView` hace `ORDER BY RANDOM()` dentro de `promotedProducts` (`06_API_CLIENTE.md:30`).
- **Expiración automática:** `promociones.fecha_fin` es `fecha_inicio + dias_duracion`. Cuando `NOW() > fecha_fin` y `estado='ACTIVA'`, job diario debe `SET estado='EXPIRADA'` (no endpoint, es cron `UPDATE promociones SET estado='EXPIRADA' WHERE estado='ACTIVA' AND fecha_fin < NOW()`).
- **Desnormalización:** `promociones.nombre_plan/costo_usd/dias_duracion/espacios_asignados` copia de `planes_publicidad` al momento de compra — si plan cambia precio no afecta campañas ya contratadas.
