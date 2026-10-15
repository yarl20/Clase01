# 06. API del Cliente / Comprador

> Plataforma COMPUNEX B2B — Endpoints del Rol CLIENT

---

## GET /api/home/featured-content

**Descripción:** Obtiene el contenido organizado de la página de inicio del cliente.

**Acceso:** PUBLIC (sin autenticación requerida; si hay token se personalizan resultados)

**Query Params:**
- `clientId` (string, opcional) — UUID del cliente para personalizar por preferencias

**Response HTTP 200:**
```json
{
  "recommendedProducts": [ ProductSummaryDTO... ],
  "promotedProducts": [ ProductSummaryDTO... ],
  "generalProducts": [ ProductSummaryDTO... ],
  "activeOffers": [ OfferSummaryDTO... ],
  "featuredProviders": [ ProviderSummaryDTO... ]
}
```

**Lógica (PostgreSQL):**
1. `recommendedProducts`: SELECT * FROM productos WHERE es_recomendado=true AND estado='ACTIVO' ORDER BY fecha_publicacion DESC LIMIT 8
2. `promotedProducts`: SELECT * FROM productos WHERE es_promocionado=true AND estado='ACTIVO' ORDER BY RANDOM() LIMIT 8 -- PostgreSQL: RANDOM() (distribución dinámica)
3. `activeOffers`: SELECT * FROM ofertas JOIN productos ON ofertas.producto_id=productos.id WHERE ofertas.estado='ACTIVA' AND productos.estado='ACTIVO' LIMIT 6
4. `featuredProviders`: SELECT * FROM perfiles_proveedor JOIN usuarios ON perfiles_proveedor.usuario_id=usuarios.id WHERE destacado=true AND usuarios.estado='ACTIVO'
5. `generalProducts`: SELECT * FROM productos WHERE estado='ACTIVO' AND es_recomendado=false ORDER BY fecha_publicacion DESC LIMIT 12

---

## GET /api/products

**Descripción:** Catálogo general de productos con búsqueda y filtros.

**Acceso:** PUBLIC

**Query Params:**
| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `search` | string | null | Búsqueda en título y descripción |
| `category` | string | null | ID de categoría (ej. "ram") |
| `commercialModel` | string | null | WHOLESALE_TRADITIONAL \| EXCLUSIVE_DISTRIBUTION \| STOCK_LOT |
| `minPrice` | decimal | null | Precio mínimo por unidad (USD) |
| `maxPrice` | decimal | null | Precio máximo por unidad (USD) |
| `sortBy` | string | "relevance" | relevance \| price_asc \| price_desc \| newest |
| `page` | int | 0 | Página (0-indexed) |
| `size` | int | 12 | Resultados por página |

**Response HTTP 200:**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "SSD Kingston NV2 1TB PCIe 4.0",
      "category": "ssd",
      "categoryName": "Discos SSD & NVMe",
      "providerId": "uuid-proveedor",
      "providerName": "TechNova Mayorista S.A.C.",
      "providerLogo": "https://...",
      "images": ["https://..."],
      "commercialModel": "WHOLESALE_TRADITIONAL",
      "commercialModelLabel": "Venta Mayorista Tradicional",
      "formatType": "Caja Máster Sellada",
      "moq": "2 Cajas Máster (40 unidades)",
      "referencePriceUnit": 44.50,
      "currency": "USD",
      "isRecommended": true,
      "isPromoted": true,
      "isOffer": false
    }
  ],
  "totalElements": 85,
  "totalPages": 8,
  "currentPage": 0,
  "size": 12
}
```

**Filtros en SQL (PostgreSQL):**
```sql
WHERE estado = 'ACTIVO'
  AND (titulo ILIKE '%search%' OR descripcion ILIKE '%search%'
       OR to_tsvector('spanish', titulo || ' ' || descripcion) @@ plainto_tsquery('spanish', :search))
  AND categoria_id = :categoriaId
  AND modelo_comercial = :modeloComercial::modelo_comercial
  AND precio_unitario_ref BETWEEN :minPrice AND :maxPrice
ORDER BY (sortBy logic) -- relevance: ts_rank, precio: precio_unitario_ref ASC/DESC
```

---

## GET /api/products/{productId}

**Descripción:** Obtiene el detalle completo de un producto.

**Acceso:** PUBLIC

**Path Params:**
- `productId` (string) — UUID del producto

**Response HTTP 200:**
```json
{
  "id": "uuid",
  "title": "SSD Kingston NV2 1TB PCIe 4.0 NVMe M.2 (2280)",
  "category": "ssd",
  "categoryName": "Discos SSD & NVMe",
  "images": ["https://img1", "https://img2"],
  "specs": {
    "Factor de forma": "M.2 2280",
    "Interfaz": "PCIe 4.0 x4 NVMe",
    "Velocidad de lectura": "Hasta 3,500 MB/s"
  },
  "description": "Excelente almacenamiento para ensambles masivos...",
  "commercialModel": "WHOLESALE_TRADITIONAL",
  "commercialModelLabel": "Venta Mayorista Tradicional",
  "formatType": "Caja Máster Sellada",
  "unitsPerPackage": "20 unidades por caja",
  "moq": "2 Cajas Máster (40 unidades en total)",
  "referencePriceUnit": 44.50,
  "referencePriceTotal": 1780.00,
  "currency": "USD",
  "commercialTerms": "Pago al contado. Facturación electrónica formal.",
  "isRecommended": true,
  "isPromoted": true,
  "isOffer": false,
  "offer": null,
  "provider": {
    "id": "uuid-proveedor",
    "companyName": "TechNova Mayorista S.A.C.",
    "tradeName": "TechNova Wholesale",
    "logo": "https://...",
    "city": "Lima, Perú",
    "verified": true,
    "rating": 4.9
  },
  "publishedAt": "2026-08-10T00:00:00Z"
}
```

**Response HTTP 404:** Producto no encontrado, eliminado o no público

**Nota:** Si el producto tiene una oferta activa, el objeto `offer` incluye `offerTitle`, `discountPercent`, `promotionalPrice`, `validUntil`.

---

## GET /api/providers/{providerId}/public-profile

**Descripción:** Perfil público de un proveedor.

**Acceso:** PUBLIC

**Response HTTP 200:**
```json
{
  "id": "uuid-proveedor",
  "companyName": "TechNova Mayorista S.A.C.",
  "tradeName": "TechNova Wholesale",
  "logo": "https://...",
  "banner": "https://...",
  "contactPhone": "+51 (01) 748-9900",
  "email": "ventas.mayoristas@technova.com",
  "address": "Av. Tecnológica 1420",
  "city": "Lima, Perú",
  "yearsInMarket": "12 años",
  "description": "Importadores directos...",
  "commercialPolicy": "Despacho prioritario...",
  "specialties": ["Almacenamiento NVMe", "Memorias RAM"],
  "brands": ["Kingston", "Corsair"],
  "verified": true,
  "rating": 4.9,
  "totalProductsCount": 38
}
```

**Response HTTP 404:** Proveedor no encontrado o inactivo

---

## GET /api/providers/{providerId}/catalog

**Descripción:** Catálogo de productos activos de un proveedor específico.

**Acceso:** PUBLIC

**Query Params:** `search`, `category`, `page`, `size`

**Response HTTP 200:** Page<ProductSummaryDTO> (solo productos ACTIVE del proveedor)

---

## GET /api/clients/me/profile

**Descripción:** Perfil del cliente autenticado.

**Acceso:** CLIENT

**Response HTTP 200:**
```json
{
  "id": "uuid",
  "name": "Carlos Mendoza R.",
  "email": "compras@tecnostore.com",
  "storeName": "TecnoStore Express",  "phone": "+51 987 654 321",
  "city": "Arequipa",
  "address": "Av. Comercial 740, Tienda 12",
  "registeredAt": "2026-01-12T00:00:00Z"
}
```

---

## PUT /api/clients/me/profile

**Descripción:** Actualiza el perfil del cliente autenticado.

**Acceso:** CLIENT

**Request Body:**
```json
{
  "name": "Carlos Mendoza R.",
  "storeName": "TecnoStore Express & Tech",
  "phone": "+51 987 654 321",
  "city": "Arequipa",
  "address": "Av. Comercial 740, Tienda 12"
}
```

**Nota:** El campo `email` no es modificable desde este endpoint (requiere flujo especial de cambio de email si se implementa en el futuro). `contactPhone` tampoco es modificable por el cliente.

**Response HTTP 200:** ClientProfileDTO actualizado
**Response HTTP 400:** Datos inválidos

---

## GET /api/clients/me/preferences

**Descripción:** Obtiene las preferencias del cliente autenticado.

**Acceso:** CLIENT

**Response HTTP 200:**
```json
{
  "selectedCategories": ["ram", "ssd", "gpu"],
  "selectedCommercialModels": ["WHOLESALE_TRADITIONAL", "STOCK_LOT"],
  "keywords": ["Kingston", "NVMe M.2", "DDR5"]
}
```

---

## PUT /api/clients/me/preferences

**Descripción:** Actualiza las preferencias del cliente.

**Acceso:** CLIENT

**Request Body:**
```json
{
  "selectedCategories": ["ram", "ssd"],
  "selectedCommercialModels": ["WHOLESALE_TRADITIONAL"],
  "keywords": ["Kingston", "RTX 4060"]
}
```

**Response HTTP 200:** ClientPreferencesDTO actualizado

**Validaciones:**
- `selectedCategories`: cada elemento debe existir en la tabla `categories`
- `selectedCommercialModels`: valores válidos: WHOLESALE_TRADITIONAL, EXCLUSIVE_DISTRIBUTION, STOCK_LOT
- `keywords`: máximo 20 elementos, cada uno máximo 100 chars

---

## POST /api/applications/provider

**Descripción:** Envío de solicitud de empresa para convertirse en proveedor.

**Acceso:** PUBLIC

**Request Body:**
```json
{
  "companyName": "ByteMax Mayoristas Perú S.A.C.",  "contactName": "Jorge Benavides Larrea",
  "email": "jbenavides@bytemax.pe",
  "phone": "+51 991 445 220",
  "city": "Lima",
  "address": "Av. Industrial 890, Los Olivos",
  "brands": "Intel, Kingston, Western Digital",
  "message": "Somos importadores directos de procesadores..."
}
```

**Response HTTP 201:**
```json
{
  "id": 1,
  "companyName": "ByteMax Mayoristas Perú S.A.C.",
  "status": "PENDING",
  "appliedAt": "2026-09-01T22:00:00Z",
  "message": "Su solicitud ha sido recibida. El equipo de COMPUNEX B2B la revisará en los próximos días hábiles."
}
```

**Response HTTP 409:** Ya existe una solicitud activa o aprobada con el mismo Identificaci�n comercial

**Validaciones:**
- `companyName`: no vacío
- `contactPhone`: no vacío, verificar que no tenga una solicitud PENDING o APPROVED con el mismo Identificaci�n comercial
- `contactName`: no vacío
- `email`: formato válido
- `phone`: no vacío
- `message`: no vacío

---

## GET /api/content/about

**Descripción:** Contenido público de la sección "Nosotros".

**Acceso:** PUBLIC

**Response HTTP 200:**
```json
{
  "title": "El Ecosistema Mayorista...",
  "subtitle": "Conectamos de forma ágil...",
  "mission": "Digitalizar y transparentar...",
  "pillars": [
    { "title": "Especialización Total", "description": "Enfocados exclusivamente..." },
    { "title": "Proveedores Verificados", "description": "Cada empresa mayorista..." }
  ]
}
```

---

## GET /api/content/contact

**Descripción:** Datos de contacto públicos de la plataforma.

**Acceso:** PUBLIC

**Response HTTP 200:**
```json
{
  "supportEmail": "soporte@compunexb2b.com",
  "providerInquiriesEmail": "proveedores@compunexb2b.com",
  "centralPhone": "+51 (01) 700-4500",
  "whatsappSupport": "+51 999 888 777",
  "officeAddress": "Torre Empresarial Tecnológica, Piso 14, San Isidro, Lima",
  "operatingHours": "Lunes a Viernes: 8:30 AM - 6:30 PM"
}
```

---

## GET /api/categories

**Descripción:** Lista de todas las categorías activas.

**Acceso:** PUBLIC

**Response HTTP 200:**
```json
[
  {
    "id": "ram",
    "name": "Memorias RAM",
    "icon": "Cpu",
    "description": "Módulos DDR4, DDR5, SODIMM...",
    "subcategories": ["DDR4", "DDR5", "SODIMM Laptop"],
    "productsCount": 14
  }
]
```
