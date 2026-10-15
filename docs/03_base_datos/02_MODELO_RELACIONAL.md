# 03. Modelo Relacional — Diagrama de Entidades y Relaciones

> Plataforma COMPUNEX B2B — PostgreSQL 15+ Normalizado Español

---

## Diagrama Entidad-Relación (Mermaid) — Español Normalizado

```mermaid
erDiagram
    usuarios {
        bigserial id PK
        uuid uuid UK
        varchar nombre
        varchar correo UK
        varchar hash_contrasena
        enum rol
        enum estado
        text motivo_estado
        timestamptz fecha_registro
        timestamptz ultimo_acceso
        timestamptz fecha_actualizacion
    }

    perfiles_cliente {
        bigserial id PK
        bigint usuario_id FK
        varchar nombre_tienda
        varchar telefono
        varchar ciudad
        varchar direccion
        timestamptz fecha_creacion
        timestamptz fecha_actualizacion
    }

    preferencias_cliente {
        bigserial id PK
        bigint perfil_cliente_id FK
        timestamptz fecha_actualizacion
    }
    preferencias_cliente_categorias {
        bigserial id PK
        bigint preferencia_id FK
        varchar categoria_id FK
    }
    preferencias_cliente_modelos {
        bigserial id PK
        bigint preferencia_id FK
        enum modelo_comercial
    }
    preferencias_cliente_palabras {
        bigserial id PK
        bigint preferencia_id FK
        varchar palabra_clave
    }

    perfiles_proveedor {
        bigserial id PK
        bigint usuario_id FK
        varchar razon_social
        varchar nombre_comercial
        varchar telefono_contacto
        varchar url_logo
        varchar url_banner
        varchar direccion
        varchar ciudad
        varchar anos_mercado
        text descripcion
        text politica_comercial
        numeric calificacion
        boolean verificado
        boolean destacado
        timestamptz fecha_creacion
        timestamptz fecha_actualizacion
    }
    proveedor_marcas {
        bigserial id PK
        bigint proveedor_id FK
        varchar marca
    }
    proveedor_especialidades {
        bigserial id PK
        bigint proveedor_id FK
        varchar especialidad
    }

    solicitudes_proveedor {
        bigserial id PK
        varchar razon_social
        varchar nombre_contacto
        varchar correo
        varchar telefono
        varchar ciudad
        varchar direccion
        text marcas_texto
        text mensaje
        enum estado
        text motivo_rechazo
        bigint admin_revisor_id FK
        timestamptz fecha_solicitud
        timestamptz fecha_revision
        bigint proveedor_creado_id FK
    }

    categorias {
        varchar id PK
        varchar nombre
        varchar icono
        varchar descripcion
        enum estado
        int total_productos
        timestamptz fecha_creacion
        timestamptz fecha_actualizacion
    }

    subcategorias {
        bigserial id PK
        varchar categoria_id FK
        varchar nombre
    }

    productos {
        bigserial id PK
        uuid uuid UK
        bigint proveedor_id FK
        varchar categoria_id FK
        varchar titulo
        text descripcion
        enum modelo_comercial
        varchar tipo_formato
        varchar unidades_por_paquete
        varchar pedido_minimo
        numeric precio_unitario_ref
        numeric precio_total_ref
        varchar moneda
        text terminos_comerciales
        enum estado
        text motivo_moderacion
        boolean es_recomendado
        boolean es_promocionado
        boolean tiene_oferta
        timestamptz fecha_publicacion
        timestamptz fecha_actualizacion
        timestamptz fecha_eliminacion
        bigint eliminado_por FK
    }
    producto_especificaciones {
        bigserial id PK
        bigint producto_id FK
        varchar clave
        varchar valor
    }

    imagenes_producto {
        bigserial id PK
        bigint producto_id FK
        varchar url_imagen
        smallint orden
    }

    ofertas {
        bigserial id PK
        bigint proveedor_id FK
        bigint producto_id FK
        varchar titulo_oferta
        smallint porcentaje_descuento
        numeric precio_original
        numeric precio_promocional
        varchar moneda
        varchar vigencia
        text terminos
        enum estado
        text motivo_moderacion
        timestamptz fecha_creacion
        timestamptz fecha_actualizacion
        timestamptz fecha_eliminacion
    }

    planes_publicidad {
        bigserial id PK
        varchar nombre
        smallint dias_duracion
        numeric costo_usd
        text descripcion
        varchar espacios_asignados
        boolean activo
        boolean popular
    }

    promociones {
        bigserial id PK
        bigint proveedor_id FK
        bigint producto_id FK
        bigint plan_publicidad_id FK
        varchar nombre_plan
        numeric costo_usd
        smallint dias_duracion
        varchar espacios_asignados
        timestamptz fecha_inicio
        timestamptz fecha_fin
        enum estado
        text nota_moderacion
        timestamptz fecha_creacion
        timestamptz fecha_cancelacion
    }

    conversaciones {
        bigserial id PK
        uuid uuid UK
        bigint cliente_id FK
        bigint proveedor_id FK
        bigint producto_referenciado_id FK
        timestamptz fecha_ultimo_mensaje
        varchar vista_previa_ultimo
        int no_leidos_cliente
        int no_leidos_proveedor
        timestamptz fecha_creacion
    }

    mensajes {
        bigserial id PK
        bigint conversacion_id FK
        bigint usuario_remitente_id FK
        enum rol_remitente
        text texto
        boolean leido
        timestamptz fecha_envio
    }

    contenido_plataforma {
        bigserial id PK
        varchar clave_contenido UK
        jsonb contenido_json
        timestamptz fecha_actualizacion
        bigint admin_actualizador_id FK
    }

    registros_auditoria {
        bigserial id PK
        enum tipo_entidad
        bigint id_entidad_original
        varchar titulo_entidad
        bigint usuario_eliminador_id FK
        enum rol_eliminador
        timestamptz fecha_eliminacion
        text motivo
        jsonb datos_snapshot
        varchar estado
    }

    usuarios ||--o| perfiles_cliente : "tiene perfil"
    usuarios ||--o| perfiles_proveedor : "tiene perfil"
    perfiles_cliente ||--o| preferencias_cliente : "tiene preferencias"
    preferencias_cliente ||--o{ preferencias_cliente_categorias : "categorías"
    preferencias_cliente ||--o{ preferencias_cliente_modelos : "modelos"
    preferencias_cliente ||--o{ preferencias_cliente_palabras : "palabras clave"
    perfiles_proveedor ||--o{ proveedor_marcas : "marcas"
    perfiles_proveedor ||--o{ proveedor_especialidades : "especialidades"
    perfiles_cliente ||--o{ conversaciones : "inicia"
    perfiles_proveedor ||--o{ conversaciones : "recibe"
    perfiles_proveedor ||--o{ productos : "publica"
    perfiles_proveedor ||--o{ ofertas : "crea"
    perfiles_proveedor ||--o{ promociones : "contrata"
    categorias ||--o{ productos : "clasifica"
    categorias ||--o{ subcategorias : "tiene"
    productos ||--o{ imagenes_producto : "imágenes"
    productos ||--o{ producto_especificaciones : "especificaciones"
    productos ||--o{ ofertas : "tiene ofertas"
    productos ||--o{ promociones : "campañas"
    productos }o--o{ conversaciones : "referenciado en"
    planes_publicidad ||--o{ promociones : "define"
    conversaciones ||--o{ mensajes : "contiene"
    solicitudes_proveedor }o--o| perfiles_proveedor : "origina"
    usuarios }o--o{ registros_auditoria : "ejecuta"
```

---

## Cardinalidades Clave (PostgreSQL Español)

| Relación | Cardinalidad | Descripción |
|---|---|---|
| usuarios → perfiles_cliente | 1:1 | Un usuario CLIENTE tiene exactamente un perfil |
| usuarios → perfiles_proveedor | 1:1 | Un usuario PROVEEDOR tiene exactamente un perfil |
| perfiles_cliente → preferencias_cliente | 1:1 | Un cliente tiene exactamente un set de preferencias |
| preferencias_cliente → preferencias_cliente_categorias/modelos/palabras | 1:N | Normalización de arreglos |
| perfiles_proveedor → proveedor_marcas/especialidades | 1:N | Normalización de JSON |
| perfiles_proveedor → productos | 1:N | Un proveedor tiene muchos productos |
| perfiles_proveedor → ofertas | 1:N | Un proveedor tiene muchas ofertas |
| perfiles_proveedor → promociones | 1:N | Un proveedor tiene muchas campañas |
| categorias → productos | 1:N | Una categoría agrupa muchos productos |
| categorias → subcategorias | 1:N | Una categoría tiene varias subcategorías |
| productos → imagenes_producto | 1:N | Un producto tiene múltiples imágenes |
| productos → producto_especificaciones | 1:N | Un producto tiene múltiples specs clave-valor |
| productos → ofertas | 1:N | Un producto puede tener varias ofertas históricas |
| productos → promociones | 1:N | Un producto puede ser promocionado en diferentes períodos |
| planes_publicidad → promociones | 1:N | Un plan sirve de base para múltiples campañas |
| perfiles_cliente + perfiles_proveedor → conversaciones | N:M a través de conversaciones | Par único por UNIQUE (cliente_id, proveedor_id) |
| conversaciones → mensajes | 1:N | Una conversación tiene muchos mensajes |

---

## Flujos de Relación Principales (PostgreSQL)

### Flujo: Registro de Cliente
```
POST /register
→ Inserta: usuarios (rol=CLIENTE)
→ Inserta: perfiles_cliente (usuario_id)
→ Inserta: preferencias_cliente (perfil_cliente_id) — vacías
```

### Flujo: Aprobación de Proveedor
```
POST /contact → Inserta: solicitudes_proveedor (estado=PENDIENTE)
ADMIN → PATCH /admin/applications/{id}/approve
→ Actualiza: solicitudes_proveedor (estado=APROBADA)
→ Inserta: usuarios (rol=PROVEEDOR)
→ Inserta: perfiles_proveedor (usuario_id) + proveedor_marcas / especialidades
→ Actualiza: solicitudes_proveedor.proveedor_creado_id
```

### Flujo: Creación de Producto
```
PROVEEDOR → POST /providers/me/products
→ Inserta: productos (proveedor_id, estado=ACTIVO, es_recomendado=false)
→ Inserta: imagenes_producto (producto_id, url_imagen)
→ Inserta: producto_especificaciones (producto_id, clave, valor)
→ Actualiza: categorias.total_productos + 1
```

### Flujo: Oferta sobre Producto
```
PROVEEDOR → POST /providers/me/offers
→ Inserta: ofertas (producto_id, estado=ACTIVA)
→ Actualiza: productos.tiene_oferta = true
```

### Flujo: Campaña Publicitaria
```
PROVEEDOR → POST /providers/me/promotions
→ Inserta: promociones (fecha_inicio=NOW(), fecha_fin=NOW()+dias_duracion, estado=ACTIVA)
→ Actualiza: productos.es_promocionado = true
```

### Flujo: Eliminación Lógica de Producto (Proveedor)
```
PROVEEDOR → DELETE /providers/me/products/{id}
→ Actualiza: productos.estado = ELIMINADO_LOGICO, fecha_eliminacion = NOW()
→ Inserta: registros_auditoria (datos_snapshot = JSONB del producto)
→ Actualiza: categorias.total_productos - 1
```

### Flujo: Inicio de Chat
```
CLIENTE → POST /chat/conversations
→ Si existe conversación con ese proveedor: retorna existente (UNIQUE cliente_id, proveedor_id)
→ Si no: Inserta conversaciones (cliente_id, proveedor_id, producto_referenciado_id?)
CLIENTE → POST /chat/conversations/{id}/messages
→ Inserta: mensajes (conversacion_id, usuario_remitente_id, texto)
→ Actualiza: conversaciones.fecha_ultimo_mensaje, vista_previa_ultimo, no_leidos_proveedor + 1
```

---

## Diagrama Simplificado de Flujo de Estados (PostgreSQL)

### Estados de `productos.estado`
```
ACTIVO ──[proveedor deshabilita]──→ DESHABILITADO_POR_PROVEEDOR
DESHABILITADO_POR_PROVEEDOR ──[proveedor reactiva]──→ ACTIVO

ACTIVO ──[admin oculta]──→ OCULTO_POR_ADMIN
OCULTO_POR_ADMIN ──[admin restaura]──→ ACTIVO

ACTIVO ──[proveedor/admin elimina]──→ ELIMINADO_LOGICO (terminal)
DESHABILITADO_POR_PROVEEDOR ──[admin elimina]──→ ELIMINADO_LOGICO (terminal)
OCULTO_POR_ADMIN ──[admin elimina]──→ ELIMINADO_LOGICO (terminal)
```

### Estados de `ofertas.estado`
```
ACTIVA ──[proveedor deshabilita]──→ DESHABILITADA
DESHABILITADA ──[proveedor activa]──→ ACTIVA

ACTIVA ──[admin oculta]──→ OCULTA_POR_ADMIN
OCULTA_POR_ADMIN ──[admin restaura]──→ ACTIVA

ACTIVA | DESHABILITADA ──[proveedor/admin elimina]──→ ELIMINADA_LOGICA (terminal)
```

### Estados de `solicitudes_proveedor.estado`
```
PENDIENTE ──[admin aprueba]──→ APROBADA (terminal)
PENDIENTE ──[admin rechaza]──→ RECHAZADA (terminal)
```

### Estados de `promociones.estado`
```
ACTIVA ──[tiempo expirado]──→ EXPIRADA (automático por fecha_fin)
ACTIVA ──[admin cancela]──→ CANCELADA (terminal)
```

### Estados de `usuarios.estado`
```
ACTIVO ──[admin desactiva]──→ INACTIVO
INACTIVO ──[admin activa]──→ ACTIVO
ACTIVO | INACTIVO ──[admin bloquea]──→ BLOQUEADO
BLOQUEADO ──[admin desbloquea]──→ ACTIVO
```
