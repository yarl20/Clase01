# 18. Reglas de Negocio

> Plataforma COMPUNEX B2B — Reglas teóricas — PostgreSQL
> Fuente única: `01_idea_principal.md`, `02_roles_y_permisos.md`, `03_funcionalidades.md`, `04_requisitos_funcionales_y_no_funcionales.md`, `PLANWEB/*`, y los 3 diseños `git/diseno_cliente`, `git/diseno_proveedor`, `git/diseno_administrador` (contexts + mockData + vistas)
> No se inventan reglas fuera de esas fuentes. Todo RN existe en algún archivo de idea o es comportamiento observable en la demo.

---

## RN-01. Rubro Exclusivo de Tecnología

**Fuente:** `01_idea_principal.md:18` + `03_funcionalidades.md:55` + `adminMockData.js:152` `app_03 ElectroHogar` rechazada + `INITIAL_ADMIN_PRODUCTS:322` `prod_99_violating` refrigeradora `OCULTO_POR_ADMIN`.

El sistema solo admite componentes y accesorios de computación: RAM, SSD, HDD, USB, GPU, CPU, motherboard, PSU. Electrodomésticos, telefonía y cualquier producto ajeno están fuera de alcance. Publicación fuera de rubro → `productos.motivo_moderacion = "Publicación fuera de rubro"` → `estado='OCULTO_POR_ADMIN'` y luego `ELIMINADO_LOGICO`.

**Demo:** `AdminProductsModerationView` filtra por categoría y marca `OCULTO_POR_ADMIN`.

---

## RN-02. Ausencia Total de Gestión de Stock

**Fuente:** `01_idea_principal.md:37` + `04_requisitos:12` `RF-12` + `03_funcionalidades.md:69` `RNF-05`.

No existe campo `stock`, `cantidad`, `inventario` en ninguna tabla (`productos` no tiene stock). No se descuenta inventario, no se deshabilita por `cantidad 0`. La cantidad en la ficha (ej: `20 unidades por caja`, `1 Palé 24 unidades`) es **forma de comercialización**, no disponibilidad de almacén. Disponibilidad real se confirma en `conversaciones`/`mensajes`.

**Demo:** `CreateEditProductView.jsx:458` formulario no tiene campo stock; `ProviderContext.jsx:62` `addProduct` crea `status=ACTIVE` sin stock.

---

## RN-03. Modelos de Comercialización por Volumen

**Fuente:** `01_idea_principal.md:45` + `03_funcionalidades.md:58` + `02_BASE_DATOS.md:293` + `providerMockData.js:43` `commercialModel`.

Cada `productos.modelo_comercial` es obligatorio y es uno de:
1. `VENTA_MAYORISTA_TRADICIONAL` — cajas máster, palés, paquetes + `pedido_minimo` (MOQ) en unidades o monto. Ej: `MOQ: 2 Cajas Máster (40 unidades)` (`prod_1`).
2. `DISTRIBUCION_EXCLUSIVA` — acuerdo para ser punto autorizado por zona. Ej: `Palé de Distribución Regional 24 unidades` (`prod_3`).
3. `LOTE_STOCK` — paquete cerrado indivisible de liquidación. Ej: `Lote cerrado 50x SSD` (`prod_4`) o `100x USB IronKey` (`prod_13`).

**Demo:** `ExploreProductsView.jsx:38` filtro `commercialModel`, `ProductDetailView` muestra `formatType`, `unitsPerPackage`, `moq`, `referencePriceUnit/Total`.

---

## RN-04. Moneda Única USD

**Fuente:** `01_ANALISIS_GENERAL:1136` `currency USD`, `04_SQL_DATABASE.sql:259` `moneda VARCHAR(3) DEFAULT 'USD'`.

Todo `productos.precio_unitario_ref` y `ofertas.precio_*` es `USD`. No hay conversión. `currency` siempre `USD`.

---

## RN-05. Sin Checkout Transaccional

**Fuente:** `01_idea_principal.md:13` + `RNF-05`.

La plataforma **no procesa pagos, no cobra, no crea pedidos**. Es exhibición + publicidad + contacto. Todo cierre es directo en `mensajes` (transferencia, guía de remisión, factura). `promociones` es el único cobro (publicidad), no venta de productos.

**Demo:** `ProviderChatView` y `ChatView` contienen plantillas `Confirmar Stock`, `Descuento 3% Contado`, `Solicitar Datos de Facturación` — negociación manual.

---

## RN-06. Control de Acceso RBAC Estricto (3 Roles)

**Fuente:** `02_roles_y_permisos.md:9` + `05_API_AUTENTICACION.md:184` + `04_SQL_DATABASE.sql:usuarios.rol`.

Roles: `ADMIN` (interno), `PROVEEDOR` (empresa aprobada), `CLIENTE` (tienda). Matriz `02_roles_y_permisos.md:179` — cada acción está permitida/denegada por rol. `usuarios.rol` es `ENUM rol_usuario`.

**Demo:** `ClientContext`, `ProviderContext`, `AdminContext` separados; `ProviderNavbar` y `AdminSidebar` muestran solo sus vistas.

---

## RN-07. Alta de Proveedores Solo por Solicitud + Aprobación Manual

**Fuente:** `01_idea_principal.md:71` + `02_roles_y_permisos.md:70` + `03_funcionalidades.md:12` + `AdminApplicationsView.jsx:68` `Filtro Exclusivo`.

Cliente se registra libre (`POST /auth/register` rol CLIENTE). Empresa **no** se autorregistra como PROVEEDOR: envía `POST /applications/provider` (`solicitudes_proveedor` `estado='PENDIENTE'`) vía `ContactView.jsx` (form: `razon_social`, `nombre_contacto`, `correo`, `telefono`, `ciudad`, `direccion`, `marcas_texto`, `mensaje`). `ADMIN` revisa en `AdminApplicationsView` y ejecuta `PATCH /approve` → crea `usuarios` PROVEEDOR + `perfiles_proveedor` + `proveedor_marcas`. Sin `taxId`.

**Demo:** `ContactView.jsx` tab `Postulación para Proveedores` con nota `Las empresas no pueden autorregistrarse` + `AdminApplicationsView` con 3 tabs `Todas/Pendientes/Aprobadas/Rechazadas` (ej: `PENDIENTE ByteMax`, `RECHAZADA ElectroHogar`).

---

## RN-08. Perfil del Proveedor como Home

**Fuente:** `01_idea_principal.md:73` + `PLANWEB/03_plan_rol_proveedor.md:58` + `ProviderProfileHomeView.jsx:53`.

El proveedor no tiene dashboard genérico; su página principal es su **perfil público + catálogo**. Desde allí edita `razon_social`, `nombre_comercial`, `telefono_contacto`, `url_logo`, `descripcion`, `politica_comercial`, `direccion`, `ciudad` vía modal. Ve `productos` debajo. Botón `Agregar nuevo producto` si está vacío + acceso permanente en `ProviderNavbar`.

---

## RN-09. Estados de Publicación y Ciclo de Vida

**Fuente:** `03_funcionalidades.md:64` + `03_MODELO_RELACIONAL.md:334` + `productos.estado` ENUM + `AdminProductsModerationView`.

- `ACTIVO` — visible en búsquedas, inicio y catálogo del proveedor.
- `DESHABILITADO_POR_PROVEEDOR` — oculta al público, visible en cuenta del proveedor para reactivar (`toggleProductStatus` → `ACTIVE`).
- `OCULTO_POR_ADMIN` — oculta por moderación (`AdminProductsModerationView` → `motivo_moderacion`), solo ADMIN restaura.
- `ELIMINADO_LOGICO` — terminal, invisible para proveedor y público, archivado en `registros_auditoria` con `datos_snapshot` JSONB.

Transiciones: `ACTIVO ↔ DESHABILITADO_POR_PROVEEDOR` (proveedor), `ACTIVO ↔ OCULTO_POR_ADMIN` (admin), cualquier → `ELIMINADO_LOGICO` (ambos, terminal).

**Demo:** `ProductManagementView.jsx` tabs `Todos | Activos | Deshabilitados`, botones `Desactivar/Activar`, `Eliminar` con confirmación → `DELETED_LOGICAL`.

---

## RN-10. Estados de Ofertas

**Fuente:** `OffersManagementView.jsx` + `ofertas.estado` + `03_MODELO_RELACIONAL.md:349`.

`ACTIVA` ↔ `DESHABILITADA` (proveedor `toggleOfferStatus`), `ACTIVA` ↔ `OCULTA_POR_ADMIN` (admin), → `ELIMINADA_LOGICA` (terminal, ambos). Al crear `ofertas` → `productos.tiene_oferta=true`; al eliminar última activa → `tiene_oferta=false`.

**Demo:** `INITIAL_OFFERS:213` (`offer_1` 12% OFF, `offer_2` 15% OFF) con `validUntil` textual, no `TIMESTAMPTZ`.

---

## RN-11. Estados de Solicitudes de Proveedor

**Fuente:** `solicitudes_proveedor.estado` + `03_MODELO_RELACIONAL.md:360`.

`PENDIENTE` → `APROBADA` (crea cuenta) o `RECHAZADA` (con `motivo_rechazo`). Terminal. No se puede revertir.

**Demo:** `AdminApplicationsView` con `PENDIENTE`, `APROBADA • Proveedor Creado`, `RECHAZADA` + motivo.

---

## RN-12. Estados de Promociones y Campañas de Publicidad

**Fuente:** `promociones.estado` + `PromotionsManagementView.jsx` + `planes_publicidad` + `03_MODELO_RELACIONAL.md:366`.

`ACTIVA` → `EXPIRADA` (automático cuando `NOW() > fecha_fin`) o `CANCELADA` (admin con `nota_moderacion`). `promociones` tiene `fecha_inicio = NOW()`, `fecha_fin = NOW + dias_duracion`. Solo 1 `ACTIVA` por `producto_id` (`POST /providers/me/promotions` valida `409 si ya existe activa`).

Planes en `planes_publicidad` (`07_API_PROVEEDOR.md:330`): `Plan Impulso Básico 7 días $49`, `Plan Pro Mayorista 15 días $89 (popular)`, `Plan Expansión Premium 30 días $149`.

**Demo:** `INITIAL_PROMOTIONS:242` (`prom_1` 15 días, `prom_2` 30 días) con `daysRemaining` calculado.

---

## RN-13. Estados de Usuarios

**Fuente:** `usuarios.estado` + `AdminUsersView` + `03_MODELO_RELACIONAL.md:372`.

`ACTIVO` ↔ `INACTIVO` (admin desactiva), `ACTIVO|INACTIVO` → `BLOQUEADO` (con `motivo_estado`), `BLOQUEADO` → `ACTIVO` (desbloquea). `BLOCKED` impide `POST /auth/login` (`403`).

---

## RN-14. Eliminación Lógica y Conservación Histórica

**Fuente:** `01_idea_principal.md:58` + `03_funcionalidades.md:152` + `04_SQL_DATABASE.sql:registros_auditoria`.

Ninguna entidad relevante se borra físicamente. `DELETE` lógico → `estado='ELIMINADO_LOGICO'` + `fecha_eliminacion=NOW()` + `eliminado_por=auth.id` + `INSERT INTO registros_auditoria (tipo_entidad, id_entidad_original, titulo_entidad, rol_eliminador, datos_snapshot)`. El proveedor no ve ni recupera lo eliminado; solo `ADMIN` ve `AdminAuditHistoryView` con `datos_snapshot` inmutable.

**Tablas con eliminación lógica:** `productos`, `ofertas` (y vía `registros_auditoria` para `proveedor`/`usuario`).

**Demo:** `prod_13 Lote 100x USB` con `status=DELETED_LOGICAL` + `deletedAt` en `providerMockData.js:205`.

---

## RN-15. Publicidad y Promoción Pagada con Prioridad No Garantizada

**Fuente:** `01_idea_principal.md:65` + `03_funcionalidades.md:89` + `AdminPromotionsView` + `HomeView`.

Proveedor selecciona producto propio y contrata plan (`POST /providers/me/promotions {productId, planId}`). `productos.es_promocionado=true` (prioridad ALTA en `Home` bloque `Promocionados`). **No garantiza posición fija** — `HomeView` bloque `Promocionados` hace `ORDER BY RANDOM()` + `is_promoted=true`.

Admin supervisa en `AdminPromotionsView` y puede `CANCELADA` con motivo.

**Demo:** `PromotionsManagementView.jsx` con 3 planes y campañas activas `prom_1`, `prom_2`.

---

## RN-16. Productos Recomendados y Proveedores Destacados (Curaduría Admin)

**Fuente:** `03_funcionalidades.md:109` + `AdminRecommendationsView` + `AdminFeaturedProvidersView` + `HomeView`.

- `productos.es_recomendado` (boolean) — ADMIN marca con `PATCH /admin/productos/{id}/recomendacion` → aparece en `Home` bloque `Recomendados`.
- `perfiles_proveedor.destacado` (boolean) — ADMIN marca con `PATCH /admin/proveedores/{id}/destacado` → aparece en `Home` bloque `Proveedores Destacados`.

**Demo:** `INITIAL_PRODUCTS:127` `isRecommended=true` (4 productos) y `INITIAL_FEATURED_PROVIDERS:413` `["prov_1","prov_2","prov_3","prov_4"]`.

---

## RN-17. Estructura de Descubrimiento en Inicio (Home Cliente)

**Fuente:** `03_funcionalidades.md:106` + `HomeView.jsx` + `06_API_CLIENTE.md:7` `GET /home/featured-content`.

El `Home` organiza 5 bloques diferenciados:
1. `Productos Recomendados` (`es_recomendado=true`)
2. `Productos Promocionados` (`es_promocionado=true`, paga)
3. `Productos Generales` (orgánico, `estado='ACTIVO'` y no es recomendado)
4. `Ofertas` (`ofertas.estado='ACTIVA'` join productos)
5. `Proveedores Destacados` (`destacado=true`)

Diferenciación visual clara (badge `Recomendado`/`Patrocinado`/`Oferta`).

---

## RN-18. Búsqueda, Filtros y Preferencias (Sin Anular Publicidad)

**Fuente:** `03_funcionalidades.md:115` + `ExploreProductsView.jsx:38` + `PreferencesView.jsx` + `ClientContext.jsx:224` `getRecommendedProducts`.

- **Filtros:** `search` (título/descripción, PostgreSQL `ILIKE` + `GIN to_tsvector spanish`), `categoria` (ram/ssd...), `modelo_comercial`, `precio_rango` (`precio_unitario_ref BETWEEN`), `orden` (relevancia, precio asc/desc, novedad).
- **Preferencias:** `preferencias_cliente` con 3 tablas hijas normalizadas (`preferencias_cliente_categorias`, `modelos`, `palabras`). Cliente elige `selectedCategories`, `selectedCommercialModels`, `keywords` (`PreferencesView`). `Home` personaliza con `getRecommendedProducts` pero **no anula publicidad** (`HomeView` respeta bloques pagados).

**Demo:** `INITIAL_CLIENT_PREFERENCES:425` (`selectedCategories: ["ram","ssd","gpu"]`, `keywords: ["Kingston","NVMe"]`).

---

## RN-19. Chat Privado 1-1 y Negociación Directa

**Fuente:** `03_funcionalidades.md:126` + `ChatView.jsx:16` + `ProviderChatView.jsx` + `conversaciones` + `mensajes`.

- Apertura desde `ProductDetailView` (`Contactar al Proveedor`) o `ProviderProfileView` (`Iniciar Chat`) con `providerId` y opcional `referencedProductId`.
- Canal 1-1 estricto `UNIQUE(cliente_id, proveedor_id)` — una sola conversación por par (referencia de producto via `producto_referenciado_id` y chip en UI).
- `mensajes` con `rol_remitente`, `texto`, `leido`, `fecha_envio`. Contadores `no_leidos_cliente`/`proveedor` + `vista_previa_ultimo` + `fecha_ultimo_mensaje`.
- Cierre de acuerdos (precios finales, despacho, pago) es directo en chat, fuera de plataforma. No hay checkout.
- Simulación en demo: `isProviderTyping`/`isClientTyping` con `setTimeout 1800ms` y plantillas rápidas (`Plazo de despacho`, `Descuento 5 cajas`, `Proforma` / `Confirmar Stock`).

---

## RN-20. Privacidad de Comunicaciones

**Fuente:** `02_roles_y_permisos.md:69` + `04_requisitos:RNF-02` + `03_funcionalidades.md:132`.

Chats son privados entre participante `CLIENTE` y `PROVEEDOR`. `ADMIN` **no** tiene acceso ordinario a `mensajes`. Solo mecanismos especiales por soporte/denuncia formal (no implementados en demo, solo `AdminContext` no lee chats).

---

## RN-21. Contenido Institucional y Contacto (Solo ADMIN edita)

**Fuente:** `03_funcionalidades.md:138` + `AdminContentAboutView` + `AdminContentContactView` + `contenido_plataforma`.

- `Nosotros` (`clave='nosotros'`): `titulo`, `subtitulo`, `mision`, `pilares[]` — editable solo por ADMIN (`PUT /admin/contenido/nosotros`).
- `Contacto` (`clave='contacto'`): `correo_soporte`, `correo_proveedores`, `telefono_central`, `whatsapp_soporte`, `direccion_oficina`, `horario_atencion` — editable solo por ADMIN.

**Demo:** `INITIAL_ABOUT_CONTENT:415` + `INITIAL_CONTACT_INFO:426`.

---

## RN-22. Categorías y Tipos Gestionados Solo por ADMIN

**Fuente:** `AdminCategoriesView.jsx` + `categorias` + `subcategorias` + `06_API_CLIENTE.md:367` `GET /categories`.

Cliente y proveedor solo leen `categorias` (dropdown en `CreateEditProductView`). ADMIN crea/edita/activa/desactiva `categorias` y `subcategorias`. `categorias.total_productos` es desnormalizado para dashboard.

**Demo:** `INITIAL_ADMIN_CATEGORIES:183` 8 categorías (`ram`, `ssd`, `gpu`, `cpu`, `motherboard`, `hdd`, `psu`, `usb`) con subcategorías (`DDR4`, `M.2 PCIe 4.0`, etc.).

---

## RN-23. Validación de Formularios

**Fuente:** Observado en `CreateEditProductView.jsx:457` + `ContactView.jsx` + `AccountView.jsx` + `ProviderContext.jsx`.

- Producto: `titulo` requerido max 500, `categoria_id` debe existir y `estado='ACTIVA'`, `modelo_comercial` en ENUM, `precio_unitario_ref >0`, `specs` requerido, `imageUrls` al menos 1.
- Solicitud proveedor: `razon_social`, `nombre_contacto`, `correo` válido, `telefono` requerido, `marcas_texto`, `mensaje` requeridos.
- Preferencias: `selectedCategories` cada `categoria_id` debe existir, `selectedCommercialModels` en ENUM, `keywords` max 20 x 100 chars.
- Oferta: `porcentaje_descuento` 1-99, `precio_promocional < precio_original` y `>0` (`ofertas` CHECK).

---

## RN-24. Publicación del Proveedor desde Perfil y Header

**Fuente:** `ProviderProfileHomeView.jsx:199` + `ProviderNavbar.jsx` + `PLANWEB/03_plan_rol_proveedor.md:58`.

Proveedor publica desde su `Perfil Principal` (botón `Agregar nuevo producto` si vacío) o `Header` permanente `Publicar Producto`. `CreateEditProductView` maneja `addProduct` vs `updateProduct` según `editingProductId`.

---

## RN-25. Historial y Trazabilidad de Eliminaciones

**Fuente:** `AdminAuditHistoryView.jsx` + `registros_auditoria` + `INITIAL_AUDIT_LOGS:435`.

Todo `ELIMINADO_LOGICO` genera `registros_auditoria` con `tipo_entidad` (PRODUCTO/OFERTA), `rol_eliminador` (PROVEEDOR/ADMIN), `motivo`, `datos_snapshot` JSONB. Vista `AdminAuditHistoryView` filtra por `tipo_entidad` y `rol_eliminador`, muestra `datos_snapshot` completo, solo lectura.

**Demo:** `hist_01` producto USB por proveedor, `hist_02` smartphone por admin (fuera de rubro), `hist_03` oferta expirada.

---

*Fin de reglas teóricas — todas trazables a archivos de idea o a una vista/context/mock de `git/diseno_*` sin invención.*
