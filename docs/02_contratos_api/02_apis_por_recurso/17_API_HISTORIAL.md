# 17. API de Historial y Auditoría

> Plataforma COMPUNEX B2B — Endpoints de Historial de Eliminaciones Lógicas — PostgreSQL
> Fuente única: `git/diseno_administrador/src/components/views/AdminAuditHistoryView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:156` `deleteProductAdmin` + `AdminContext.jsx:210` `deleteOfferAdmin` + `git/diseno_administrador/src/data/adminMockData.js:435` `INITIAL_AUDIT_LOGS` (`hist_01`, `hist_02`, `hist_03`) + `git/diseno_proveedor/src/context/ProviderContext.jsx:102` `deleteProduct/deleteOffer` (proveedor también genera auditoría)
> No se inventan tipos de entidad fuera de los 3 del mock.

---

## Modelo PostgreSQL

**Tabla:** `registros_auditoria` (`04_SQL_DATABASE.sql:registros_auditoria`)

- `id` BIGSERIAL PK, `tipo_entidad` ENUM `tipo_entidad_auditoria` (`PRODUCTO`,`OFERTA`,`PROVEEDOR`,`USUARIO`,`OTRO`), `id_entidad_original` BIGINT, `titulo_entidad` VARCHAR(500), `usuario_eliminador_id` FK → `usuarios.id` NULL, `rol_eliminador` ENUM `rol_eliminador` (`PROVEEDOR`,`ADMIN`), `fecha_eliminacion` TIMESTAMPTZ DEFAULT NOW(), `motivo` TEXT, `datos_snapshot` JSONB NOT NULL, `estado` VARCHAR(50) DEFAULT 'ARCHIVADO'.

**Demo 3 registros (INITIAL_AUDIT_LOGS:435):**
- `hist_01` `PRODUCTO` — `Lote Cerrado: 100x Memorias USB Kingston IronKey 32GB` — eliminado por `TechNova Mayorista S.A.C. (Proveedor)` el `18 de Agosto, 2026 - 15:40` — motivo `Eliminación lógica solicitada por el proveedor tras agotar lote` — `datos_snapshot: {categoria:"usb", formato:"Lote cerrado", pedido_minimo:"1 Lote", precio:18.00}`
- `hist_02` `PRODUCTO` — `Lote 20x Smartphone Xiaomi Redmi Note 13 Pro 256GB` — eliminado por `Administrador (Moderación)` el `05 de Agosto, 2026` — motivo `Telefonía móvil no autorizada dentro del rubro exclusivo de componentes` — `datos_snapshot: {categoria:"otros", formato:"Caja de 20 unidades", precio:210.00}`
- `hist_03` `OFERTA` — `Oferta Flash Fin de Mes RAM DDR4 16GB` — eliminada por `SiliconByte` el `01 de Agosto` — motivo `Expiración del período de oferta` — `datos_snapshot: {descuento:"18% OFF", precioPromocional:28.50}`

---

## Endpoints (Solo Administrador, Solo Lectura)

### GET /api/admin/auditoria

**Vista:** `AdminAuditHistoryView.jsx` — `Centro de inspección de todos los registros con eliminación lógica` — lista con `tipo_entidad`, `id_entidad_original`, `titulo`, `quién eliminó`, `cuándo`, `motivo`, `datos_snapshot` (JSON colapsado), filtros `tipo_entidad` (PRODUCTO | OFERTA) y `rol_eliminador` (PROVEEDOR | ADMIN).

**Acceso:** ADMIN

**Query Params:**
- `tipoEntidad` (PRODUCTO | OFERTA | ALL) — filtro `tipo_entidad`
- `rolEliminador` (PROVEEDOR | ADMIN | ALL) — filtro `rol_eliminador`
- `page`, `size`

**Response HTTP 200:** `Page<RegistroAuditoriaDTO>`
```json
{
  "content": [
    {
      "id": "hist_01",
      "tipoEntidad": "PRODUCTO",
      "idEntidadOriginal": 13,
      "tituloEntidad": "Lote Cerrado: 100x Memorias USB Kingston IronKey 32GB",
      "usuarioEliminadorId": "uuid-proveedor-1",
      "rolEliminador": "PROVEEDOR",
      "fechaEliminacion": "2026-08-18T15:40:00Z",
      "motivo": "Eliminación lógica solicitada por el proveedor tras agotar lote Stock-lot.",
      "datosSnapshot": {
        "categoria": "usb",
        "tipo_formato": "Lote cerrado e indivisible",
        "pedido_minimo": "1 Lote completo",
        "precio_unitario_ref": 18.00,
        "precio_total_ref": 1800.00
      },
      "estado": "ARCHIVADO"
    }
  ],
  "totalElements": 3
}
```

**SQL:** `SELECT * FROM registros_auditoria WHERE (:tipoEntidad IS NULL OR tipo_entidad=:tipoEntidad::tipo_entidad_auditoria) AND (:rolEliminador IS NULL OR rol_eliminador=:rolEliminador::rol_eliminador) ORDER BY fecha_eliminacion DESC`

### GET /api/admin/auditoria/{registroId}

**Vista:** Modal `Ver datos_snapshot completo` al hacer click en fila de `AdminAuditHistoryView` — muestra `datos_snapshot` JSONB formateado con `clave/valor` del producto/oferta antes de eliminar.

**Acceso:** ADMIN

**Response HTTP 200:** `RegistroAuditoriaDetalleDTO` — mismo que listado pero con `datos_snapshot` completo sin truncar.

**HTTP 404:** no existe.

---

## Generación de Registros (no endpoint, es efecto de DELETE)

**No hay `POST /admin/auditoria`** — los registros se crean automáticamente al ejecutar eliminación lógica en:

- `DELETE /api/proveedores/yo/productos/{id}` (proveedor) → `AdminContext.jsx:102` `deleteProduct` + `ProviderContext.jsx:102` `deleteProduct` → `INSERT INTO registros_auditoria (tipo_entidad='PRODUCTO', ...)`.
- `DELETE /api/proveedores/yo/ofertas/{id}` → `deleteOffer` → `tipo_entidad='OFERTA'`.
- `DELETE /api/admin/productos/{id}` (`AdminContext.jsx:156` `deleteProductAdmin`) → `tipo_entidad='PRODUCTO'`, `rol_eliminador='ADMIN'`, `motivo` desde `Body { motivo }`.
- `DELETE /api/admin/ofertas/{id}` (`deleteOfferAdmin:210`) → `tipo_entidad='OFERTA'`, `rol_eliminador='ADMIN'`.

**Campos `datos_snapshot`:**
- Para `PRODUCTO`: `to_jsonb(productos.*)` + `array_agg(producto_especificaciones)` + `array_agg(imagenes_producto)` — copia completa antes de `SET estado='ELIMINADO_LOGICO'`.
- Para `OFERTA`: `to_jsonb(ofertas.*)` antes de `ELIMINADA_LOGICA`.

---

## Reglas Observadas

- **Inmutable y solo lectura:** `AdminAuditHistoryView` muestra `Datos de solo lectura (inmutable)` — no hay `PUT/DELETE` sobre `registros_auditoria`. Solo `INSERT` y `GET`.
- **Filtrado por tipo y rol:** UI tiene 2 selects `Filtrar por tipo de entidad (PRODUCTO | OFERTA)` y `Filtrado por quién eliminó (Proveedor | Admin)` — mapean a `tipo_entidad` y `rol_eliminador`.
- **Motivo siempre registrado:** `hist_01` `motivo: Eliminación lógica solicitada...`, `hist_02` `Telefonía no autorizada...`, `hist_03` `Expiración del período...` — `motivo` es `NOT NULL` en UI pero `NULL` en DB si no se provee.
- **No hay auditoría de usuarios/proveedores en demo:** `tipo_entidad` enum incluye `PROVEEDOR, USUARIO, OTRO` pero `INITIAL_AUDIT_LOGS` solo tiene `PRODUCTO` y `OFERTA`; no se inventa auditoría de usuarios.
