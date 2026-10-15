# 14. API de Categorías y Tipos

> Plataforma COMPUNEX B2B — Endpoints de Categorías — PostgreSQL
> Fuente única: `git/diseno_administrador/src/components/views/AdminCategoriesView.jsx` + `git/diseno_administrador/src/context/AdminContext.jsx:237` `createCategory/updateCategory/toggleCategoryStatus` + `git/diseno_administrador/src/data/adminMockData.js:183` `INITIAL_ADMIN_CATEGORIES` + `git/diseno_cliente/src/components/views/ExploreProductsView.jsx` filtros + `git/diseno_proveedor/src/components/views/CreateEditProductView.jsx` dropdown categorías
> No se inventan categorías fuera de las 8 existentes.

---

## Modelo PostgreSQL

**Tablas:** `categorias` (id VARCHAR(50) PK textual: `ram`, `ssd`, `gpu`, `cpu`, `motherboard`, `hdd`, `psu`, `usb`) + `subcategorias` (id BIGSERIAL PK, categoria_id FK, nombre VARCHAR(100)) + campo desnormalizado `categorias.total_productos` INT DEFAULT 0 para dashboard.

**Demo 8 categorías (fijas en seed `04_SQL_DATABASE.sql:categorias`):**
- `ram` Memorias RAM — `Cpu` — sub: DDR4, DDR5, SODIMM Laptop, RAM Servidor
- `ssd` Discos SSD & NVMe — `HardDrive` — sub: M.2 PCIe 4.0/5.0, SATA III 2.5", SSD Externo
- `gpu` Tarjetas Gráficas — `Layers` — sub: GeForce RTX 4000, Radeon RX 7000, Workstation Pro
- `cpu` Procesadores — `Zap` — sub: Intel 14ª/13ª Gen, AMD AM5/AM4
- `motherboard` Placas Madre — `Grid` — chipsets Z790/B760, X670/B650, Micro-ATX, Mini-ITX
- `hdd` Discos Duros — `Database` — sub: Surveillance, NAS, Desktop
- `psu` Fuentes de Poder — `BatteryCharging` — sub: 80+ Bronze/Gold/Platinum
- `usb` Memorias USB — `Usb` — sub: USB 3.2, Tipo-C, MicroSD

Cada categoría tiene `estado` ENUM `ACTIVA|INACTIVA` y `total_productos`.

---

## Endpoints Públicos (lectura)

### GET /api/categorias

**Vista:** `ExploreProductsView.jsx` (filtros rápidos por categoría) + `CreateEditProductView.jsx` (dropdown `Categoría tecnológica`) + `HomeView` + `AboutView` + `AdminCategoriesView` árbol.

**Acceso:** PUBLIC

**Response HTTP 200:** `List<CategoriaDTO>`
```json
[
  {
    "id": "ram",
    "nombre": "Memorias RAM",
    "icono": "Cpu",
    "descripcion": "Módulos DDR4, DDR5, SODIMM para PC y Laptops",
    "estado": "ACTIVA",
    "totalProductos": 14,
    "subcategorias": ["DDR4", "DDR5", "SODIMM Laptop", "RAM Servidor"]
  }
]
```

**SQL:** `SELECT categorias.*, array_agg(subcategorias.nombre) AS subcategorias FROM categorias LEFT JOIN subcategorias ON subcategorias.categoria_id=categorias.id WHERE categorias.estado='ACTIVA' GROUP BY categorias.id ORDER BY categorias.nombre`

**Nota:** `totalProductos` es `categorias.total_productos` desnormalizado, actualizado al crear/eliminar `productos`.

### GET /api/categorias/{categoriaId}

**Response HTTP 200:** `CategoriaDTO` con subcategorías `SELECT * FROM subcategorias WHERE categoria_id=:id`

**HTTP 404:** no existe.

---

## Endpoints Administrador

### POST /api/admin/categorias

**Vista:** `AdminCategoriesView.jsx` — botón `Crear nueva categoría` → modal `nombre, subcategorías (lista)` → `createCategory`.

**Request Body:**
```json
{
  "nombre": "Refrigeración",
  "icono": "Snowflake",
  "descripcion": "Coolers y refrigeración líquida",
  "subcategorias": ["Aire", "Líquida 240mm", "Líquida 360mm"]
}
```

**Validaciones:**
- `nombre` no vacío, máximo 100, único (no duplicado case-insensitive)
- `subcategorias` array no vacío, cada elemento no vacío, máximo 100, sin duplicados

**Lógica:**
1. Generar `id = lower(replace(nombre, ' ', '_'))` (ej: `refrigeracion`) — PK textual
2. `INSERT INTO categorias (id, nombre, icono, descripcion, estado='ACTIVA', total_productos=0)`
3. `INSERT INTO subcategorias (categoria_id, nombre) VALUES (...)` por cada subcategoría

**Response HTTP 201:** `CategoriaDTO` creada.
**HTTP 409:** nombre ya existe.

### PUT /api/admin/categorias/{categoriaId}

**Vista:** `Editar categoría existente` → modal.

**Request Body:** mismo que POST (`nombre`, `icono`, `descripcion`, `subcategorias[]`)

**Lógica:**
1. `UPDATE categorias SET nombre=:nombre, icono=:icono, descripcion=:descripcion WHERE id=:categoriaId`
2. `DELETE FROM subcategorias WHERE categoria_id=:categoriaId` + reinsertar lista (reemplazo completo, simple y trazable en demo).

**Response HTTP 200**

### PATCH /api/admin/categorias/{categoriaId}/estado

**Vista:** Botón `Activar/Desactivar categoría` en `AdminCategoriesView` — `toggleCategoryStatus`.

**Request Body:** `{ estado: "ACTIVA" | "INACTIVA" }`

**Lógica:** `UPDATE categorias SET estado=:estado WHERE id=:categoriaId`

**Efecto:** `GET /api/categorias` (público) filtra solo `ACTIVA`; `CreateEditProductView` dropdown valida `categorias.estado='ACTIVA'` al crear `productos` (`07_API_PROVEEDOR.md:147` validación `categoryId` ACTIVE).

**Response HTTP 200**

---

## Reglas Observadas

- **Categorías gestionadas solo por ADMIN:** Cliente y Proveedor solo leen (`ExploreProductsView`, `CreateEditProductView`); no pueden crear/editar.
- **No hay DELETE físico de categorías:** No existe botón Eliminar en `AdminCategoriesView`; solo `ACTIVA ↔ INACTIVA`. Borrado físico rompería FK `productos.categoria_id`.
- **Productos_count desnormalizado:** `AdminDashboardView` no lo muestra, pero `AdminCategoriesView` muestra `productsCount` por categoría (de `INITIAL_ADMIN_CATEGORIES`). Se incrementa en `POST /providers/me/products` (`+1`) y decrementa en `DELETE` lógica (`-1` si estaba ACTIVO).
