# 10. API de Ofertas

> Plataforma COMPUNEX B2B — Endpoints de Ofertas — PostgreSQL
> Fuente única: `git/diseno_proveedor/src/components/views/OffersManagementView.jsx` + `git/diseno_proveedor/src/context/ProviderContext.jsx:117` `addOffer/updateOffer/toggleOfferStatus/deleteOffer` + `git/diseno_proveedor/src/data/providerMockData.js:213` `INITIAL_OFFERS` + `git/diseno_administrador/src/components/views/AdminOffersModerationView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:194` `toggleOfferStatusAdmin/moderateOffer/deleteOfferAdmin`
> No se inventan endpoints fuera de esas vistas.

---

## Modelo

**Tabla PostgreSQL:** `ofertas` (`08`/`02` + `04_SQL_DATABASE.sql:ofertas`)
- `id` BIGSERIAL PK, `proveedor_id` FK → `perfiles_proveedor.id`, `producto_id` FK → `productos.id`, `titulo_oferta` VARCHAR(300), `porcentaje_descuento` SMALLINT CHECK 1-99, `precio_original` NUMERIC(10,2), `precio_promocional` NUMERIC(10,2) CHECK < original, `moneda` VARCHAR(3) DEFAULT 'USD', `vigencia` VARCHAR(100) (ej: "15 de Octubre, 2026" o "Hasta agotar lote"), `terminos` TEXT, `estado` ENUM `estado_oferta` (`ACTIVA`,`DESHABILITADA`,`OCULTA_POR_ADMIN`,`ELIMINADA_LOGICA`), `motivo_moderacion` TEXT, `fecha_creacion`, `fecha_actualizacion`, `fecha_eliminacion`.

**Demo:** `INITIAL_OFFERS:213` — `offer_1` `Descuento Especial Lanzamiento DDR5` 12% (`112→98`), `offer_2` `Super Oferta Modular 850W` 15% (`108→92.50`), ambas `status=ACTIVE`.

---

## Endpoints Proveedor (misma oferta vista por PROVEEDOR)

### GET /api/proveedores/yo/ofertas

**Vista:** `OffersManagementView.jsx` — lista con filtros `Todos | Activos | Deshabilitados`, cards con `productoRelacionado`, `descuento%`, `precioOriginal`, `precioPromocional`, `vigencia`, `estado`.

**Acceso:** PROVEEDOR

**Query Params:** `estado` (ACTIVA | DESHABILITADA, opcional), `page`, `size`

**Response HTTP 200:** `Page<OfertaDTO>` — `SELECT * FROM ofertas WHERE proveedor_id=:auth.proveedorId AND estado != 'ELIMINADA_LOGICA'`

### POST /api/proveedores/yo/ofertas

**Vista:** Modal `Crear nueva oferta` en `OffersManagementView` — dropdown solo `productos` con `estado='ACTIVO'` del proveedor + campos `titulo_oferta`, `% descuento (0–99)`, `precio_original`, `precio_promocional`, `vigencia` (texto), `terminos`.

**Request Body:**
```json
{
  "productoId": "uuid-producto",
  "tituloOferta": "Descuento Especial Lanzamiento DDR5 por Caja Máster",
  "porcentajeDescuento": 12,
  "precioOriginal": 112.00,
  "precioPromocional": 98.00,
  "vigencia": "15 de Octubre, 2026",
  "terminos": "Válido únicamente para compras mínimas de 1 Caja Máster (15 kits)."
}
```

**Validaciones (observadas en `ProviderContext.jsx:118`):**
- `productoId` debe existir, `productos.proveedor_id = auth.proveedorId` y `productos.estado='ACTIVO'`
- `porcentajeDescuento` 1-99
- `precio_promocional < precio_original` y `>0` (CHECK)

**Lógica:** `INSERT INTO ofertas (...) estado='ACTIVA'` + `UPDATE productos SET tiene_oferta=true WHERE id=:productoId` — `ProviderContext.jsx:127` marca `isOffer=true`.

**Response HTTP 201:** `OfertaDTO`
**HTTP 404:** producto no existe o no pertenece al proveedor

### PUT /api/proveedores/yo/ofertas/{ofertaId}

**Vista:** Modal `Editar oferta` — mismos campos.

**Validación:** `ofertas.proveedor_id = auth.proveedorId` y `estado != 'ELIMINADA_LOGICA'`

**Response HTTP 200:** Oferta actualizada

### PATCH /api/proveedores/yo/ofertas/{ofertaId}/estado

**Vista:** Botón `Deshabilitar/Activar` → `toggleOfferStatus` en `ProviderContext.jsx:150` — `ACTIVE ↔ DESHABILITADA`.

**Request Body:** `{ estado: "ACTIVA" | "DESHABILITADA" }`

**Response HTTP 200:** `{ id, estado }`

### DELETE /api/proveedores/yo/ofertas/{ofertaId}

**Vista:** Botón `Eliminar` con confirmación → `deleteOffer` → `ELIMINADA_LOGICA`.

**Lógica:**
1. Verificar `proveedor_id` propietario
2. `UPDATE ofertas SET estado='ELIMINADA_LOGICA', fecha_eliminacion=NOW() WHERE id=?`
3. `INSERT INTO registros_auditoria (tipo_entidad='OFERTA', id_entidad_original, titulo_entidad=titulo_oferta, rol_eliminador='PROVEEDOR', datos_snapshot=to_jsonb(oferta))`
4. Si `NOT EXISTS (SELECT 1 FROM ofertas WHERE producto_id=:productoId AND estado='ACTIVA')` → `UPDATE productos SET tiene_oferta=false`

**Response HTTP 204**

---

## Endpoints Públicos (vista Cliente)

### GET /api/ofertas

**Vista:** `HomeView.jsx` bloque `Ofertas Vigentes` + `ExploreProductsView` — `GET /api/products` ya incluye ofertas, pero este endpoint lista solo ofertas activas con producto activo.

**Acceso:** PUBLIC

**Response HTTP 200:** `Page<OfertaConProductoDTO>` — `SELECT ofertas.* JOIN productos ON ofertas.producto_id=productos.id WHERE ofertas.estado='ACTIVA' AND productos.estado='ACTIVO' LIMIT 6` (ver `06_API_CLIENTE.md:30`)

No hay creación/edición pública.

---

## Endpoints Administrador

### GET /api/admin/ofertas

**Vista:** `AdminOffersModerationView.jsx` — tabla global con `título, producto, proveedor, descuento%, precios, vigencia, estado`, filtros por `estado`.

**Acceso:** ADMIN

**Query Params:** `proveedorId`, `estado` (ACTIVA | DESHABILITADA | OCULTA_POR_ADMIN | ELIMINADA_LOGICA), `page`, `size`

**SQL:** `SELECT ofertas.* FROM ofertas JOIN productos ... JOIN perfiles_proveedor ... WHERE ...`

### PUT /api/admin/ofertas/{ofertaId}

**Descripción:** `moderateOffer` — admin edita cualquier campo de la oferta (moderación).

**Response HTTP 200**

### PATCH /api/admin/ofertas/{ofertaId}/visibilidad

**Body:** `{ estado: "ACTIVA" | "OCULTA_POR_ADMIN", motivo?: string }` — `toggleOfferStatusAdmin:194` escribe `motivo_moderacion`.

### DELETE /api/admin/ofertas/{ofertaId}

**Body:** `{ motivo: string }` — `deleteOfferAdmin:210` — `estado='ELIMINADA_LOGICA'` + `INSERT registros_auditoria tipo='OFERTA', rol_eliminador='ADMIN'` + `productos.tiene_oferta` recalculado.

---

## Reglas Observadas (no inventadas)

- `vigencia` es `VARCHAR(100)` textual (`"15 de Octubre, 2026"`, `"Hasta agotar lote (Últimos 3 lotes)"` en `INITIAL_PRODUCTS:236`) — no es `TIMESTAMPTZ`; filtrar vigentes requiere `estado='ACTIVA'` no fecha.
- `descuento%` solo informático; `precio_promocional < precio_original` es CHECK.
- `Home` filtra `ofertas.estado='ACTIVA'` + `productos.estado='ACTIVO'`.
