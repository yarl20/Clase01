# 09. API de Productos

> Plataforma COMPUNEX B2B — Endpoints de Productos: creación, edición, estados, moderación — PostgreSQL
> Fuente única: `git/diseno_proveedor/src/components/views/CreateEditProductView.jsx` + `git/diseno_proveedor/src/components/views/ProductManagementView.jsx` + `git/diseno_proveedor/src/context/ProviderContext.jsx:62` `addProduct/updateProduct/toggleProductStatus/deleteProduct` + `git/diseno_proveedor/src/data/providerMockData.js:33` `INITIAL_PROVIDER_PRODUCTS` (7 productos: 4 ACTIVO, 1 DESHABILITADO, 1 ELIMINADO_LOGICO, 1 con promoción) + `git/diseno_cliente/src/components/views/ProductDetailView.jsx` + `git/diseno_cliente/src/components/views/ExploreProductsView.jsx` + `git/diseno_cliente/src/components/views/HomeView.jsx` + `git/diseno_administrador/src/components/views/AdminProductsModerationView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:135` `toggleProductVisibilityAdmin/moderateProduct/deleteProductAdmin/toggleProductRecommendation`
> No se inventan campos fuera de esas vistas/contexts.

---

## Modelo PostgreSQL

**Tablas:** `productos` + `producto_especificaciones` + `imagenes_producto` + `categorias` + `registros_auditoria` (`04_SQL_DATABASE.sql:productos`)

- `productos` (id BIGSERIAL PK, uuid UUID UNIQUE DEFAULT gen_random_uuid(), proveedor_id FK → `perfiles_proveedor.id`, categoria_id FK → `categorias.id` VARCHAR(50), titulo VARCHAR(500), descripcion TEXT, modelo_comercial ENUM `modelo_comercial` (`VENTA_MAYORISTA_TRADICIONAL`,`DISTRIBUCION_EXCLUSIVA`,`LOTE_STOCK`), tipo_formato VARCHAR(200), unidades_por_paquete VARCHAR(100), pedido_minimo VARCHAR(200) (MOQ), precio_unitario_ref NUMERIC(10,2) CHECK >0, precio_total_ref NUMERIC(12,2), moneda VARCHAR(3) DEFAULT 'USD', terminos_comerciales TEXT, estado ENUM `estado_producto` (`ACTIVO`,`DESHABILITADO_POR_PROVEEDOR`,`OCULTO_POR_ADMIN`,`ELIMINADO_LOGICO`), motivo_moderacion TEXT, es_recomendado BOOLEAN DEFAULT FALSE, es_promocionado BOOLEAN DEFAULT FALSE, tiene_oferta BOOLEAN DEFAULT FALSE, fecha_publicacion TIMESTAMPTZ DEFAULT NOW(), fecha_actualizacion TIMESTAMPTZ, fecha_eliminacion TIMESTAMPTZ, eliminado_por BIGINT FK → `usuarios.id`)
- `producto_especificaciones` (producto_id FK, clave VARCHAR(100), valor VARCHAR(300), UNIQUE producto_id+clave) — normalización de `specs` (en cliente es objeto `{ "Factor de forma":"M.2 2280"}`, en proveedor es string `"M.2 2280, PCIe..."`)
- `imagenes_producto` (producto_id FK, url_imagen VARCHAR(1000), orden SMALLINT DEFAULT 0)

**Demo 7 productos proveedor (INITIAL_PROVIDER_PRODUCTS:33):**
- `prod_1` SSD Kingston NV2 1TB — `ssd`, `VENTA_MAYORISTA_TRADICIONAL`, `Caja Máster Sellada`, `20 unidades por caja`, `MOQ 2 Cajas (40)`, `$44.50`, `ACTIVO`, `es_promocionado=true`
- `prod_2` RAM Corsair DDR5 32GB — `ram`, `VENTA_MAYORISTA_TRADICIONAL`, `Caja Máster 15 kits`, `MOQ 1 Caja (15 kits)`, `$98`, `ACTIVO`, `tiene_oferta=true`
- `prod_7` Fuente Gigabyte 850W — `psu`, `ACTIVO`, `tiene_oferta=true`
- `prod_10` WD Black SN850X 2TB — `ssd`, `ACTIVO`
- `prod_11` RAM Kingston Fury 16GB — `ram`, `DESHABILITADO` (botón Desactivado)
- `prod_12` RTX 4070 Ti 16GB — `gpu`, `DISTRIBUCION_EXCLUSIVA`, `Palé 12 unidades`, `$795`, `ACTIVO`, `es_promocionado=true`
- `prod_13` Lote 100x USB IronKey — `usb`, `LOTE_STOCK`, `ELIMINADO_LOGICO` (en histórico)
- Admin `prod_99_violating` Refrigeradora Mabe — `OCULTO_POR_ADMIN` (fuera de rubro, `motivo_moderacion`)

---

## Endpoints Públicos (Lectura)

### GET /api/productos

**Vistas:** `HomeView.jsx` (bloques Recomendados/Promocionados/Generales) + `ExploreProductsView.jsx` (catálogo con búsqueda y filtros múltiples) — `06_API_CLIENTE.md:36` ya documenta `GET /api/products` con filtros `search, category, commercialModel, minPrice, maxPrice, sortBy, page, size`; aquí se detalla la capa producto.

**Acceso:** PUBLIC

**Response HTTP 200:** `Page<ProductoResumenDTO>` — `SELECT * FROM productos WHERE estado='ACTIVO' AND (titulo ILIKE '%search%' OR descripcion ILIKE '%search%' OR to_tsvector('spanish', titulo||descripcion) @@ plainto_tsquery) AND categoria_id=:categoria AND modelo_comercial=:modeloComercial::modelo_comercial AND precio_unitario_ref BETWEEN :minPrice AND :maxPrice ORDER BY ... LIMIT :size OFFSET :page*:size`

**DTO incluye:** `id (uuid)`, `titulo`, `categoria`, `categoriaNombre`, `proveedorId`, `proveedorNombre`, `proveedorLogo`, `imagenes[]` (de `imagenes_producto` orden 0), `modelo_comercial`, `tipo_formato`, `pedido_minimo`, `precio_unitario_ref`, `es_recomendado`, `es_promocionado`, `tiene_oferta`.

### GET /api/productos/{productoId}

**Vista:** `ProductDetailView.jsx` — galería múltiples imágenes con selección, ficha `specs` tabla clave-valor (de `producto_especificaciones`), `modelo_comercial`, `tipo_formato`, `unidades_por_paquete`, `pedido_minimo`, `precio_unitario_ref`, `precio_total_ref`, `moneda`, `terminos_comerciales`, badges `Recomendado/Promocionado/Oferta`, bloque proveedor (`logo, nombre`), botones `Contactar` → chat y `Ver Perfil`.

**Path Params:** `productoId` UUID

**SQL:** `SELECT productos.*, array_agg(producto_especificaciones.clave||': '||valor) AS specs, array_agg(imagenes_producto.url_imagen ORDER BY orden) AS imagenes FROM productos LEFT JOIN producto_especificaciones ON ... LEFT JOIN imagenes_producto ON ... WHERE productos.uuid=:productoId AND productos.estado='ACTIVO' GROUP BY productos.id` + join `perfiles_proveedor` + si `tiene_oferta=true` join `ofertas` vigente `estado='ACTIVA'` para `offerDiscount/validUntil/originalPrice`.

**Response HTTP 200:** `ProductoDetalleDTO`
**HTTP 404:** no existe, `ELIMINADO_LOGICO`, `OCULTO_POR_ADMIN` o `DESHABILITADO_POR_PROVEEDOR` (no público).

### GET /api/proveedores/{proveedorId}/catalogo

**Vista:** `ProviderProfileView.jsx` catálogo exclusivo del proveedor — ya en `15_API_PROVEEDORES.md` — `SELECT * FROM productos WHERE proveedor_id=:proveedorId AND estado='ACTIVO' AND categoria_id=:categoria AND titulo ILIKE '%search%'`.

### GET /api/home/contenido-destacado

**Vista:** `HomeView.jsx` 5 bloques — ya en `06_API_CLIENTE.md` — `SELECT ... WHERE es_recomendado=true / es_promocionado=true` (ver `12` y `13`).

---

## Endpoints Proveedor (CRUD propio)

### POST /api/proveedores/yo/productos

**Vista:** `CreateEditProductView.jsx` — formulario `Crear` con campos requeridos observados (`title`, `category`, `imageUrl`/`imageUrls[]`, `commercialModel`, `formatType`, `unitsPerPackage`, `moq`, `referencePriceUnit`, `specs`, `description`, `commercialTerms`).

**Request Body:**
```json
{
  "titulo": "SSD Kingston NV2 1TB PCIe 4.0 NVMe M.2 (2280)",
  "categoriaId": "ssd",
  "urlsImagen": ["https://url1.jpg", "https://url2.jpg"],
  "modeloComercial": "VENTA_MAYORISTA_TRADICIONAL",
  "tipoFormato": "Caja Máster Sellada",
  "unidadesPorPaquete": "20 unidades por caja",
  "pedidoMinimo": "2 Cajas Máster (40 unidades)",
  "precioUnitarioRef": 44.50,
  "moneda": "USD",
  "especificaciones": { "Factor de forma": "M.2 2280", "Interfaz": "PCIe 4.0 x4 NVMe" },
  "descripcion": "Excelente almacenamiento para ensamblajes masivos...",
  "terminosComerciales": "Pago al contado o transferencia confirmada para despacho inmediato."
}
```

**Campos NO en body (calculados/asignados):** `moneda` siempre `USD` si no se envía, `estado='ACTIVO'`, `es_promocionado=false`, `es_recomendado=false`, `tiene_oferta=false`, `precio_total_ref = precio_unitario_ref * parseInt(unidades_por_paquete)` (descriptivo, solo si es numérico; en demo `20 unidades por caja` es VARCHAR no calculable exacto, se guarda `precio_total_ref` si se envía o NULL), `proveedor_id = auth.proveedorId`, `uuid=gen_random_uuid()`.

**Validaciones (`CreateEditProductView.jsx:457`):**
- `titulo` no vacío, máx 500
- `categoriaId` debe existir y `categorias.estado='ACTIVA'`
- `precioUnitarioRef >0` (CHECK)
- `modeloComercial` en ENUM
- `urlsImagen` al menos 1 (no vacío)

**Lógica:**
1. Validar `categoriaId` ACTIVE
2. `INSERT INTO productos (proveedor_id, categoria_id, titulo, ...) RETURNING id, uuid`
3. `INSERT INTO imagenes_producto (producto_id, url_imagen, orden) VALUES` (orden 0 = principal)
4. `INSERT INTO producto_especificaciones (producto_id, clave, valor) VALUES` (descomponer objeto `especificaciones`)
5. `UPDATE categorias SET total_productos = total_productos + 1 WHERE id=:categoriaId`

**Response HTTP 201:** `ProductoDTO` completo
**HTTP 404:** categoria no existe o inactiva

### GET /api/proveedores/yo/productos

**Vista:** `ProductManagementView.jsx` — lista TODOS los productos del proveedor (excluye `ELIMINADO_LOGICO`), cards con `estado` badge `Activo en Catálogo` / `Desactivado (Oculto)`, `isPromoted` badge, `isOffer` badge, filtros Tabs `Todos | Activos | Deshabilitados` + búsqueda interna + contadores `nonDeletedProducts.length`.

**Query Params:** `estado` (ACTIVO | DESHABILITADO_POR_PROVEEDOR, opcional — si no se envía retorna ambos), `search` (título ILIKE), `page`, `size`

**SQL:** `SELECT * FROM productos WHERE proveedor_id=:auth.proveedorId AND estado != 'ELIMINADO_LOGICO' AND (estado=:estado OR :estado IS NULL) AND titulo ILIKE '%search%' ORDER BY fecha_publicacion DESC`

**Response HTTP 200:** `Page<ProductoProveedorDTO>` (incluye `estado`, `es_promocionado`, `tiene_oferta` para badges).

### PUT /api/proveedores/yo/productos/{productoId}

**Vista:** `CreateEditProductView.jsx` modo edición (navega con `editingProductId`) — mismos campos que POST, todos opcionales (solo se actualizan enviados).

**Validaciones:**
- `productos.proveedor_id = auth.proveedorId` (si no, `403`)
- `productos.estado != 'ELIMINADO_LOGICO'` (si ya eliminado, `404`/`409`)

**Lógica:** `UPDATE productos SET titulo=COALESCE(:titulo,titulo), ... WHERE id=:productoId` + `DELETE + INSERT` para `imagenes_producto` y `producto_especificaciones` si se enviaron.

**Response HTTP 200**

### PATCH /api/proveedores/yo/productos/{productoId}/estado

**Vista:** Botón `Desactivar` / `Activar` en `ProductManagementView.jsx` y `ProviderProfileHomeView.jsx` — `toggleProductStatus` → `ACTIVE ↔ DESHABILITADO_POR_PROVEEDOR` con toast `Producto Deshabilitado (oculto al público)` / `Producto Activado`.

**Request Body:** `{ estado: "ACTIVO" | "DESHABILITADO_POR_PROVEEDOR" }` (en API inglés `ACTIVE|DISABLED`, mapea a español DB)

**Response HTTP 200:** `{ id, estado }`

### DELETE /api/proveedores/yo/productos/{productoId}

**Vista:** Botón `Eliminar` con confirmación → `deleteProduct` → `ELIMINADO_LOGICO` con modal `Eliminación Lógica Aplicada`.

**Lógica:**
1. Verificar propietario y no ya `ELIMINADO_LOGICO`
2. `UPDATE productos SET estado='ELIMINADO_LOGICO', fecha_eliminacion=NOW(), eliminado_por=auth.usuarioId WHERE id=:productoId`
3. `INSERT INTO registros_auditoria (tipo_entidad='PRODUCTO', id_entidad_original=:productoId, titulo_entidad=:titulo, usuario_eliminador_id=auth.usuarioId, rol_eliminador='PROVEEDOR', datos_snapshot=to_jsonb(productoAntes), estado='ARCHIVADO')`
4. Si `es_promocionado=true`: `UPDATE promociones SET estado='CANCELADA', nota_moderacion='Producto eliminado por proveedor' WHERE producto_id=:productoId AND estado='ACTIVA'` + `productos.es_promocionado=false`
5. Si `tiene_oferta=true`: `UPDATE ofertas SET estado='ELIMINADA_LOGICA' WHERE producto_id=:productoId AND estado='ACTIVA'` + `productos.tiene_oferta=false`
6. Si `estado previo='ACTIVO'`: `UPDATE categorias SET total_productos = total_productos -1 WHERE id=:categoriaId`

**Response HTTP 204**

---

## Endpoints Administrador (Moderación)

### GET /api/admin/productos

**Vista:** `AdminProductsModerationView.jsx:21` — tabla global TODOS los productos + filtros `categoria`, `proveedorId`, `estado` (`ACTIVO|DESHABILITADO_POR_PROVEEDOR|OCULTO_POR_ADMIN|ELIMINADO_LOGICO`), búsqueda `titulo`.

**Query Params:** `categoria`, `proveedorId`, `estado`, `search`, `page`, `size`

**SQL:** `SELECT productos.*, perfiles_proveedor.razon_social FROM productos JOIN perfiles_proveedor ON productos.proveedor_id=perfiles_proveedor.id WHERE ...`

### GET /api/admin/productos/{productoId}

Modal `Ver detalles completos` con `specs`, `descripcion`, `terminos_comerciales`.

### PUT /api/admin/productos/{productoId}

**Descripción:** `moderateProduct` — admin edita cualquier campo (mismo DTO que proveedor) para corregir.

### PATCH /api/admin/productos/{productoId}/visibilidad

**Vista:** Botones `Ocultar/Restaurar` → `toggleProductVisibilityAdmin` con motivo exigido.

**Body:** `{ estado: "ACTIVO" | "OCULTO_POR_ADMIN", motivo?: string }` → `UPDATE productos SET estado=:estado, motivo_moderacion=:motivo WHERE id=:productoId`

**Ejemplo demo:** `prod_99_violating` `HIDDEN_BY_ADMIN` con `motivo_moderacion: "Publicación fuera de rubro (Electrodomésticos no permitidos)"`.

### PATCH /api/admin/productos/{productoId}/recomendacion

**Vista:** Toggle `Recomendado` en `AdminProductsModerationView` y `AdminRecommendationsView`.

**Body:** `{ es_recomendado: boolean }` → `UPDATE productos SET es_recomendado=:boolean`

### DELETE /api/admin/productos/{productoId}

**Body:** `{ motivo: string }` — `deleteProductAdmin:156` — mismo flujo que proveedor pero `rol_eliminador='ADMIN'` y `eliminado_por=auth.adminId`.

---

## Máquina de Estados (observada en `03_MODELO_RELACIONAL.md:334`)

```
ACTIVO ──[proveedor deshabilita]──→ DESHABILITADO_POR_PROVEEDOR
DESHABILITADO_POR_PROVEEDOR ──[proveedor reactiva]──→ ACTIVO

ACTIVO ──[admin oculta]──→ OCULTO_POR_ADMIN
OCULTO_POR_ADMIN ──[admin restaura]──→ ACTIVO

ACTIVO ──[proveedor/admin elimina]──→ ELIMINADO_LOGICO (terminal)
DESHABILITADO_POR_PROVEEDOR ──[admin elimina]──→ ELIMINADO_LOGICO
OCULTO_POR_ADMIN ──[admin elimina]──→ ELIMINADO_LOGICO
```

**Reglas:**
- Proveedor no puede establecer `OCULTO_POR_ADMIN` ni `ELIMINADO_LOGICO` vía `PATCH /estado` (solo vía `DELETE`).
- Admin puede establecer cualquier estado.
- `ELIMINADO_LOGICO` es terminal (no hay `PUT` ni `PATCH` posterior).
