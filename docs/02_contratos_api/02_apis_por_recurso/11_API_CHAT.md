# 11. API de Chat Privado Cliente-Proveedor

> Plataforma COMPUNEX B2B — Endpoints de Chat 1-1 — PostgreSQL
> Fuente única: `git/diseno_cliente/src/components/views/ChatView.jsx` + `git/diseno_cliente/src/context/ClientContext.jsx:83` `startChatWithProvider/sendMessage/isProviderTyping` + `git/diseno_proveedor/src/components/views/ProviderChatView.jsx` + `git/diseno_proveedor/src/context/ProviderContext.jsx:205` `sendMessage/isClientTyping` + `git/diseno_cliente/src/data/mockData.js:431` `INITIAL_CHATS` + `git/diseno_proveedor/src/data/providerMockData.js:271` `INITIAL_PROVIDER_CHATS`
> No se inventan funcionalidades fuera de esas vistas/contexts.

---

## Modelo PostgreSQL

**Tablas:** `conversaciones` + `mensajes` (`04_SQL_DATABASE.sql:conversaciones/mensajes`)

- `conversaciones` (id BIGSERIAL PK, uuid UUID UNIQUE DEFAULT gen_random_uuid(), cliente_id FK → `perfiles_cliente.id`, proveedor_id FK → `perfiles_proveedor.id`, producto_referenciado_id FK → `productos.id` NULL, fecha_ultimo_mensaje TIMESTAMPTZ, vista_previa_ultimo VARCHAR(500), no_leidos_cliente INT DEFAULT 0, no_leidos_proveedor INT DEFAULT 0, fecha_creacion) + `UNIQUE(cliente_id, proveedor_id)` + índices `idx_conv_ultimo_mensaje DESC`.
- `mensajes` (id BIGSERIAL PK, conversacion_id FK → `conversaciones.id` ON DELETE CASCADE, usuario_remitente_id FK → `usuarios.id`, rol_remitente ENUM `CLIENTE|PROVEEDOR`, texto TEXT, leido BOOLEAN DEFAULT FALSE, fecha_envio TIMESTAMPTZ DEFAULT NOW()).

**Demo:** `INITIAL_CHATS:431` — `chat_1` TechNova `SSD Kingston NV2`, `chat_2` CompuCore `Lote 50 SSD`, `chat_3` SiliconByte `RTX 4060`; cada `mensajes[]` con `id, sender (client/provider), text, timestamp, isRead`. Proveedor `INITIAL_PROVIDER_CHATS:271` — `chat_prov_1` TecnoStore Arequipa, `chat_prov_2` PC Gamer Trujillo, `chat_prov_3` CyberHard Cusco, con `referencedProductId/Title` y `unreadCount`.

---

## Endpoints Compartidos (CLIENTE | PROVEEDOR)

Todos requieren `Authorization: Bearer <JWT>` con `rol=CLIENTE` o `PROVEEDOR`. El backend filtra por `auth` (no expone conversaciones ajenas).

### GET /api/chat/conversaciones

**Vista:** Panel izquierdo de `ChatView.jsx` (cliente) — lista `CONVERSACIONES ACTIVAS ({chats.length})` con `providerLogo, providerName, lastMessage, lastMessageTime, referencedProductTitle (Box icon)`; y `ProviderChatView.jsx` — `SOLICITUDES DE COMPRADORES ({chats.length})` con `buyerAvatar, buyerStoreName, buyerName, buyerCity, lastMessage`.

**Acceso:** CLIENTE | PROVEEDOR — el backend resuelve `WHERE cliente_id = auth.perfil_cliente_id` o `proveedor_id = auth.perfil_proveedor_id`.

**Response HTTP 200:** `List<ConversacionResumenDTO>`
```json
[
  {
    "id": "uuid-conversacion",
    "clienteId": "uuid-cliente",
    "proveedorId": "uuid-proveedor",
    "proveedorNombre": "TechNova Mayorista S.A.C.",
    "proveedorLogo": "https://...",
    "clienteNombreTienda": "TecnoStore Express",
    "ultimoMensaje": "Estimado Carlos, sí tenemos disponibles las 4 cajas máster...",
    "fechaUltimoMensaje": "Hoy 10:45 AM",
    "noLeidos": 1,
    "productoReferenciadoId": "uuid-prod_1",
    "productoReferenciadoTitulo": "SSD Kingston NV2 1TB PCIe 4.0"
  }
]
```

**SQL:** `SELECT * FROM conversaciones WHERE cliente_id=:clienteId OR proveedor_id=:proveedorId ORDER BY fecha_ultimo_mensaje DESC NULLS LAST`

### POST /api/chat/conversaciones

**Vista:** Botón `Contactar`/`Cotizar` en `ProductCard.jsx` / `ProductDetailView.jsx` → `openProductDetail` → `startChatWithProvider(providerId, productId, initialMessage)` en `ClientContext.jsx:83`. Si ya existe conversación con ese `proveedorId`, se reutiliza (línea `existingChat`); si no, crea nueva.

**Request Body:**
```json
{
  "proveedorId": "uuid-proveedor",
  "productoId": "uuid-producto (opcional)",
  "mensajeInicial": "Hola TechNova, somos TecnoStore Express de Arequipa. Quisiéramos cotizar 4 Cajas Máster del SSD Kingston NV2..."
}
```

**Lógica:**
1. Buscar `SELECT * FROM conversaciones WHERE cliente_id=:auth.clienteId AND proveedor_id=:proveedorId` — si existe, retornar existente (no crea duplicado por `UNIQUE`).
2. Si no existe: `INSERT INTO conversaciones (uuid, cliente_id, proveedor_id, producto_referenciado_id, fecha_ultimo_mensaje=NOW(), vista_previa_ultimo=mensajeInicial, no_leidos_proveedor=1)` + `INSERT INTO mensajes (conversacion_id, usuario_remitente_id=auth.usuarioId, rol_remitente='CLIENTE', texto=mensajeInicial)`.
3. Retornar `ConversacionDTO` con `mensajes[]`.

**Response HTTP 201:** Conversación creada o existente + primer mensaje.
**HTTP 400:** `proveedorId` no existe o proveedor inactivo (`usuarios.estado != ACTIVO`).
**HTTP 403:** cliente intenta chatear consigo mismo.

**Nota de diseño existente vs DB:** `Demo` muestra `referencedProductId` distinto por conversación (`chat_prov_1 prod_1`, `chat_prov_2 prod_2`, `chat_prov_3 prod_7`), pero DB `UNIQUE(cliente_id,proveedor_id)` solo permite una fila por par y guarda solo el último `producto_referenciado_id`. Comportamiento observable: si cliente cotiza `prod_1` y luego `prod_3` con mismo proveedor, se reutiliza la misma conversación y se ve chip `Ref: ...` del último producto. No se crean hilos separados.

### GET /api/chat/conversaciones/{conversacionId}/mensajes

**Vista:** Panel derecho de `ChatView.jsx` — historial `flex-grow-1` con burbujas `justify-content-end` (cliente azul) vs `justify-content-start` (proveedor gris), `CheckCheck` leído, `Spinner isProviderTyping/isClientTyping`, chip `Cotizando: titulo` si `productoReferenciado`.

**Acceso:** CLIENTE | PROVEEDOR (participante) — `403` si `auth` no es `cliente_id` ni `proveedor_id` de la conversación.

**Query Params:** `page` (0-indexed), `size` (default 30), `orden=ASC` (cronológico)

**Response HTTP 200:** `Page<MensajeDTO>` — `SELECT * FROM mensajes WHERE conversacion_id=:id ORDER BY fecha_envio ASC LIMIT :size OFFSET :page*:size`

**DTO mensaje:** `{ id, conversacionId, remitenteId, rolRemitente: "CLIENTE"|"PROVEEDOR", texto, leido, fechaEnvio }`

### POST /api/chat/conversaciones/{conversacionId}/mensajes

**Vista:** Input `Escribe tu mensaje...` + botón `Enviar` + chips `Consultas rápidas` (`Plazo de despacho`, `Descuento por 5+ cajas`, `Solicitar Proforma` en cliente; `Confirmar Stock`, `Descuento 3% Contado`, `Solicitar Datos de Facturación` en proveedor) en `ChatView.jsx:268` y `ProviderChatView.jsx:252`.

**Request Body:** `{ texto: string (requerido, trim, no vacío) }`

**Lógica:**
1. `INSERT INTO mensajes (conversacion_id, usuario_remitente_id=auth.usuarioId, rol_remitente=auth.rol, texto, fecha_envio=NOW())`
2. `UPDATE conversaciones SET fecha_ultimo_mensaje=NOW(), vista_previa_ultimo=LEFT(texto,500), no_leidos_cliente = CASE WHEN rol_remitente='PROVEEDOR' THEN no_leidos_cliente+1 ELSE no_leidos_cliente END, no_leidos_proveedor = CASE WHEN rol_remitente='CLIENTE' THEN no_leidos_proveedor+1 ELSE no_leidos_proveedor END WHERE id=:conversacionId`
3. En demo, dispara `isProviderTyping/isClientTyping` con `setTimeout 1800ms` y respuesta automática (`ClientContext.jsx:164` `triggerSimulatedReply` con 4 respuestas aleatorias); en backend real es polling o WebSocket futuro (no implementado en diseño).

**Response HTTP 201:** `MensajeDTO`

### PUT /api/chat/conversaciones/{conversacionId}/lectura

**Vista:** Al abrir conversación, `ChatView` resetea `unreadCount` (observado en `ClientContext.jsx` `unreadCount`).

**Descripción:** Marca todos los mensajes del otro participante como leídos y resetea contador según rol.

**Acceso:** Participante

**Lógica:**
- Si `auth.rol=CLIENTE`: `UPDATE mensajes SET leido=true WHERE conversacion_id=:id AND rol_remitente='PROVEEDOR' AND leido=false` + `UPDATE conversaciones SET no_leidos_cliente=0 WHERE id=:id`
- Si `PROVEEDOR`: lo inverso sobre `no_leidos_proveedor`.

**Response HTTP 204**

---

## Reglas Observadas (no inventadas)

- **Privacidad:** `AdminContext` nunca lee `mensajes`; admin no aparece en chat. `02_roles_y_permisos.md:69` confirma `ADMIN` solo interviene por soporte/denuncia.
- **Sin transacción:** `ProviderChatView.jsx:139` nota `Las ventas no se debitan en plataforma; coordina transferencias y facturas directamente`.
- **Producto referenciado opcional:** `conversaciones.producto_referenciado_id` puede ser `NULL` (chat iniciado desde `Ver Perfil` sin producto).
- **Paginación mensajes:** `ChatView` muestra `overflow-y-auto` con `messagesEndRef` scroll al fondo; backend debe paginar `ASC` para scroll.
