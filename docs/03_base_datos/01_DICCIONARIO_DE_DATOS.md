# 01. Diccionario de Datos Oficial — COMPUNEX B2B

> **Plataforma:** COMPUNEX B2B — Ecosistema Mayorista de Componentes de Computación  
> **Motor de Persistencia:** PostgreSQL 15+  
> **Normalización:** Tercera Forma Normal (3FN)  
> **Total de Tablas:** 22 tablas organizadas en 7 módulos funcionales  
> **Convenciones:** Nombres en español (`snake_case`), claves primarias sintéticas (`BIGSERIAL`), identificadores públicos opcionales (`UUID v4`), integridad referencial estricta y eliminación lógica auditada.

---

## 🏛️ Principios de Diseño de la Base de Datos

1. **Eliminación Lógica Obligatoria (*Soft Delete*):** Ninguna entidad de negocio (`productos`, `ofertas`, `usuarios`) sufre `DELETE` físico. Se utiliza un campo `estado` (`ELIMINADO_LOGICO`) y se genera un snapshot inmutable en `registros_auditoria`.
2. **Ausencia de Control de Stock:** El sistema opera bajo modelos de preventa y distribución mayorista (`cajas máster`, `palés`, `lotes`). La disponibilidad se confirma en el módulo de chat.
3. **Moneda Única (USD):** Todo precio referencial y promocional opera exclusivamente en Dólares Americanos (`USD`).
4. **Normalización 3FN:** Cero columnas JSON multivalor para atributos estructurados; se emplean tablas hijas normalizadas (`producto_especificaciones`, `imagenes_producto`, `proveedor_marcas`, `proveedor_especialidades`).

---

## 📑 ÍNDICE DE TABLAS POR MÓDULO

1. **Módulo 1: Autenticación y Cuentas de Usuario** (`usuarios`)
2. **Módulo 2: Perfiles y Preferencias de Clientes** (`perfiles_cliente`, `preferencias_cliente`, `preferencias_cliente_categorias`, `preferencias_cliente_modelos`, `preferencias_cliente_palabras`)
3. **Módulo 3: Perfiles y Postulaciones de Proveedores** (`perfiles_proveedor`, `proveedor_marcas`, `proveedor_especialidades`, `solicitudes_proveedor`)
4. **Módulo 4: Catálogo, Categorías y Productos** (`categorias`, `subcategorias`, `productos`, `producto_especificaciones`, `imagenes_producto`)
5. **Módulo 5: Ofertas y Campañas Publicitarias** (`ofertas`, `planes_publicidad`, `promociones`)
6. **Módulo 6: Mensajería B2B y Negociación** (`conversaciones`, `mensajes`)
7. **Módulo 7: Plataforma y Auditoría** (`contenido_plataforma`, `registros_auditoria`)

---

## 🗄️ ESPECIFICACIÓN DETALLADA DE LAS 22 TABLAS

---

### MÓDULO 1: AUTENTICACIÓN Y CUENTAS

#### 1. Tabla: `usuarios`
Entidad maestra de identidad y credenciales de acceso para los 3 roles del sistema (`ADMIN`, `PROVEEDOR`, `CLIENTE`).

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` (Long) | Identificador interno autonumérico |
| `uuid` | `UUID` | `NOT NULL, UNIQUE, DEFAULT gen_random_uuid()` | `uuid` (UUID) | Identificador público para URLs y tokens |
| `nombre` | `VARCHAR(200)` | `NOT NULL` | `nombre` / `fullName` | Nombre completo o nombre del representante |
| `correo` | `VARCHAR(255)` | `NOT NULL, UNIQUE` | `email` | Correo electrónico corporativo (login) |
| `hash_contrasena` | `VARCHAR(255)` | `NOT NULL` | *(no expuesto)* | Contraseña cifrada con algoritmo BCrypt (cost 12) |
| `rol` | `rol_usuario` (ENUM) | `NOT NULL` | `role` (`ADMIN`,`PROVEEDOR`,`CLIENTE`) | Rol de seguridad para control de acceso RBAC |
| `estado` | `estado_usuario` (ENUM) | `NOT NULL, DEFAULT 'ACTIVO'` | `status` (`ACTIVO`,`INACTIVO`,`BLOQUEADO`) | Estado operativo de la cuenta |
| `motivo_estado` | `TEXT` | `NULL` | `statusReason` | Justificación administrativa en caso de bloqueo |
| `fecha_registro` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha y hora de creación de la cuenta |
| `ultimo_acceso` | `TIMESTAMPTZ` | `NULL` | `lastLogin` | Registro del último inicio de sesión exitoso |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Actualizado automáticamente por trigger |

* **Índices:** `idx_usuarios_rol` (ALTA), `idx_usuarios_estado` (ALTA), `idx_usuarios_fecha_registro` (MEDIA).

---

### MÓDULO 2: CLIENTES Y PREFERENCIAS

#### 2. Tabla: `perfiles_cliente`
Perfil comercial del comprador mayorista o dueño de tienda minorista. Relación 1-a-1 con `usuarios`.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador interno del perfil |
| `usuario_id` | `BIGINT` | `NOT NULL, UNIQUE, FK → usuarios(id)` | `userId` | Usuario dueño de la cuenta (rol `CLIENTE`) |
| `nombre_tienda` | `VARCHAR(255)` | `NOT NULL` | `storeName` | Nombre comercial de la tienda o negocio del cliente |
| `telefono` | `VARCHAR(30)` | `NULL` | `phone` | Teléfono de contacto para coordinaciones |
| `ciudad` | `VARCHAR(100)` | `NULL` | `city` | Ciudad de operación comercial (ej. Lima, Arequipa) |
| `direccion` | `VARCHAR(300)` | `NULL` | `address` | Dirección fiscal o de despacho |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha de registro del perfil |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Actualizado por trigger |

* **Índice:** `idx_perfil_cliente_ciudad` (MEDIA).

#### 3. Tabla: `preferencias_cliente`
Contenedor de configuración de intereses de compra del cliente para personalizar el catálogo y la página de inicio.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de las preferencias |
| `perfil_cliente_id` | `BIGINT` | `NOT NULL, UNIQUE, FK → perfiles_cliente(id) ON DELETE CASCADE` | `clientId` | Perfil del cliente asociado |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Última actualización de intereses |

#### 4. Tabla: `preferencias_cliente_categorias`
Categorías de hardware suscritas por el cliente para recibir novedades y recomendaciones.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `preferencia_id` | `BIGINT` | `NOT NULL, FK → preferencias_cliente(id) ON DELETE CASCADE` | `preferenceId` | Contenedor de preferencias padre |
| `categoria_id` | `VARCHAR(50)` | `NOT NULL, FK → categorias(id) ON DELETE CASCADE` | `categoryId` | Código de categoría de interés (ej. `"ram"`, `"ssd"`) |

* **Restricción:** `UNIQUE (preferencia_id, categoria_id)`.
* **Índice:** `idx_pref_cat_categoria` (ALTA).

#### 5. Tabla: `preferencias_cliente_modelos`
Modelos de comercialización preferidos por el cliente (`MAYORISTA`, `DISTRIBUCION`, `LOTE`).

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `preferencia_id` | `BIGINT` | `NOT NULL, FK → preferencias_cliente(id) ON DELETE CASCADE` | `preferenceId` | Preferencia padre |
| `modelo_comercial` | `modelo_comercial` | `NOT NULL` | `commercialModel` | Enum del modelo comercial de interés |

* **Restricción:** `UNIQUE (preferencia_id, modelo_comercial)`.

#### 6. Tabla: `preferencias_cliente_palabras`
Términos de búsqueda frecuentes para sugerencias personalizadas de productos.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `preferencia_id` | `BIGINT` | `NOT NULL, FK → preferencias_cliente(id) ON DELETE CASCADE` | `preferenceId` | Preferencia padre |
| `palabra_clave` | `VARCHAR(100)` | `NOT NULL` | `keyword` | Término clave (ej. `"NVMe"`, `"DDR5"`, `"RTX"`) |

* **Índice:** `idx_pref_palabra` (ALTA).

---

### MÓDULO 3: PROVEEDORES Y POSTULACIONES

#### 7. Tabla: `perfiles_proveedor`
Perfil corporativo oficial de la empresa importadora o distribuidora mayorista. Relación 1-a-1 con `usuarios`.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador interno del proveedor |
| `usuario_id` | `BIGINT` | `NOT NULL, UNIQUE, FK → usuarios(id)` | `userId` | Cuenta de usuario dueña (rol `PROVEEDOR`) |
| `razon_social` | `VARCHAR(255)` | `NOT NULL` | `companyName` | Razón social legal registrada |
| `nombre_comercial` | `VARCHAR(255)` | `NULL` | `tradeName` | Nombre de marca comercial |
| `identificacion_fiscal`| `VARCHAR(50)` | `NULL` | `taxId` | Número de RUC o identificación tributaria |
| `telefono_contacto`| `VARCHAR(50)` | `NULL` | `contactPhone` | Central telefónica o WhatsApp de ventas |
| `url_logo` | `VARCHAR(1000)` | `NULL` | `logo` | URL del isotipo o logotipo de la empresa |
| `url_banner` | `VARCHAR(1000)` | `NULL` | `banner` | URL de la cabecera publicitaria del perfil |
| `direccion` | `VARCHAR(300)` | `NULL` | `address` | Sede central o dirección de almacén principal |
| `ciudad` | `VARCHAR(100)` | `NULL` | `city` | Ciudad base de operaciones |
| `anos_mercado` | `VARCHAR(30)` | `NULL` | `yearsInMarket` | Trayectoria comercial (ej. `"12 años"`) |
| `descripcion` | `TEXT` | `NULL` | `description` | Reseña institucional de la empresa mayorista |
| `politica_comercial`| `TEXT` | `NULL` | `commercialPolicy` | Condiciones de facturación, despacho y garantías |
| `calificacion` | `NUMERIC(3,2)` | `NULL, DEFAULT 0.00, CHECK (0-5)` | `rating` | Promedio de reputación B2B |
| `verificado` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `verified` | Badge de mayorista formal auditado por Admin |
| `destacado` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `featured` | Bandera para aparecer en el Home del Cliente |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha de alta del perfil |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Actualizado automáticamente por trigger |

* **Índices:** `idx_proveedor_destacado` (ALTA), `idx_proveedor_verificado` (ALTA), `idx_proveedor_ciudad` (MEDIA).

#### 8. Tabla: `proveedor_marcas`
Marcas oficiales de tecnología distribuidas por el proveedor (normalización de arreglo).

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE CASCADE` | `providerId` | Proveedor dueño |
| `marca` | `VARCHAR(100)` | `NOT NULL` | `brand` | Marca (ej. `"Kingston"`, `"Corsair"`, `"Gigabyte"`) |

* **Restricción:** `UNIQUE (proveedor_id, marca)`.
* **Índice:** `idx_marca` (ALTA).

#### 9. Tabla: `proveedor_especialidades`
Líneas de producto y especialidades técnicas del proveedor.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE CASCADE` | `providerId` | Proveedor dueño |
| `especialidad` | `VARCHAR(150)` | `NOT NULL` | `specialty` | Especialidad (ej. `"Almacenamiento NVMe"`, `"DDR5"`) |

* **Restricción:** `UNIQUE (proveedor_id, especialidad)`.

#### 10. Tabla: `solicitudes_proveedor`
Formulario de postulación de empresas externas interesadas en ser proveedores mayoristas autorizados.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de la postulación |
| `razon_social` | `VARCHAR(255)` | `NOT NULL` | `companyName` | Razón social de la empresa postulante |
| `nombre_contacto` | `VARCHAR(200)` | `NOT NULL` | `contactName` | Nombre del representante o gerente de ventas |
| `correo` | `VARCHAR(255)` | `NOT NULL` | `email` | Correo de contacto comercial |
| `telefono` | `VARCHAR(50)` | `NOT NULL` | `phone` | Teléfono de contacto |
| `ciudad` | `VARCHAR(100)` | `NULL` | `city` | Ciudad de la empresa |
| `direccion` | `VARCHAR(300)` | `NULL` | `address` | Dirección de oficinas / almacén |
| `marcas_texto` | `TEXT` | `NULL` | `brandsText` | Marcas que distribuye en texto libre |
| `mensaje` | `TEXT` | `NULL` | `message` | Presentación comercial o portafolio |
| `estado` | `estado_solicitud` | `NOT NULL, DEFAULT 'PENDIENTE'` | `status` (`PENDIENTE`,`APROBADA`,`RECHAZADA`) | Estado de la revisión por el Administrador |
| `motivo_rechazo` | `TEXT` | `NULL` | `rejectionReason` | Motivo en caso de no cumplir requisitos |
| `admin_revisor_id` | `BIGINT` | `NULL, FK → usuarios(id) ON DELETE SET NULL` | `reviewerAdminId` | Administrador que auditó la postulación |
| `fecha_solicitud` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha de envío del formulario |
| `fecha_revision` | `TIMESTAMPTZ` | `NULL` | `reviewedAt` | Fecha en que el administrador evaluó |
| `proveedor_creado_id`| `BIGINT` | `NULL, FK → perfiles_proveedor(id) ON DELETE SET NULL` | `createdProviderId` | Perfil creado si la solicitud fue aprobada |

* **Índices:** `idx_solicitud_estado` (ALTA), `idx_solicitud_correo` (MEDIA).

---

### MÓDULO 4: CATÁLOGO, CATEGORÍAS Y PRODUCTOS

#### 11. Tabla: `categorias`
Maestra de familias de componentes de computación autorizadas (rubro exclusivo de tecnología).

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `VARCHAR(50)` | `PRIMARY KEY` | `id` (ej. `"ram"`, `"ssd"`) | Clave textual canónica (slug) |
| `nombre` | `VARCHAR(100)` | `NOT NULL` | `name` | Nombre para visualización en UI |
| `icono` | `VARCHAR(50)` | `NULL` | `icon` (ej. `"Cpu"`, `"Layers"`) | Nombre del icono de Lucide / Bootstrap |
| `descripcion` | `VARCHAR(300)` | `NULL` | `description` | Breve descripción técnica de la categoría |
| `estado` | `estado_categoria` | `NOT NULL, DEFAULT 'ACTIVA'` | `status` (`ACTIVA`,`INACTIVA`) | Control de visibilidad en el catálogo |
| `total_productos` | `INT` | `NOT NULL, DEFAULT 0` | `totalProducts` | Contador de productos para dashboards |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha de creación de la categoría |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Actualizado por trigger |

* **Índice:** `idx_categoria_estado` (MEDIA).

#### 12. Tabla: `subcategorias`
Desglose técnico interno de cada categoría (ej. DDR4, DDR5, PCIe 4.0).

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador autonumérico |
| `categoria_id` | `VARCHAR(50)` | `NOT NULL, FK → categorias(id) ON DELETE CASCADE` | `categoryId` | Categoría padre |
| `nombre` | `VARCHAR(100)` | `NOT NULL` | `name` | Nombre de la subcategoría |

* **Índice:** `idx_subcat_categoria` (ALTA).

#### 13. Tabla: `productos`
Entidad transaccional central del sistema. Representa una publicación B2B mayorista por volumen.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador interno relacional |
| `uuid` | `UUID` | `NOT NULL, UNIQUE, DEFAULT gen_random_uuid()` | `uuid` | UUID público para rutas de catálogo |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE RESTRICT` | `providerId` | Empresa mayorista que comercializa |
| `categoria_id` | `VARCHAR(50)` | `NOT NULL, FK → categorias(id) ON DELETE RESTRICT` | `categoryId` | Categoría técnica del componente |
| `titulo` | `VARCHAR(500)` | `NOT NULL` | `title` | Título comercial descriptivo |
| `descripcion` | `TEXT` | `NOT NULL` | `description` | Ficha técnica y alcance del producto |
| `modelo_comercial`| `modelo_comercial`| `NOT NULL` | `commercialModel` | `VENTA_MAYORISTA_TRADICIONAL`, `DISTRIBUCION_EXCLUSIVA`, `LOTE_STOCK` |
| `tipo_formato` | `VARCHAR(200)` | `NOT NULL` | `formatType` | Empaque (ej. `"Caja Máster Sellada"`, `"Palé"`) |
| `unidades_por_paquete`| `VARCHAR(100)`| `NULL` | `unitsPerPackage` | Contenido por bulto (ej. `"20 unidades"`) |
| `pedido_minimo` | `VARCHAR(200)` | `NULL` | `moq` | Mínimo de compra (ej. `"2 Cajas Máster"`) |
| `precio_unitario_ref`| `NUMERIC(10,2)`| `NOT NULL, CHECK (> 0)` | `referencePriceUnit` | Precio referencial unitario en USD |
| `precio_total_ref`| `NUMERIC(12,2)`| `NULL` | `referencePriceTotal` | Precio del lote o bulto completo en USD |
| `moneda` | `VARCHAR(3)` | `NOT NULL, DEFAULT 'USD'` | `currency` | Moneda de cotización (fija en USD) |
| `terminos_comerciales`| `TEXT` | `NULL` | `commercialTerms` | Garantía, tiempo de entrega y pago |
| `estado` | `estado_producto`| `NOT NULL, DEFAULT 'ACTIVO'` | `status` | `ACTIVO`, `DESHABILITADO_POR_PROVEEDOR`, `OCULTO_POR_ADMIN`, `ELIMINADO_LOGICO` |
| `motivo_moderacion`| `TEXT` | `NULL` | `moderationReason` | Razón de bloqueo si fue ocultado por Admin |
| `es_recomendado` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `isRecommended` | Curado por el Admin para el Home |
| `es_promocionado` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `isPromoted` | Activado por campaña publicitaria pagada |
| `tiene_oferta` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `hasOffer` | Bandera si cuenta con oferta activa |
| `fecha_publicacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha en que se publicó en catálogo |
| `fecha_actualizacion`| `TIMESTAMPTZ`| `NOT NULL, DEFAULT NOW()` | `updatedAt` | Modificación por trigger |
| `fecha_eliminacion`| `TIMESTAMPTZ` | `NULL` | `deletedAt` | Timestamp de eliminación lógica |
| `eliminado_por` | `BIGINT` | `NULL, FK → usuarios(id) ON DELETE SET NULL` | `deletedBy` | Usuario que realizó la baja lógica |

* **Índices:** `idx_producto_proveedor` (ALTA), `idx_producto_categoria` (ALTA), `idx_producto_estado` (ALTA), `idx_producto_busqueda` GIN (ALTA: Full-text search en español), `idx_producto_precio` (MEDIA).

#### 14. Tabla: `producto_especificaciones`
Tabla hija normalizada con la ficha técnica clave-valor del componente de hardware.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de la especificación |
| `producto_id` | `BIGINT` | `NOT NULL, FK → productos(id) ON DELETE CASCADE` | `productId` | Producto al que pertenece |
| `clave` | `VARCHAR(100)` | `NOT NULL` | `key` / `clave` | Nombre del atributo (ej. `"Interfaz"`, `"Capacidad"`) |
| `valor` | `VARCHAR(300)` | `NOT NULL` | `value` / `valor` | Valor técnico (ej. `"PCIe 4.0 x4"`, `"32GB"`) |

* **Restricción:** `UNIQUE (producto_id, clave)`.

#### 15. Tabla: `imagenes_producto`
Galería de fotografías de alta resolución del producto y sus empaques.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de la imagen |
| `producto_id` | `BIGINT` | `NOT NULL, FK → productos(id) ON DELETE CASCADE` | `productId` | Producto asociado |
| `url_imagen` | `VARCHAR(1000)` | `NOT NULL` | `url` | Enlace HTTPS de la imagen |
| `orden` | `SMALLINT` | `NOT NULL, DEFAULT 0` | `order` | Orden de visualización (0 = foto de portada) |

* **Índice:** `idx_imagen_producto` (ALTA).

---

### MÓDULO 5: OFERTAS Y PUBLICIDAD

#### 16. Tabla: `ofertas`
Promociones temporales de descuento por volumen aplicadas sobre productos activos.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de la oferta |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE RESTRICT` | `providerId` | Empresa dueña de la oferta |
| `producto_id` | `BIGINT` | `NOT NULL, FK → productos(id) ON DELETE RESTRICT` | `productId` | Producto al que aplica la rebaja |
| `titulo_oferta` | `VARCHAR(300)` | `NOT NULL` | `offerTitle` | Título promocional (ej. `"12% OFF Lanzamiento"`) |
| `porcentaje_descuento`| `SMALLINT` | `NOT NULL, CHECK (1-99)` | `discountPercent` | Porcentaje numérico de rebaja |
| `precio_original` | `NUMERIC(10,2)` | `NOT NULL` | `originalPrice` | Precio regular de catálogo |
| `precio_promocional` | `NUMERIC(10,2)` | `NOT NULL, CHECK (> 0)` | `promotionalPrice` | Precio rebajado con descuento |
| `moneda` | `VARCHAR(3)` | `NOT NULL, DEFAULT 'USD'` | `currency` | Moneda de cotización (USD) |
| `vigencia` | `VARCHAR(100)` | `NULL` | `validUntil` | Fecha o texto de expiración |
| `terminos` | `TEXT` | `NULL` | `terms` | Condiciones de la oferta mayorista |
| `estado` | `estado_oferta` | `NOT NULL, DEFAULT 'ACTIVA'` | `status` | `ACTIVA`, `DESHABILITADA`, `OCULTA_POR_ADMIN`, `ELIMINADA_LOGICA` |
| `motivo_moderacion` | `TEXT` | `NULL` | `moderationReason` | Justificación si el Admin la dio de baja |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Fecha de publicación de la oferta |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Modificación por trigger |
| `fecha_eliminacion` | `TIMESTAMPTZ` | `NULL` | `deletedAt` | Timestamp de eliminación lógica |

* **Restricción:** `CHECK (precio_promocional < precio_original)`.
* **Índices:** `idx_oferta_proveedor` (MEDIA), `idx_oferta_producto` (ALTA), `idx_oferta_estado` (ALTA).

#### 17. Tabla: `planes_publicidad`
Catálogo de membresías y paquetes de visibilidad comercial para proveedores.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador del plan |
| `nombre` | `VARCHAR(200)` | `NOT NULL` | `name` | Nombre (ej. `"Plan Pro Mayorista"`) |
| `dias_duracion` | `SMALLINT` | `NOT NULL, CHECK (> 0)` | `durationDays` | Vigencia de la campaña (7, 15 o 30 días) |
| `costo_usd` | `NUMERIC(10,2)` | `NOT NULL` | `cost` | Tarifa publicitaria en USD ($49, $89, $149) |
| `descripcion` | `TEXT` | `NULL` | `description` | Beneficios del plan de visibilidad |
| `espacios_asignados`| `VARCHAR(300)` | `NULL` | `spaces` | Ubicaciones (ej. `"Bloque Promocionados en Inicio"`) |
| `activo` | `BOOLEAN` | `NOT NULL, DEFAULT TRUE` | `active` | Si el plan está disponible para contratación |
| `popular` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `popular` | Badge visual de "Más Popular" en UI |

* **Índice:** `idx_plan_activo` (MEDIA).

#### 18. Tabla: `promociones`
Campañas publicitarias contratadas y activadas por un proveedor sobre un producto.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de la campaña |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE RESTRICT` | `providerId` | Empresa contratante |
| `producto_id` | `BIGINT` | `NOT NULL, FK → productos(id) ON DELETE RESTRICT` | `productId` | Producto promocionado en inicio |
| `plan_publicidad_id`| `BIGINT` | `NOT NULL, FK → planes_publicidad(id) ON DELETE RESTRICT` | `planId` | Paquete contratado |
| `nombre_plan` | `VARCHAR(200)` | `NOT NULL` | `planName` | Nombre desnormalizado para historial |
| `costo_usd` | `NUMERIC(10,2)` | `NOT NULL` | `costUsd` | Importe abonado |
| `dias_duracion` | `SMALLINT` | `NOT NULL` | `durationDays` | Duración total contratada |
| `espacios_asignados`| `VARCHAR(300)` | `NULL` | `assignedSpaces` | Ubicaciones asignadas |
| `fecha_inicio` | `TIMESTAMPTZ` | `NOT NULL` | `startDate` | Fecha y hora de activación |
| `fecha_fin` | `TIMESTAMPTZ` | `NOT NULL` | `endDate` | Fecha de expiración de la campaña |
| `estado` | `estado_promocion`| `NOT NULL, DEFAULT 'ACTIVA'` | `status` (`ACTIVA`,`EXPIRADA`,`CANCELADA`) | Estado actual de la campaña |
| `nota_moderacion` | `TEXT` | `NULL` | `moderationNote` | Observación administrativa |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Registro de la contratación |
| `fecha_cancelacion` | `TIMESTAMPTZ` | `NULL` | `canceledAt` | En caso de baja anticipada |

* **Restricción:** `CHECK (fecha_fin > fecha_inicio)`.
* **Índices:** `idx_promo_proveedor` (MEDIA), `idx_promo_producto` (ALTA), `idx_promo_estado` (ALTA), `idx_promo_fecha_fin` (ALTA: expiración automática).

---

### MÓDULO 6: MENSAJERÍA B2B Y NEGOCIACIÓN

#### 19. Tabla: `conversaciones`
Canal de negociación privado 1-a-1 entre un Cliente y un Proveedor.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador interno |
| `uuid` | `UUID` | `NOT NULL, UNIQUE, DEFAULT gen_random_uuid()` | `uuid` | UUID público de la conversación |
| `cliente_id` | `BIGINT` | `NOT NULL, FK → perfiles_cliente(id) ON DELETE RESTRICT` | `clientId` | Cliente comprador |
| `proveedor_id` | `BIGINT` | `NOT NULL, FK → perfiles_proveedor(id) ON DELETE RESTRICT` | `providerId` | Empresa proveedora |
| `producto_referenciado_id` | `BIGINT` | `NULL, FK → productos(id) ON DELETE SET NULL` | `referencedProductId` | Producto que inició la cotización |
| `fecha_ultimo_mensaje` | `TIMESTAMPTZ` | `NULL` | `lastMessageTime` | Para ordenar la bandeja de entrada |
| `vista_previa_ultimo` | `VARCHAR(500)` | `NULL` | `lastMessagePreview` | Texto breve para el listado de chats |
| `no_leidos_cliente` | `INT` | `NOT NULL, DEFAULT 0` | `unreadCountClient` | Mensajes pendientes de leer por el cliente |
| `no_leidos_proveedor` | `INT` | `NOT NULL, DEFAULT 0` | `unreadCountProvider`| Mensajes pendientes de leer por el proveedor |
| `fecha_creacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `createdAt` | Creación del hilo de chat |

* **Restricción:** `UNIQUE (cliente_id, proveedor_id)` (un solo hilo continuo por par de actores).
* **Índices:** `idx_conv_ultimo_mensaje` (ALTA), `idx_conv_proveedor` (MEDIA).

#### 20. Tabla: `mensajes`
Mensajes individuales dentro de una conversación con control de lectura y autoría.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador del mensaje |
| `conversacion_id` | `BIGINT` | `NOT NULL, FK → conversaciones(id) ON DELETE CASCADE` | `chatId` | Hilo de chat al que pertenece |
| `usuario_remitente_id` | `BIGINT` | `NOT NULL, FK → usuarios(id) ON DELETE RESTRICT` | `senderId` | Usuario autor del mensaje |
| `rol_remitente` | `rol_remitente` | `NOT NULL` | `senderRole` (`CLIENTE` o `PROVEEDOR`) | Rol de quien envía para pintar burbuja de chat |
| `texto` | `TEXT` | `NOT NULL` | `text` | Contenido del mensaje de negociación |
| `leido` | `BOOLEAN` | `NOT NULL, DEFAULT FALSE` | `isRead` | Indicador de doble check azul de lectura |
| `fecha_envio` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `timestamp` | Fecha y hora exacta de envío |

* **Índices:** `idx_mensaje_conversacion` (ALTA), `idx_mensaje_fecha` (ALTA), `idx_mensaje_leido` (MEDIA).

---

### MÓDULO 7: PLATAFORMA Y AUDITORÍA

#### 21. Tabla: `contenido_plataforma`
Gestor de contenidos estáticos y páginas institucionales editables por el Administrador.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador único |
| `clave_contenido` | `VARCHAR(50)` | `NOT NULL, UNIQUE` | `sectionKey` (`'nosotros'`, `'contacto'`) | Clave de sección editable |
| `contenido_json` | `JSONB` | `NOT NULL` | `content` (Objeto JSON) | Documento con misión, visión, teléfonos oficiales |
| `fecha_actualizacion`| `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `updatedAt` | Última edición |
| `admin_actualizador_id`| `BIGINT` | `NULL, FK → usuarios(id) ON DELETE SET NULL` | `updatedByAdminId` | Administrador que aplicó los cambios |

#### 22. Tabla: `registros_auditoria`
Libro mayor inmutable de trazabilidad (*Audit Log*). Registra cada baja lógica o moderación forzosa.

| Campo | Tipo PostgreSQL | Restricciones | Mapeo React / DTO | Descripción |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | `id` | Identificador de auditoría |
| `tipo_entidad` | `tipo_entidad_auditoria`| `NOT NULL` | `entityType` (`PRODUCTO`, `OFERTA`, etc.) | Naturaleza del recurso afectado |
| `id_entidad_original` | `BIGINT` | `NOT NULL` | `originalEntityId` | ID original en su tabla correspondiente |
| `titulo_entidad` | `VARCHAR(500)` | `NOT NULL` | `entityTitle` | Título del producto u oferta eliminada |
| `usuario_eliminador_id`| `BIGINT` | `NULL, FK → usuarios(id) ON DELETE SET NULL` | `deletedByUserId` | Responsable de la acción |
| `rol_eliminador` | `rol_eliminador`| `NOT NULL` | `deletedByRole` (`PROVEEDOR` o `ADMIN`) | Nivel de privilegio del actor |
| `fecha_eliminacion` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT NOW()` | `deletedAt` | Timestamp exacto de la baja lógica |
| `motivo` | `TEXT` | `NULL` | `reason` | Explicación o causa de la moderación |
| `datos_snapshot` | `JSONB` | `NOT NULL` | `snapshot` | Copia de respaldo en JSON del registro antes de borrar |
| `estado` | `VARCHAR(50)` | `NOT NULL, DEFAULT 'ARCHIVADO'`| `status` | Estado del registro de auditoría |

* **Índices:** `idx_auditoria_tipo` (MEDIA), `idx_auditoria_fecha` (BAJA), `idx_auditoria_rol` (MEDIA).
