# 13. API de Recomendaciones y Destacados

> Plataforma COMPUNEX B2B — Endpoints de Curaduría — PostgreSQL
> Fuente única: `git/diseno_administrador/src/components/views/AdminRecommendationsView.jsx` + `git/diseno_administrador/src/components/views/AdminFeaturedProvidersView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:265` `toggleProductRecommendation` + `AdminContext.jsx:282` `toggleFeaturedProvider` + `git/diseno_administrador/src/data/adminMockData.js:413` `INITIAL_FEATURED_PROVIDERS` + `git/diseno_cliente/src/components/views/HomeView.jsx` bloques `Recomendados` y `Proveedores Destacados`
> No se inventan endpoints fuera de esas vistas.

---

## Modelo PostgreSQL (flags de curaduría)

- `productos.es_recomendado` BOOLEAN DEFAULT FALSE — `03_MODELO_RELACIONAL` + `04_SQL_DATABASE.sql:productos` — `AdminRecommendationsView` hace toggle.
- `perfiles_proveedor.destacado` BOOLEAN DEFAULT FALSE — `AdminFeaturedProvidersView` hace toggle. `INITIAL_FEATURED_PROVIDERS:413` = `["prov_1","prov_2","prov_3","prov_4"]`.

Ambos flags son **desnormalizados para Home** (prioridad ALTA).

---

## Productos Recomendados

### GET /api/admin/productos/recomendados

**Vista:** `AdminRecommendationsView.jsx` — `Gestión de la selección curada de productos recomendados` — lista TODOS los `productos` con indicador `isRecommended` + toggle sin modal + feedback inmediato.

**Descripción:** Aunque la vista client-side filtra, el endpoint admin equivale a `GET /api/admin/productos?es_recomendado=true` o a listado completo con flag.

**Acceso:** ADMIN

**Query Params:** `page`, `size`, `search` (título ILIKE) — reutiliza `GET /api/admin/productos` con filtro `es_recomendado`.

**Response HTTP 200:** `Page<ProductoModeracionDTO>` con `es_recomendado`.

### PATCH /api/admin/productos/{productoId}/recomendacion

**Vista:** Toggle `Recomendado` por producto en `AdminRecommendationsView` + `AdminProductsModerationView` — `PATCH /admin/products/{productId}/recommendation {isRecommended: boolean}` (`AdminContext.jsx:265`).

**Request Body:** `{ es_recomendado: boolean }`

**Lógica:** `UPDATE productos SET es_recomendado=:boolean WHERE id=:productoId` + trigger `fecha_actualizacion`.

**Efecto en Home cliente:** `GET /api/home/featured-content` → `recommendedProducts: SELECT * FROM productos WHERE es_recomendado=true AND estado='ACTIVO' ORDER BY fecha_publicacion DESC LIMIT 8` (`06_API_CLIENTE.md:30`). `AdminContext.jsx:265` muestra toast `Producto Recomendado` / `Recomendación Retirada`.

**Response HTTP 200**
**HTTP 404:** producto no encontrado.

---

## Proveedores Destacados

### GET /api/admin/proveedores/destacados

**Vista:** `AdminFeaturedProvidersView.jsx` — `Curaduría: Proveedores Mayoristas Destacados` — lista `providerUsers` con indicador `Mayorista Destacado en Inicio` vs `Mayorista Estándar`, badge `Mayorista Destacado en Inicio` (warning) vs `Estándar` (dark).

**Acceso:** ADMIN

**Response HTTP 200:** `List<ProveedorDestacadoDTO>` — `SELECT * FROM perfiles_proveedor WHERE destacado=true` join `usuarios`. Conteo `Destacados seleccionados: X de Y`.

### GET /api/proveedores/destacados

**Vista:** `HomeView.jsx` bloque `Proveedores Destacados` — público.

**Acceso:** PUBLIC

**Response HTTP 200:** `List<ProveedorResumenDTO>` — `SELECT * FROM perfiles_proveedor WHERE destacado=true AND verificado=true AND usuarios.estado='ACTIVO' LIMIT 8` (verificado + activo).

### PATCH /api/admin/proveedores/{proveedorId}/destacado

**Vista:** Botón `Marcar como Proveedor Destacado` / `Retirar de Mayoristas Destacados` en `AdminFeaturedProvidersView` — `toggleFeaturedProvider` en `AdminContext.jsx:282`.

**Request Body:** `{ destacado: boolean }`

**Lógica:** `UPDATE perfiles_proveedor SET destacado=:boolean WHERE id=:proveedorId` + toast `Proveedor Destacado` / `Proveedor Retirado`.

**Response HTTP 200**
**HTTP 404:** proveedor no encontrado.

---

## Reglas Observadas

- **Recomendado y Promocionado no son excluyentes:** `productos` puede tener `es_recomendado=true` y `es_promocionado=true` simultáneamente (ej: `prod_1` SSD Kingston es `isRecommended=true` e `isPromoted=true` en `INITIAL_ADMIN_PRODUCTS:194`). `Home` los muestra en bloques separados.
- **Verificación fiscal previa:** `AdminFeaturedProvidersView` banner dice `Distintivo basado en solvencia formal, catálogo activo y verificación comercial` — solo `perfiles_proveedor.verificado=true` puede ser `destacado` (regla implícita en demo, no validada por backend pero existe en UI).
