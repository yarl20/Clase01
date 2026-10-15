# 01. Análisis General del Sistema — Por Rol y Por Apartado

> Plataforma COMPUNEX B2B — Análisis técnico de funcionalidades encontradas en las demos frontend

---

## ANÁLISIS DE ROL A: CLIENTE / COMPRADOR

### Apartados identificados en la demo `diseno_cliente`:

---

### A-01. Inicio (HomeView)

**Descripción:** Página principal del cliente. Muestra productos categorizados en bloques diferenciados.

**Quién accede:** CLIENTE autenticado (también visible sin autenticación en versión futura)

**Funcionalidades:**
- Visualizar bloque de Productos Recomendados (curados por Administrador)
- Visualizar bloque de Productos Promocionados (publicidad contratada por Proveedor)
- Visualizar bloque de Productos Generales (catálogo orgánico)
- Visualizar sección de Ofertas Vigentes
- Visualizar sección de Proveedores Destacados
- Barra de búsqueda por texto
- Filtro rápido por categoría
- Hacer clic en producto para ir a detalle
- Hacer clic en proveedor para ir a perfil de proveedor
- Botón "Contactar" o "Cotizar" desde card de producto → abre chat

**Información que muestra:**
- Lista de productos con: imagen, título, categoría, modelo comercial, precio referencial, proveedor, badges (Recomendado / Patrocinado / Oferta)
- Lista de proveedores destacados con: logo, nombre, especialidades

**Información que recibe:** Preferencias del cliente para personalizar el orden

**Acciones:**
- `verDetalleProducto(productId)` → navega a ProductDetailView
- `verPerfilProveedor(providerId)` → navega a ProviderProfileView
- `abrirChatConProveedor(providerId, productId)` → navega a ChatView
- `buscar(searchTerm)` → navega a ExploreProductsView con término
- `filtrarPorCategoria(categoryId)` → navega a ExploreProductsView con filtro

**Validaciones:** Ninguna (vista de lectura)

**Estados posibles:**
- Loading de productos
- Sin resultados

**Entidades relacionadas:** Product, Provider, Offer, Promotion, Category, ClientPreference

**Endpoints necesarios:**
```
GET /api/home/featured-content
  → Responde: { recommendedProducts[], promotedProducts[], generalProducts[], activeOffers[], featuredProviders[] }
  → Query params: clientId (opcional para personalización)
```

---

### A-02. Exploración de Productos (ExploreProductsView)

**Descripción:** Vista de catálogo general con búsqueda avanzada y filtros múltiples.

**Quién accede:** CLIENTE

**Funcionalidades:**
- Barra de búsqueda por texto (nombre, marca, descripción)
- Filtro por categoría (RAM, SSD, GPU, CPU, etc.)
- Filtro por modelo comercial (Mayorista Tradicional, Distribución Exclusiva, Stock-lot)
- Filtro por rango de precio (slider)
- Ordenar por: Relevancia, Precio menor, Precio mayor
- Listado paginado de productos activos
- Contador de resultados
- Limpiar filtros

**Información que muestra:** Lista de productos activos con sus datos completos

**Filtros disponibles (encontrados en ClientContext):**
- `searchTerm`
- `selectedCategory` (all | ram | ssd | gpu | cpu | motherboard | hdd | psu | usb)
- `selectedCommercialModel` (all | WHOLESALE_TRADITIONAL | EXCLUSIVE_DISTRIBUTION | STOCK_LOT)
- `priceRange` ([minPrice, maxPrice])
- `sortBy` (relevance | price_asc | price_desc)

**Endpoints necesarios:**
```
GET /api/products
  Query params:
  - search (string, opcional)
  - category (string, opcional)
  - commercialModel (string, opcional)
  - minPrice (number, opcional)
  - maxPrice (number, opcional)
  - sortBy (string, opcional)
  - page (int, default 0)
  - size (int, default 12)
  → Response: Page<ProductSummaryDTO>
  → HTTP 200
```

---

### A-03. Detalle de Producto (ProductDetailView)

**Descripción:** Vista completa de un producto individual con toda su información técnica y comercial.

**Quién accede:** CLIENTE

**Funcionalidades:**
- Ver galería de imágenes (múltiples imágenes con selección)
- Ver ficha técnica con especificaciones (tabla clave-valor)
- Ver modelo de comercialización y formato de empaque
- Ver MOQ (cantidad mínima de pedido)
- Ver precio referencial unitario y total por formato
- Ver moneda (USD)
- Ver términos y condiciones comerciales
- Ver badge de estado (Recomendado / Patrocinado / Oferta Activa)
- Ver información básica del proveedor (logo, nombre)
- Botón "Contactar al Proveedor" → abre chat referenciando el producto
- Botón "Ver Perfil del Proveedor" → navega a ProviderProfileView
- Ver si tiene oferta activa con descuento y validez

**Información que muestra:**
- `title`, `category`, `images[]`, `specs{}`, `description`, `commercialModel`, `formatType`, `unitsPerPackage`, `moq`, `referencePriceUnit`, `referencePriceTotal`, `currency`, `commercialTerms`
- Si tiene oferta: `offerDiscount`, `offerValidUntil`, `originalPriceUnit`
- Flags: `isRecommended`, `isPromoted`, `isOffer`
- Datos del proveedor: `providerName`, `providerLogo`, `providerId`

**Endpoints necesarios:**
```
GET /api/products/{productId}
  → Response: ProductDetailDTO (datos completos del producto + datos del proveedor)
  → HTTP 200 | 404 si no existe o está eliminado/oculto
```

---

### A-04. Perfil Público del Proveedor (ProviderProfileView)

**Descripción:** Vista del perfil público de un proveedor específico, con su información corporativa y catálogo.

**Quién accede:** CLIENTE

**Funcionalidades:**
- Ver banner/portada del proveedor
- Ver logo y nombre de la empresa
- Ver datos de contacto permitidos (teléfono, email, ciudad)
- Ver descripción del proveedor
- Ver especialidades y marcas que maneja
- Ver política comercial
- Ver rating y años en el mercado
- Listado de productos del proveedor (solo ACTIVE)
- Búsqueda de productos dentro del catálogo del proveedor
- Filtro por categoría dentro del catálogo
- Botón "Iniciar Chat" → ChatView referenciando proveedor
- Botón "Cotizar" por producto → ChatView referenciando producto+proveedor
- Contador de productos totales

**Información que muestra:**
- `companyName`, `tradeName`, `logo`, `banner`, `contactPhone`, `email`, `address`, `city`, `description`, `specialties[]`, `brands[]`, `commercialPolicy`, `rating`, `yearsInMarket`, `totalProductsCount`
- Lista de productos activos del proveedor

**Endpoints necesarios:**
```
GET /api/providers/{providerId}/public-profile
  → Response: ProviderPublicProfileDTO
  → HTTP 200 | 404

GET /api/providers/{providerId}/catalog
  Query params:
  - search (string, opcional)
  - category (string, opcional)
  - page (int)
  - size (int)
  → Response: Page<ProductSummaryDTO> (solo productos ACTIVE del proveedor)
  → HTTP 200 | 404
```

---

### A-05. Chat Privado (ChatView)

**Descripción:** Sistema de mensajería privada 1-a-1 entre Cliente y Proveedor.

**Quién accede:** CLIENTE (inicia conversaciones)

**Funcionalidades:**
- Lista de conversaciones activas en panel izquierdo
- Cada conversación muestra: avatar proveedor, nombre, último mensaje, hora, contador de no leídos
- Al seleccionar conversación: abre historial de mensajes
- Identificación visual de mensajes propios (derecha, azul) vs del proveedor (izquierda, gris)
- Campo de texto para escribir mensaje
- Botón Enviar
- Indicador "Proveedor está escribiendo..."
- Marca de lectura en mensajes
- Si la conversación tiene producto referenciado: muestra chip con nombre del producto al inicio
- Crear nueva conversación desde botón "Iniciar Chat con Proveedor"
- Simulación de respuesta automática del proveedor (en demo)
- Timestamp por mensaje

**EstEstructura de datos de chat (encontrada en mockData):**
```
Conversation: {
  id, providerId, providerName, providerLogo,
  lastMessage, lastMessageTime, unreadCount,
  referencedProductId, referencedProductTitle,
  messages: [{ id, sender, text, timestamp, isRead }]
}
```

**Entidades relacionadas:** Conversation, Message, Client, Provider, Product (referencia)

**Endpoints necesarios:**
```
GET /api/chat/conversations
  → Response: ConversationSummaryDTO[]
  → Requiere autenticación CLIENTE

POST /api/chat/conversations
  Body: { providerId, productId? (opcional), initialMessage }
  → Response: ConversationDTO
  → HTTP 201 | 400

GET /api/chat/conversations/{conversationId}/messages
  Query params: page, size
  → Response: Page<MessageDTO>
  → HTTP 200 | 403 (si no participante) | 404

POST /api/chat/conversations/{conversationId}/messages
  Body: { text }
  → Response: MessageDTO
  → HTTP 201

PUT /api/chat/conversations/{conversationId}/read
  → Marca todos los mensajes como leídos
  → HTTP 204
```

---

### A-06. Preferencias del Cliente (PreferencesView)

**Descripción:** Panel de configuración de preferencias de búsqueda y catálogo del cliente.

**Quién accede:** CLIENTE

**Funcionalidades:**
- Selección/deselección de categorías de interés (checkboxes con iconos)
- Selección de modelos comerciales de interés (chips/tags)
- Agregar palabras clave o términos de búsqueda favoritos (campo de texto + botón Agregar)
- Eliminar términos de búsqueda individualmente
- Botón "Guardar Preferencias"
- Feedback visual al guardar (Toast)

**Información que muestra:**
- `selectedCategories: string[]` — IDs de categorías activas
- `selectedCommercialModels: string[]` — modelos de comercialización
- `keywords: string[]` — términos de búsqueda libres

**Cómo se usan:** Personalizan el orden/contenido del Home del cliente (sin anular espacios publicitarios contratados)

**Endpoints necesarios:**
```
GET /api/clients/me/preferences
  → Response: ClientPreferencesDTO
  → HTTP 200

PUT /api/clients/me/preferences
  Body: { selectedCategories[], selectedCommercialModels[], keywords[] }
  → Response: ClientPreferencesDTO actualizado
  → HTTP 200 | 400
```

---

### A-07. Cuenta del Cliente (AccountView)

**Descripción:** Panel de perfil y configuración de cuenta del cliente.

**Quién accede:** CLIENTE

**Funcionalidades:**
- Ver datos personales actuales (nombre, email, tienda, contactPhone, teléfono, ciudad, dirección, fecha de registro)
- Editar datos personales → formulario inline con botón Guardar
- Cambiar contraseña → formulario con: contraseña actual, nueva contraseña, confirmar contraseña
- Validación de que nueva contraseña === confirmar contraseña
- Feedback visual de guardado exitoso

**Información que muestra/modifica:**
- `name`, `email`, `storeName`, `phone`, `city`, `address`
- Campo de contraseña (solo para cambio, nunca se muestra la actual)

**Endpoints necesarios:**
```
GET /api/clients/me/profile
  → Response: ClientProfileDTO
  → HTTP 200

PUT /api/clients/me/profile
  Body: { name, storeName, phone, city, address }
  → Response: ClientProfileDTO
  → HTTP 200 | 400

PUT /api/clients/me/password
  Body: { currentPassword, newPassword }
  → HTTP 204 | 400 (contraseña actual incorrecta) | 422
```

---

### A-08. Sección "Nosotros" (AboutView)

**Descripción:** Página institucional con información sobre la plataforma. Solo lectura para el cliente.

**Quién accede:** Cualquier visitante / CLIENTE

**Funcionalidades:**
- Ver título y subtítulo institucional
- Ver misión de la plataforma
- Ver pilares (cards con título y descripción)
- Ver categorías de productos manejados por la plataforma

**Información que muestra:** Contenido gestionado por el Administrador

**Endpoints necesarios:**
```
GET /api/content/about
  → Response: AboutContentDTO
  → HTTP 200
```

---

### A-09. Sección "Contacto" (ContactView)

**Descripción:** Página de contacto y formulario para solicitar ser proveedor.

**Quién accede:** Cualquier visitante / CLIENTE

**Funcionalidades:**
- Ver canales de contacto: email de soporte, email de proveedores, teléfono central, WhatsApp, dirección, horarios
- Formulario de Solicitud de Proveedor con campos:
  - Nombre de la empresa
  - 
  - Nombre del contacto
  - Email
  - Teléfono
  - Ciudad
  - Dirección
  - Marcas/productos que manejan
  - Mensaje / descripción de la empresa
  - Botón "Enviar Solicitud"
- Confirmación visual de envío exitoso (Toast)

**Información que genera:** Solicitud de proveedor (ProviderApplication)

**Endpoints necesarios:**
```
GET /api/content/contact
  → Response: ContactInfoDTO
  → HTTP 200

POST /api/applications/provider
  Body: { companyName, contactPhone, contactName, email, phone, city, address, brands, message }
  → Response: ApplicationDTO
  → HTTP 201 | 400 | 409 (si Identificaci�n comercial ya tiene solicitud pendiente/aprobada)
```

---

## ANÁLISIS DE ROL B: PROVEEDOR

### Apartados identificados en la demo `diseno_proveedor`:

---

### B-01. Perfil Principal / Home del Proveedor (ProviderProfileHomeView)

**Descripción:** Página de inicio exclusiva del proveedor. Muestra su perfil público editable y sus productos.

**Quién accede:** PROVEEDOR autenticado

**Funcionalidades:**
- Ver banner corporativo
- Ver logo, nombre de empresa, nombre comercial
- Ver datos: contactPhone (Identificaci�n comercial), teléfono, email, dirección, ciudad, años en mercado, rating, descripción, política comercial
- Ver marcas/especialidades
- Editar información del perfil → modal de edición
  - Campos editables: `companyName`, `tradeName`, `contactPhone`, `address`, `city`, `description`, `commercialPolicy`, `brands[]`
  - Campos NO editables por el proveedor: `email`, `rating`, `verified`
- Ver listado de sus propios productos (ACTIVE + DISABLED) debajo del perfil
- Ver botón "Agregar nuevo producto" (visible cuando no hay productos)
- Botón permanente en header para acceder a gestión de productos

**Información que muestra/modifica:** Perfil completo del proveedor (ver estEstructura en mockData)

**Endpoints necesarios:**
```
GET /api/providers/me/profile
  → Response: ProviderProfileDTO
  → HTTP 200

PUT /api/providers/me/profile
  Body: { companyName, tradeName, contactPhone, address, city, description, commercialPolicy, brands[] }
  → Response: ProviderProfileDTO actualizado
  → HTTP 200 | 400
```

---

### B-02. Gestión de Productos (ProductManagementView)

**Descripción:** Listado completo de todos los productos del proveedor con acciones de gestión.

**Quién accede:** PROVEEDOR

**Funcionalidades:**
- Ver lista de TODOS sus productos (ACTIVE + DISABLED, excluye DELETED_LOGICAL)
- Tarjetas de producto con: imagen, título, categoría, modelo comercial, precio, estado, badges (Promocionado / Oferta)
- Tabs de filtrado: Todos | Activos | Deshabilitados
- Barra de búsqueda interna
- Acción "Editar" → navega a CreateEditProductView en modo edición
- Acción "Deshabilitar" / "Activar" → toggle de estado ACTIVE ↔ DISABLED
- Acción "Eliminar" → confirmación modal → eliminación lógica (DELETED_LOGICAL)
- Botón "Agregar Nuevo Producto"
- Contadores de totales por estado

**Estados de producto:** ACTIVE | DISABLED | DELETED_LOGICAL

**Endpoints necesarios:**
```
GET /api/providers/me/products
  Query params:
  - status (ACTIVE | DISABLED — no incluye DELETED_LOGICAL)
  - search (string)
  - page, size
  → Response: Page<ProviderProductDTO>

PATCH /api/providers/me/products/{productId}/status
  Body: { status: "ACTIVE" | "DISABLED" }
  → HTTP 200 | 403 | 404

DELETE /api/providers/me/products/{productId}
  → Aplica eliminación lógica (status = DELETED_LOGICAL)
  → HTTP 204 | 403 | 404
```

---

### B-03. Crear / Editar Producto (CreateEditProductView)

**Descripción:** Formulario completo de creación y edición de publicaciones de productos.

**Quién accede:** PROVEEDOR

**Campos del formulario (analizados desde el componente):**
- `title` (requerido) — Nombre/título de la publicación
- `category` (requerido) — Categoría tecnológica (dropdown de categorías activas)
- `imageUrl` (requerido) — URL de imagen principal
- `commercialModel` (requerido) — WHOLESALE_TRADITIONAL | EXCLUSIVE_DISTRIBUTION | STOCK_LOT
- `formatType` (requerido) — Descripción del empaque (ej. "Caja Máster Sellada")
- `unitsPerPackage` (requerido) — Unidades contenidas en el formato
- `moq` (requerido) — Cantidad mínima de pedido
- `referencePriceUnit` (requerido, number) — Precio por unidad en USD
- `specs` (requerido) — Especificaciones técnicas (texto libre)
- `description` (requerido) — Descripción para compradores
- `commercialTerms` (requerido) — Términos de despacho y garantía

**Campos NO en formulario (calculados o asignados internamente):**
- `currency` — siempre "USD"
- `status` — siempre "ACTIVE" al crear
- `referencePriceTotal` — calculado (priceUnit × unitsPerPackage)
- `isPromoted` — false al crear
- `isOffer` — false al crear
- `createdAt` — servidor
- `providerId` — del token JWT

**Validaciones:**
- title: no vacío, máximo 200 chars
- referencePriceUnit: > 0
- category: debe existir en BD y estar activa
- commercialModel: uno de los 3 valores permitidos

**Endpoints necesarios:**
```
POST /api/providers/me/products
  Body: ProductCreateDTO (campos del formulario)
  → Response: ProductDTO
  → HTTP 201 | 400

PUT /api/providers/me/products/{productId}
  Body: ProductUpdateDTO
  → Response: ProductDTO
  → HTTP 200 | 400 | 403 | 404
```

---

### B-04. Gestión de Ofertas (OffersManagementView)

**Descripción:** Listado y gestión de las ofertas publicadas por el proveedor.

**Quién accede:** PROVEEDOR

**Funcionalidades:**
- Ver lista de ofertas con estado (ACTIVE | DISABLED)
- Para cada oferta: título, producto relacionado, descuento %, precio original, precio promocional, validez, estado
- Crear nueva oferta → modal con formulario:
  - Seleccionar producto de su catálogo (dropdown, solo productos ACTIVE)
  - Título de la oferta
  - % de descuento (0–99)
  - Precio original
  - Precio promocional
  - Fecha de validez (texto descriptivo en demo)
  - Términos y condiciones de la oferta
- Editar oferta → modal con mismos campos
- Deshabilitar/Activar oferta → ACTIVE ↔ DISABLED
- Eliminar oferta → confirmación modal → eliminación lógica (DELETED_LOGICAL)

**EstEstructura de oferta (encontrada en mockData):**
```
Offer: {
  id, productId, productTitle, offerTitle,
  discountPercent, originalPrice, promotionalPrice,
  currency, validUntil, terms, status
}
```

**Endpoints necesarios:**
```
GET /api/providers/me/offers
  Query params: status, page, size
  → Response: Page<OfferDTO>

POST /api/providers/me/offers
  Body: { productId, offerTitle, discountPercent, originalPrice, promotionalPrice, validUntil, terms }
  → Response: OfferDTO
  → HTTP 201 | 400 | 404 (si producto no existe o no pertenece al proveedor)

PUT /api/providers/me/offers/{offerId}
  Body: OfferUpdateDTO
  → HTTP 200 | 400 | 403 | 404

PATCH /api/providers/me/offers/{offerId}/status
  Body: { status: "ACTIVE" | "DISABLED" }
  → HTTP 200 | 403 | 404

DELETE /api/providers/me/offers/{offerId}
  → Eliminación lógica
  → HTTP 204 | 403 | 404
```

---

### B-05. Publicidad y Promociones (PromotionsManagementView)

**Descripción:** Gestión de campañas publicitarias contratadas por el proveedor.

**Quién accede:** PROVEEDOR

**Planes disponibles (encontrados en el componente):**
- Plan Impulso Básico: 7 días, $49 USD
- Plan Pro Mayorista: 15 días, $89 USD (recomendado)
- Plan Expansión Premium: 30 días, $149 USD

**Funcionalidades:**
- Ver campañas activas con: nombre del producto, plan, fecha inicio/fin, días restantes, costo, espacios asignados
- Ver planes de visibilidad disponibles
- Contratar nueva campaña → modal:
  - Seleccionar producto (dropdown solo productos ACTIVE)
  - Seleccionar plan publicitario
  - Confirmar y activar
- Ver nota: "la publicidad otorga mayor prioridad de aparición, sin garantizar posición fija"

**EstEstructura de promoción (encontrada en mockData):**
```
Promotion: {
  id, productId, productTitle, planName,
  durationDays, daysRemaining, startDate, endDate,
  costUsd, status, spacesAssigned
}
```

**Endpoints necesarios:**
```
GET /api/providers/me/promotions
  → Response: PromotionDTO[]

GET /api/advertising/plans
  → Response: AdvertisingPlanDTO[] (planes disponibles)
  → Público o solo autenticados

POST /api/providers/me/promotions
  Body: { productId, planId }
  → Response: PromotionDTO
  → HTTP 201 | 400 | 404 | 409 (si producto ya tiene campaña activa)
```

---

### B-06. Chat del Proveedor (ProviderChatView)

**Descripción:** Sistema de mensajería desde la perspectiva del Proveedor.

**Quién accede:** PROVEEDOR

**Funcionalidades:**
- Panel izquierdo con lista de conversaciones de compradores
- Cada conversación: avatar comprador, nombre, tienda, último mensaje, hora, no leídos
- Información del comprador: nombre, nombre de tienda, ciudad
- Producto referenciado en la conversación (chip)
- Historial de mensajes con identificación: mensajes del cliente (izquierda) vs propios (derecha)
- Campo de texto + botón Enviar
- Indicador "Cliente está escribiendo..."
- Respuesta simulada automática del cliente (en demo)

**Misma estEstructura de Conversation y Message que el cliente** (ver A-05)

**Diferencia de perspectiva:** El proveedor recibe las conversaciones iniciadas por clientes

**Endpoints necesarios:**
```
GET /api/chat/conversations
  → Mismos endpoints que para el Cliente (compartidos, filtrados por rol)
  → El backend determina qué conversaciones pertenecen al usuario autenticado

POST /api/chat/conversations/{conversationId}/messages
  Body: { text }
  → HTTP 201

PUT /api/chat/conversations/{conversationId}/read
  → HTTP 204
```

---

### B-07. Vista Previa del Catálogo Público (PublicCatalogPreviewView)

**Descripción:** Vista previa de cómo ve el cliente el perfil público del proveedor.

**Quién accede:** PROVEEDOR

**Funcionalidades:**
- Muestra la vista pública del propio perfil tal como la vería un cliente
- Solo lectura desde perspectiva del proveedor
- Botón "Editar Perfil" para ir a edición

**Endpoint:** Reutiliza `GET /api/providers/{providerId}/public-profile`

---

### B-08. Cuenta del Proveedor (ProviderAccountView)

**Descripción:** Panel de configuración de la cuenta del proveedor.

**Quién accede:** PROVEEDOR

**Funcionalidades:**
- Ver datos actuales de la cuenta (email, empresa)
- Cambiar contraseña (igual que el cliente)
- Ver información no editable (contactPhone, verified, registeredAt)

**Endpoints necesarios:**
```
PUT /api/providers/me/password
  Body: { currentPassword, newPassword }
  → HTTP 204 | 400
```

---

## ANÁLISIS DE ROL C: ADMINISTRADOR

### Apartados identificados en la demo `diseno_administrador`:

---

### C-01. Dashboard Principal (AdminDashboardView)

**Descripción:** Panel de métricas y resumen del sistema.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver KPIs: total usuarios, total clientes, total proveedores, total solicitudes pendientes, total productos, total ofertas activas, total campañas activas
- Ver distribución de productos por categoría
- Widget de acceso rápido a solicitudes de proveedor pendientes
- Acceso directo a cualquier sección desde el dashboard

**Endpoints necesarios:**
```
GET /api/admin/dashboard/stats
  → Response: { totalUsers, totalClients, totalProviders, pendingApplications, totalProducts, activeOffers, activePromotions, productsByCategory[] }
  → HTTP 200
```

---

### C-02. Gestión General de Usuarios (AdminUsersView)

**Descripción:** Tabla de todos los usuarios del sistema con acciones de gestión.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Tabla con TODOS los usuarios (clientes y proveedores)
- Columnas: nombre, email, rol (CLIENT/PROVIDER), entidad/empresa, ciudad, estado, fecha registro
- Búsqueda por nombre/email
- Filtro por rol (Todos | Clientes | Proveedores)
- Filtro por estado (Todos | Activos | Inactivos | Bloqueados)
- Ver detalles del usuario → modal con toda la info + notas
- Editar datos del usuario
- Acción Bloquear/Desbloquear → requiere motivo (campo de texto)
- Acción Activar/Desactivar (ACTIVE ↔ INACTIVE)
- Estado visual diferenciado: ACTIVE (verde), INACTIVE (gris), BLOCKED (rojo)

**Endpoints necesarios:**
```
GET /api/admin/users
  Query params: role, status, search, page, size
  → Response: Page<UserAdminDTO>

GET /api/admin/users/{userId}
  → Response: UserDetailDTO
  → HTTP 200 | 404

PUT /api/admin/users/{userId}
  Body: UserUpdateDTO
  → HTTP 200 | 400 | 404

PATCH /api/admin/users/{userId}/status
  Body: { status: "ACTIVE" | "INACTIVE" | "BLOCKED", reason? }
  → HTTP 200 | 400 | 404
```

---

### C-03. Gestión de Clientes (AdminClientsView)

**Descripción:** Tabla específica de clientes compradores con sus datos comerciales.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Tabla de solo clientes
- Ver: nombre, email, nombre de tienda, ciudad, estado, fecha registro
- Búsqueda, filtro por estado
- Ver perfil completo del cliente → modal
- Editar datos comerciales del cliente
- Bloquear/Desbloquear con motivo

**Endpoints necesarios:**
```
GET /api/admin/clients
  Query params: status, search, page, size
  → Response: Page<ClientAdminDTO>

PUT /api/admin/clients/{clientId}
  Body: ClientUpdateDTO
  → HTTP 200 | 404

PATCH /api/admin/clients/{clientId}/block
  Body: { blocked: boolean, reason }
  → HTTP 200 | 404
```

---

### C-04. Gestión de Proveedores Aprobados (AdminProvidersView)

**Descripción:** Tabla de proveedores activos en el sistema.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Tabla de proveedores aprobados
- Ver: nombre empresa, Identificaci�n comercial, email, ciudad, estado, fecha registro, verificado
- Ver catálogo completo de cualquier proveedor
- Editar información del proveedor
- Suspender/Reactivar proveedor (ACTIVE ↔ INACTIVE)

**Endpoints necesarios:**
```
GET /api/admin/providers
  Query params: status, search, page, size
  → Response: Page<ProviderAdminDTO>

PUT /api/admin/providers/{providerId}
  Body: ProviderUpdateAdminDTO
  → HTTP 200 | 404

PATCH /api/admin/providers/{providerId}/status
  Body: { status: "ACTIVE" | "INACTIVE" }
  → HTTP 200 | 404
```

---

### C-05. Solicitudes de Proveedores (AdminApplicationsView)

**Descripción:** Bandeja de solicitudes de empresas que quieren ser proveedores.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Lista de solicitudes filtradas por estado: Pendientes | Aprobadas | Rechazadas
- Para cada solicitud: empresa, contacto, email, teléfono, ciudad, marcas, mensaje, fecha de aplicación
- Ver detalles completos de la solicitud → modal
- Acción "Aprobar" → crea cuenta de proveedor automáticamente en el sistema de usuarios
- Acción "Rechazar" → requiere motivo de rechazo (texto)
- Estado de rechazadas muestra: motivo de rechazo y fecha de revisión

**Al Aprobar (según AdminContext):**
- La aplicación cambia a status = APPROVED
- Se crea un nuevo usuario con role = PROVIDER en la tabla de usuarios
- Se le asignan credenciales iniciales (email de la solicitud)

**Endpoints necesarios:**
```
GET /api/admin/applications
  Query params: status (PENDING | APPROVED | REJECTED), page, size
  → Response: Page<ApplicationAdminDTO>

PATCH /api/admin/applications/{appId}/approve
  → Crea cuenta proveedor automáticamente
  → Response: { applicationDTO, newProviderDTO }
  → HTTP 200 | 400 | 404 | 409 (ya aprobada)

PATCH /api/admin/applications/{appId}/reject
  Body: { reason }
  → HTTP 200 | 400 | 404
```

---

### C-06. Moderación de Productos (AdminProductsModerationView)

**Descripción:** Catálogo global de TODOS los productos para moderación administrativa.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Tabla global con todos los productos de todos los proveedores
- Filtros: categoría, proveedor, estado (ACTIVE | DISABLED_BY_PROVIDER | HIDDEN_BY_ADMIN | DELETED_LOGICAL)
- Búsqueda por título
- Ver detalles completos del producto → modal con specs, descripción, términos comerciales
- Editar datos del producto (moderación) → mismo formulario que proveedor
- Ocultar/Restaurar → toggle ACTIVE ↔ HIDDEN_BY_ADMIN (requiere motivo)
- Eliminar lógicamente → DELETED_LOGICAL → genera entrada en audit_log
- Toggle "Recomendado" → marca/desmarca isRecommended

**Estados de producto (todos gestionados por Admin):**
- `ACTIVE` — visible públicamente
- `DISABLED_BY_PROVIDER` — deshabilitado por el proveedor
- `HIDDEN_BY_ADMIN` — ocultado por moderación
- `DELETED_LOGICAL` — eliminado lógicamente (va a audit log)

**Endpoints necesarios:**
```
GET /api/admin/products
  Query params: category, providerId, status, search, page, size
  → Response: Page<ProductAdminDTO>

PUT /api/admin/products/{productId}
  Body: ProductModerateDTO
  → HTTP 200 | 404

PATCH /api/admin/products/{productId}/visibility
  Body: { status: "ACTIVE" | "HIDDEN_BY_ADMIN", reason? }
  → HTTP 200 | 404

DELETE /api/admin/products/{productId}
  Body: { reason }
  → Eliminación lógica + audit log entry
  → HTTP 204 | 404

PATCH /api/admin/products/{productId}/recommendation
  Body: { isRecommended: boolean }
  → HTTP 200 | 404
```

---

### C-07. Moderación de Ofertas (AdminOffersModerationView)

**Descripción:** Lista global de ofertas de todos los proveedores para moderación.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Tabla de ofertas con: título, producto, proveedor, descuento %, precios, validez, estado
- Filtros por estado
- Editar oferta (moderación)
- Ocultar/Restaurar → ACTIVE ↔ HIDDEN_BY_ADMIN
- Eliminar lógicamente → DELETED_LOGICAL → audit log

**Endpoints necesarios:**
```
GET /api/admin/offers
  Query params: providerId, status, page, size
  → Response: Page<OfferAdminDTO>

PUT /api/admin/offers/{offerId}
  Body: OfferModerateDTO
  → HTTP 200 | 404

PATCH /api/admin/offers/{offerId}/visibility
  Body: { status: "ACTIVE" | "HIDDEN_BY_ADMIN", reason? }
  → HTTP 200 | 404

DELETE /api/admin/offers/{offerId}
  Body: { reason }
  → HTTP 204 | 404
```

---

### C-08. Categorías y Tipos (AdminCategoriesView)

**Descripción:** Gestión del árbol de categorías y subcategorías de productos.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver árbol de categorías con subcategorías y cantidad de productos
- Crear nueva categoría → modal con: nombre, subcategorías (lista)
- Editar categoría existente → modal
- Activar/Desactivar categoría
- Ver contador de productos en cada categoría

**EstEstructura (encontrada en adminMockData):**
```
Category: { id, name, subcategories[], status, productsCount }
```

**Endpoints necesarios:**
```
GET /api/categories
  → Response: CategoryDTO[] (público)
  → HTTP 200

POST /api/admin/categories
  Body: { name, subcategories[] }
  → Response: CategoryDTO
  → HTTP 201 | 400 | 409

PUT /api/admin/categories/{categoryId}
  Body: { name, subcategories[] }
  → HTTP 200 | 404

PATCH /api/admin/categories/{categoryId}/status
  Body: { status: "ACTIVE" | "INACTIVE" }
  → HTTP 200 | 404
```

---

### C-09. Productos Recomendados (AdminRecommendationsView)

**Descripción:** Gestión de la selección curada de productos recomendados por la plataforma.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver todos los productos activos con indicador de si están recomendados
- Toggle de recomendación por producto (sin modal adicional)
- Feedback inmediato de activación/desactivación

**Endpoint:**
```
PATCH /api/admin/products/{productId}/recommendation
  Body: { isRecommended: boolean }
  → HTTP 200 | 404
  (Reutiliza endpoint de moderación de productos)
```

---

### C-10. Proveedores Destacados (AdminFeaturedProvidersView)

**Descripción:** Gestión de qué proveedores aparecen como Destacados en el Home del cliente.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver lista de proveedores aprobados con indicador de si están destacados
- Toggle "Destacar" / "Retirar de Destacados"
- Feedback visual inmediato

**Endpoints necesarios:**
```
PATCH /api/admin/providers/{providerId}/featured
  Body: { isFeatured: boolean }
  → HTTP 200 | 404
```

---

### C-11. Publicidad y Promociones (AdminPromotionsView)

**Descripción:** Supervisión y moderación de campañas publicitarias activas.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver lista de campañas: producto, proveedor, plan, días restantes, costo, estado
- Cancelar/Pausar campaña → requiere motivo
- Reactivar campaña cancelada
- Ver nota explicativa de la política de visibilidad dinámica

**Endpoints necesarios:**
```
GET /api/admin/promotions
  Query params: status, providerId, page, size
  → Response: Page<PromotionAdminDTO>

PATCH /api/admin/promotions/{promotionId}/status
  Body: { status: "ACTIVE" | "CANCELLED", reason? }
  → HTTP 200 | 404
```

---

### C-12. Contenido "Nosotros" (AdminContentAboutView)

**Descripción:** Editor del contenido institucional de la sección Nosotros.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Editar título institucional
- Editar subtítulo
- Editar misión
- Editar pilares (lista de {título, descripción})
- Botón "Guardar Cambios"
- Feedback visual de guardado

**Endpoints necesarios:**
```
GET /api/content/about
  → Response: AboutContentDTO
  → HTTP 200

PUT /api/admin/content/about
  Body: { title, subtitle, mission, pillars[{title, description}] }
  → HTTP 200 | 400
```

---

### C-13. Contenido "Contacto" (AdminContentContactView)

**Descripción:** Editor de los datos de contacto mostrados en la sección pública Contacto.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Editar email de soporte
- Editar email para consultas de proveedores
- Editar teléfono central
- Editar WhatsApp de soporte
- Editar dirección de oficina
- Editar horario de atención

**Endpoints necesarios:**
```
GET /api/content/contact
  → Response: ContactInfoDTO
  → HTTP 200

PUT /api/admin/content/contact
  Body: { supportEmail, providerInquiriesEmail, centralPhone, whatsappSupport, officeAddress, operatingHours }
  → HTTP 200 | 400
```

---

### C-14. Historial de Auditoría (AdminAuditHistoryView)

**Descripción:** Centro de inspección de todos los registros con eliminación lógica.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver lista de entradas de auditoría (productos y ofertas eliminados lógicamente)
- Para cada entrada: tipo de entidad, ID original, título, quién eliminó, cuándo, motivo
- Ver snapshot JSON de datos originales antes de eliminación
- Filtro por tipo de entidad (PRODUCTO | OFERTA)
- Filtro por quién eliminó (Proveedor | Admin)
- Datos de solo lectura (inmutable)

**Endpoints necesarios:**
```
GET /api/admin/audit-logs
  Query params: entityType, deletedBy (role), page, size
  → Response: Page<AuditLogDTO>
  → HTTP 200

GET /api/admin/audit-logs/{logId}
  → Response: AuditLogDetailDTO (con snapshotData completo)
  → HTTP 200 | 404
```

---

### C-15. Configuración del Administrador (AdminSettingsView)

**Descripción:** Perfil y seguridad del administrador.

**Quién accede:** ADMINISTRADOR

**Funcionalidades:**
- Ver y editar datos del perfil: nombre, email, departamento
- Campo de nivel de acceso (no editable)
- Cambiar contraseña (actual, nueva, confirmar)

**Endpoints necesarios:**
```
GET /api/admin/me/profile
  → Response: AdminProfileDTO

PUT /api/admin/me/profile
  Body: { name, email, department }
  → HTTP 200 | 400

PUT /api/admin/me/password
  Body: { currentPassword, newPassword }
  → HTTP 204 | 400
```

---

## FUNCIONALIDADES ENCONTRADAS EN IMPLEMENTACIÓN (no explícitamente en documentación)

1. **Campo `currency` en productos:** La demo siempre usa "USD". La documentación no especificaba moneda explícitamente. Se asume USD como moneda única de la plataforma.

2. **Campo `verified` en proveedores:** La demo muestra un badge de "Verificado". Implica que el Admin puede marcar o desmarcar esta condición al crear/aprobar una cuenta de proveedor.

3. **Campo `rating` en proveedores:** Se muestra en la demo pero no hay funcionalidad de calificación implementada. Se documenta como dato informativo del proveedor aprobado (asignado al crear, sin módulo de reseñas).

4. **Campo `yearsInMarket` en proveedores:** Dato informativo que el proveedor puede editar en su perfil.

5. **Snapshot en Audit Log:** La demo muestra un objeto JSON de datos originales del registro eliminado. Es una fotografía (snapshot) del estado del registro en el momento de la eliminación.

6. **Estado `HIDDEN_BY_ADMIN` en productos:** Status diferenciado del `DISABLED_BY_PROVIDER`. Permite al Admin restaurar independientemente de la acción del proveedor.

7. **Planes de publicidad predefinidos:** La demo tiene 3 planes fijos ($49/7d, $89/15d, $149/30d). Estos deben guardarse en BD como AdvertisingPlan para que el Admin los pueda gestionar en el futuro.
