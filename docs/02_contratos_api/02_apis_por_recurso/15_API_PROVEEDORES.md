# 15. API de Proveedores (Perfiles Públicos)

> Plataforma COMPUNEX B2B — Endpoints de Perfiles Públicos de Proveedores — PostgreSQL
> Fuente única: `git/diseno_cliente/src/components/views/ProviderProfileView.jsx` + `git/diseno_cliente/src/components/views/ProductDetailView.jsx` (bloque proveedor) + `git/diseno_cliente/src/components/views/HomeView.jsx` (Proveedores Destacados) + `git/diseno_proveedor/src/components/views/PublicCatalogPreviewView.jsx` + `git/diseno_administrador/src/components/views/AdminProvidersView.jsx` (ficha) + `git/diseno_cliente/src/data/mockData.js:4` `INITIAL_PROVIDERS`
> No se inventan campos fuera de `INITIAL_PROVIDERS` y vistas.

---

## Modelo PostgreSQL

**Tablas:** `perfiles_proveedor` (razon_social, nombre_comercial, telefono_contacto, url_logo, url_banner, direccion, ciudad, anos_mercado, descripcion, politica_comercial, calificacion, verificado, destacado) + `proveedor_marcas` + `proveedor_especialidades` + `productos` (para `total_productos`) + `usuarios` (estado).

**Demo 4 proveedores (INITIAL_PROVIDERS):**
- `prov_1` TechNova Mayorista S.A.C. (`TechNova Wholesale`) — 12 años, 4.9, Lima, Av. Tecnológica 1420 — `Almacenamiento NVMe, Memorias RAM DDR5` — marcas Kingston/Corsair/Gigabyte/WD
- `prov_2` SiliconByte — 8 años, 4.8 — `Procesadores, Placas Madre Z790` — ASUS/MSI/AMD/Intel
- `prov_3` Nexus Hardware — 15 años, 4.7 — `Fuentes 80+ Gold` — Seagate/Samsung
- `prov_4` CompuCore — 6 años, 4.9 — `Lotes cerrados` — TeamGroup/Crucial

---

## Endpoints Públicos

### GET /api/proveedores/{proveedorId}/perfil-publico

**Vista:** `ProviderProfileView.jsx` — perfil público completo + catálogo del proveedor. Muestra banner, logo+nombre, `telefono_contacto`, `correo` (ventas.mayoristas@...), `direccion, ciudad`, `descripcion`, `especialidades[]`, `marcas[]`, `politica_comercial`, `rating`, `anos_mercado`, `totalProductosCount`, listado `productos` solo `ACTIVO`.

**Acceso:** PUBLIC

**Path Params:** `proveedorId` — `UUID` o `id` textual `prov_1` (demo usa `prov_1`; backend resuelve por `perfiles_proveedor.id` o `usuarios.uuid`)

**Response HTTP 200:** `ProveedorPerfilPublicoDTO`
```json
{
  "id": "uuid-proveedor",
  "razonSocial": "TechNova Mayorista S.A.C.",
  "nombreComercial": "TechNova Wholesale",
  "logoUrl": "https://images.unsplash.com/photo-1599305445671...",
  "bannerUrl": "https://images.unsplash.com/photo-1550751827...",
  "telefonoContacto": "+51 (01) 748-9900 / +51 992 341 820",
  "correo": "ventas.mayoristas@technova.com",
  "direccion": "Av. Tecnológica 1420, Parque Industrial Tecnológico",
  "ciudad": "Lima, Perú",
  "anosMercado": "12 años",
  "descripcion": "Importadores directos autorizados de memorias, almacenamiento sólido...",
  "politicaComercial": "Despacho prioritario a almacén central o agencias...",
  "especialidades": ["Almacenamiento NVMe", "Memorias RAM DDR5/DDR4", "Tarjetas Gráficas"],
  "marcas": ["Kingston", "Corsair", "Gigabyte", "Western Digital"],
  "verificado": true,
  "calificacion": 4.9,
  "totalProductos": 38
}
```

**SQL:** `SELECT perfiles_proveedor.*, array_agg(proveedor_marcas.marca) AS marcas, array_agg(proveedor_especialidades.especialidad) AS especialidades, (SELECT COUNT(*) FROM productos WHERE proveedor_id=perfiles_proveedor.id AND estado='ACTIVO') AS total FROM perfiles_proveedor JOIN usuarios ON perfiles_proveedor.usuario_id=usuarios.id WHERE perfiles_proveedor.id=:id AND usuarios.estado='ACTIVO' AND perfiles_proveedor.verificado=true`

**Validación:** Si `usuarios.estado != ACTIVO` o `perfiles_proveedor.verificado=false` → `404` (no público).

### GET /api/proveedores/{proveedorId}/catalogo

**Vista:** Catálogo exclusivo debajo del perfil en `ProviderProfileView.jsx` — con búsqueda interna y filtro por categoría + contador `totalProductosCount`.

**Acceso:** PUBLIC

**Query Params:** `search` (string, ILIKE en `productos.titulo`), `categoria` (ram/ssd...), `page`, `size`

**Response HTTP 200:** `Page<ProductoResumenDTO>` — `SELECT * FROM productos WHERE proveedor_id=:proveedorId AND estado='ACTIVO' AND (titulo ILIKE '%search%') AND categoria_id=:categoria ORDER BY fecha_publicacion DESC`

**Nota:** `PublicCatalogPreviewView.jsx` en `diseno_proveedor` es vista previa del proveedor que reutiliza este mismo endpoint (solo lectura).

### GET /api/proveedores

**Vista:** Listado `Proveedores Destacados` en `HomeView.jsx` + búsqueda general de proveedores (no vista dedicada pero existe como bloque).

**Acceso:** PUBLIC

**Query Params:** `search` (razon_social/nombre_comercial ILIKE), `ciudad`, `page`, `size`

**Response HTTP 200:** `Page<ProveedorResumenDTO>` — `SELECT * FROM perfiles_proveedor JOIN usuarios ON ... WHERE usuarios.estado='ACTIVO' AND verificado=true`

### GET /api/proveedores/destacados

**Vista:** `HomeView.jsx` bloque `Proveedores Destacados` + `AdminFeaturedProvidersView` curaduría.

**Acceso:** PUBLIC

**Response HTTP 200:** `List<ProveedorResumenDTO>` — `SELECT * FROM perfiles_proveedor WHERE destacado=true AND verificado=true AND usuarios.estado='ACTIVO' LIMIT 8` — ya documentado en `13_API_RECOMENDACIONES.md` pero endpoint público duplicado aquí para completitud.

---

## Endpoints Administrador (repetidos en 08 para completitud)

### GET /api/admin/proveedores — ya en `08_API_ADMINISTRADOR.md:C-04`
### PATCH /api/admin/proveedores/{id}/destacado — ya en `13_API_RECOMENDACIONES.md`

No se duplican; se referencian.

---

## Campos Expuestos vs No Expuestos (observado en diseño)

- **Sí público:** `razon_social`, `nombre_comercial`, `url_logo`, `url_banner`, `telefono_contacto`, `correo` (ventas), `direccion`, `ciudad`, `anos_mercado`, `descripcion`, `politica_comercial`, `marcas`, `especialidades`, `calificacion`, `verificado`, `totalProductos`.
- **No público:** `usuario_id`, `hash_contrasena`, `motivo_estado`, `fecha_creacion` interna. `correo` es `ventas.mayoristas@...` (correo comercial, no personal del `usuarios.correo` que puede ser igual).

**Demo:** `ProviderProfileView.jsx` muestra `📍 {profile.address}, {profile.city}` y `Teléfono de Contacto: {profile.contactPhone}` — sin RUC.

---

## Reglas Observadas

- **Solo verificados son públicos:** `perfiles_proveedor.verificado=true` (aprobado por ADMIN) es requisito para aparecer en catálogo público; `AdminProvidersView` muestra `verificado` badge.
- **Catálogo solo ACTIVO:** `ProviderProfileView` listado es `productos WHERE estado='ACTIVO'` (excluye `DESHABILITADO_POR_PROVEEDOR` y `OCULTO_POR_ADMIN` y `ELIMINADO_LOGICO`).
- **Búsqueda interna del catálogo:** `ProviderProfileView` tiene input `Buscar productos dentro del catálogo` → `search` param.
