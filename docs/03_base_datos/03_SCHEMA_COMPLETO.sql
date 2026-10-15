-- ============================================================
-- COMPUNEX B2B — Script de Base de Datos PostgreSQL NORMALIZADO (ESPAÑOL)
-- Plataforma B2B de Componentes de Computación
-- ============================================================
-- Versión: 2.1 — PostgreSQL 15+ | Español Normalizado 3FN + Índices de Prioridad
-- Motor: PostgreSQL 15+ | Encoding: UTF8
-- Principios:
--   1. Normalización 3FN: sin columnas JSON multivalor — tablas hijas normalizadas.
--   2. Sin RUC/Tax ID: eliminado por dato sensible (privacidad).
--   3. Sin control de stock ni checkout transaccional.
--   4. Eliminación lógica obligatoria (estado = 'ELIMINADO_LOGICO').
--   5. Índices de PRIORIDAD: ALTA (búsqueda/filtrado), MEDIA (reportes), BAJA (auditoría).
-- ============================================================

-- Extensión para generación de UUID v4
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tipos enumerados (PostgreSQL ENUM nativos)
DO $$ BEGIN
  CREATE TYPE rol_usuario AS ENUM ('ADMIN','PROVEEDOR','CLIENTE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_usuario AS ENUM ('ACTIVO','INACTIVO','BLOQUEADO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_solicitud AS ENUM ('PENDIENTE','APROBADA','RECHAZADA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_categoria AS ENUM ('ACTIVA','INACTIVA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE modelo_comercial AS ENUM ('VENTA_MAYORISTA_TRADICIONAL','DISTRIBUCION_EXCLUSIVA','LOTE_STOCK');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_producto AS ENUM ('ACTIVO','DESHABILITADO_POR_PROVEEDOR','OCULTO_POR_ADMIN','ELIMINADO_LOGICO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_oferta AS ENUM ('ACTIVA','DESHABILITADA','OCULTA_POR_ADMIN','ELIMINADA_LOGICA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_promocion AS ENUM ('ACTIVA','EXPIRADA','CANCELADA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE estado_conversacion AS ENUM ('ACTIVA'); -- reservado para futuro
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE rol_remitente AS ENUM ('CLIENTE','PROVEEDOR');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE tipo_entidad_auditoria AS ENUM ('PRODUCTO','OFERTA','PROVEEDOR','USUARIO','OTRO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE rol_eliminador AS ENUM ('PROVEEDOR','ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ============================================================
-- TABLA: usuarios
-- Maestra de todos los usuarios (ADMIN, PROVEEDOR, CLIENTE)
-- PRIORIDAD ALTA: login por correo, filtro por rol/estado
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
  id                    BIGSERIAL PRIMARY KEY,
  uuid                  UUID            NOT NULL UNIQUE DEFAULT gen_random_uuid(),
  nombre                VARCHAR(200)    NOT NULL,
  correo                VARCHAR(255)    NOT NULL UNIQUE,
  hash_contrasena       VARCHAR(255)    NOT NULL,
  rol                   rol_usuario     NOT NULL,
  estado                estado_usuario  NOT NULL DEFAULT 'ACTIVO',
  motivo_estado         TEXT            NULL,
  fecha_registro        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  ultimo_acceso         TIMESTAMPTZ     NULL,
  fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios (rol); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios (estado); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_usuarios_fecha_registro ON usuarios (fecha_registro); -- PRIORIDAD MEDIA

-- Trigger para actualizar fecha_actualizacion automáticamente
CREATE OR REPLACE FUNCTION actualizar_fecha_actualizacion() RETURNS TRIGGER AS $$
BEGIN NEW.fecha_actualizacion = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_usuarios_actualizacion ON usuarios;
CREATE TRIGGER trg_usuarios_actualizacion BEFORE UPDATE ON usuarios FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================
-- TABLA: perfiles_cliente (1-1 con usuarios)
-- ============================================================
CREATE TABLE IF NOT EXISTS perfiles_cliente (
  id                    BIGSERIAL PRIMARY KEY,
  usuario_id            BIGINT          NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE RESTRICT,
  nombre_tienda         VARCHAR(255)    NOT NULL,
  telefono              VARCHAR(30)     NULL,
  ciudad                VARCHAR(100)    NULL,
  direccion             VARCHAR(300)    NULL,
  fecha_creacion        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_perfil_cliente_ciudad ON perfiles_cliente (ciudad); -- PRIORIDAD MEDIA
DROP TRIGGER IF EXISTS trg_perfiles_cliente_actualizacion ON perfiles_cliente;
CREATE TRIGGER trg_perfiles_cliente_actualizacion BEFORE UPDATE ON perfiles_cliente FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================
-- TABLA: preferencias_cliente (1-1 con perfiles_cliente)
-- ============================================================
CREATE TABLE IF NOT EXISTS preferencias_cliente (
  id                    BIGSERIAL PRIMARY KEY,
  perfil_cliente_id     BIGINT          NOT NULL UNIQUE REFERENCES perfiles_cliente(id) ON DELETE CASCADE,
  fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
DROP TRIGGER IF EXISTS trg_preferencias_cliente_actualizacion ON preferencias_cliente;
CREATE TRIGGER trg_preferencias_cliente_actualizacion BEFORE UPDATE ON preferencias_cliente FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- Normalización de arreglos JSON previos:
CREATE TABLE IF NOT EXISTS preferencias_cliente_categorias (
  id                BIGSERIAL PRIMARY KEY,
  preferencia_id    BIGINT      NOT NULL REFERENCES preferencias_cliente(id) ON DELETE CASCADE,
  categoria_id      VARCHAR(50) NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
  UNIQUE (preferencia_id, categoria_id)
);
CREATE INDEX IF NOT EXISTS idx_pref_cat_categoria ON preferencias_cliente_categorias (categoria_id); -- PRIORIDAD ALTA

CREATE TABLE IF NOT EXISTS preferencias_cliente_modelos (
  id                BIGSERIAL PRIMARY KEY,
  preferencia_id    BIGINT              NOT NULL REFERENCES preferencias_cliente(id) ON DELETE CASCADE,
  modelo_comercial  modelo_comercial    NOT NULL,
  UNIQUE (preferencia_id, modelo_comercial)
);

CREATE TABLE IF NOT EXISTS preferencias_cliente_palabras (
  id                BIGSERIAL PRIMARY KEY,
  preferencia_id    BIGINT       NOT NULL REFERENCES preferencias_cliente(id) ON DELETE CASCADE,
  palabra_clave     VARCHAR(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pref_palabra ON preferencias_cliente_palabras (palabra_clave); -- PRIORIDAD ALTA

-- ============================================================
-- TABLA: perfiles_proveedor (1-1 con usuarios)
-- ============================================================
CREATE TABLE IF NOT EXISTS perfiles_proveedor (
  id                    BIGSERIAL PRIMARY KEY,
  usuario_id            BIGINT          NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE RESTRICT,
  razon_social          VARCHAR(255)    NOT NULL,
  nombre_comercial      VARCHAR(255)    NULL,
  identificacion_fiscal VARCHAR(50)     NULL,
  telefono_contacto     VARCHAR(50)     NULL,
  url_logo              VARCHAR(1000)   NULL,
  url_banner            VARCHAR(1000)   NULL,
  direccion             VARCHAR(300)    NULL,
  ciudad                VARCHAR(100)    NULL,
  anos_mercado           VARCHAR(30)     NULL,
  descripcion           TEXT            NULL,
  politica_comercial    TEXT            NULL,
  calificacion          NUMERIC(3,2)    NULL DEFAULT 0.00 CHECK (calificacion >= 0 AND calificacion <= 5),
  verificado            BOOLEAN         NOT NULL DEFAULT FALSE,
  destacado             BOOLEAN         NOT NULL DEFAULT FALSE,
  fecha_creacion        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_proveedor_destacado ON perfiles_proveedor (destacado); -- PRIORIDAD ALTA: Inicio
CREATE INDEX IF NOT EXISTS idx_proveedor_verificado ON perfiles_proveedor (verificado); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_proveedor_ciudad ON perfiles_proveedor (ciudad); -- PRIORIDAD MEDIA
DROP TRIGGER IF EXISTS trg_perfiles_proveedor_actualizacion ON perfiles_proveedor;
CREATE TRIGGER trg_perfiles_proveedor_actualizacion BEFORE UPDATE ON perfiles_proveedor FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- Normalización de marcas y especialidades (antes JSON):
CREATE TABLE IF NOT EXISTS proveedor_marcas (
  id            BIGSERIAL PRIMARY KEY,
  proveedor_id  BIGINT       NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE CASCADE,
  marca         VARCHAR(100) NOT NULL,
  UNIQUE (proveedor_id, marca)
);
CREATE INDEX IF NOT EXISTS idx_marca ON proveedor_marcas (marca); -- PRIORIDAD ALTA

CREATE TABLE IF NOT EXISTS proveedor_especialidades (
  id            BIGSERIAL PRIMARY KEY,
  proveedor_id  BIGINT       NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE CASCADE,
  especialidad  VARCHAR(150) NOT NULL,
  UNIQUE (proveedor_id, especialidad)
);

-- ============================================================
-- TABLA: solicitudes_proveedor
-- ============================================================
CREATE TABLE IF NOT EXISTS solicitudes_proveedor (
  id                    BIGSERIAL PRIMARY KEY,
  razon_social          VARCHAR(255)    NOT NULL,
  nombre_contacto       VARCHAR(200)    NOT NULL,
  correo                VARCHAR(255)    NOT NULL,
  telefono              VARCHAR(50)     NOT NULL,
  ciudad                VARCHAR(100)    NULL,
  direccion             VARCHAR(300)    NULL,
  marcas_texto          TEXT            NULL,
  mensaje               TEXT            NULL,
  estado                estado_solicitud NOT NULL DEFAULT 'PENDIENTE',
  motivo_rechazo        TEXT            NULL,
  admin_revisor_id      BIGINT          NULL REFERENCES usuarios(id) ON DELETE SET NULL,
  fecha_solicitud       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  fecha_revision        TIMESTAMPTZ     NULL,
  proveedor_creado_id   BIGINT          NULL REFERENCES perfiles_proveedor(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitudes_proveedor (estado); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_solicitud_correo ON solicitudes_proveedor (correo); -- PRIORIDAD MEDIA
CREATE INDEX IF NOT EXISTS idx_solicitud_ciudad ON solicitudes_proveedor (ciudad);

-- ============================================================
-- TABLA: categorias
-- ============================================================
CREATE TABLE IF NOT EXISTS categorias (
  id                    VARCHAR(50)   PRIMARY KEY,
  nombre                VARCHAR(100)  NOT NULL,
  icono                 VARCHAR(50)   NULL,
  descripcion           VARCHAR(300)  NULL,
  estado                estado_categoria NOT NULL DEFAULT 'ACTIVA',
  total_productos       INT           NOT NULL DEFAULT 0, -- Desnormalizado para dashboard (prioridad)
  fecha_creacion        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  fecha_actualizacion   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_categoria_estado ON categorias (estado);
DROP TRIGGER IF EXISTS trg_categorias_actualizacion ON categorias;
CREATE TRIGGER trg_categorias_actualizacion BEFORE UPDATE ON categorias FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================
-- TABLA: subcategorias
-- ============================================================
CREATE TABLE IF NOT EXISTS subcategorias (
  id            BIGSERIAL PRIMARY KEY,
  categoria_id  VARCHAR(50)   NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
  nombre        VARCHAR(100)  NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_subcat_categoria ON subcategorias (categoria_id);

-- ============================================================
-- TABLA: productos
-- Publicaciones B2B por volumen (sin stock)
-- ============================================================
CREATE TABLE IF NOT EXISTS productos (
  id                      BIGSERIAL PRIMARY KEY,
  uuid                    UUID              NOT NULL UNIQUE DEFAULT gen_random_uuid(),
  proveedor_id            BIGINT            NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE RESTRICT,
  categoria_id            VARCHAR(50)       NOT NULL REFERENCES categorias(id) ON DELETE RESTRICT,
  titulo                  VARCHAR(500)      NOT NULL,
  descripcion             TEXT              NOT NULL,
  modelo_comercial        modelo_comercial  NOT NULL,
  tipo_formato            VARCHAR(200)      NOT NULL,
  unidades_por_paquete    VARCHAR(100)      NULL,
  pedido_minimo           VARCHAR(200)      NULL,
  precio_unitario_ref     NUMERIC(10,2)     NOT NULL CHECK (precio_unitario_ref > 0),
  precio_total_ref        NUMERIC(12,2)     NULL,
  moneda                  VARCHAR(3)        NOT NULL DEFAULT 'USD',
  terminos_comerciales    TEXT              NULL,
  estado                  estado_producto   NOT NULL DEFAULT 'ACTIVO',
  motivo_moderacion       TEXT              NULL,
  es_recomendado          BOOLEAN           NOT NULL DEFAULT FALSE,
  es_promocionado         BOOLEAN           NOT NULL DEFAULT FALSE,
  tiene_oferta            BOOLEAN           NOT NULL DEFAULT FALSE,
  fecha_publicacion       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
  fecha_actualizacion     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
  fecha_eliminacion       TIMESTAMPTZ       NULL,
  eliminado_por           BIGINT            NULL REFERENCES usuarios(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_producto_proveedor ON productos (proveedor_id); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON productos (categoria_id); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_producto_estado ON productos (estado); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_producto_recomendado ON productos (es_recomendado); -- PRIORIDAD ALTA: Inicio
CREATE INDEX IF NOT EXISTS idx_producto_promocionado ON productos (es_promocionado); -- PRIORIDAD ALTA: Inicio
CREATE INDEX IF NOT EXISTS idx_producto_modelo ON productos (modelo_comercial); -- PRIORIDAD MEDIA
CREATE INDEX IF NOT EXISTS idx_producto_precio ON productos (precio_unitario_ref); -- PRIORIDAD MEDIA: filtro precio
-- GIN para búsqueda full-text en español (título + descripción)
CREATE INDEX IF NOT EXISTS idx_producto_busqueda ON productos USING GIN (to_tsvector('spanish', titulo || ' ' || descripcion));
DROP TRIGGER IF EXISTS trg_productos_actualizacion ON productos;
CREATE TRIGGER trg_productos_actualizacion BEFORE UPDATE ON productos FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- Normalización de especificaciones (antes JSON specs):
CREATE TABLE IF NOT EXISTS producto_especificaciones (
  id          BIGSERIAL PRIMARY KEY,
  producto_id BIGINT       NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
  clave       VARCHAR(100) NOT NULL,
  valor       VARCHAR(300) NOT NULL,
  UNIQUE (producto_id, clave)
);

-- ============================================================
-- TABLA: imagenes_producto
-- ============================================================
CREATE TABLE IF NOT EXISTS imagenes_producto (
  id          BIGSERIAL PRIMARY KEY,
  producto_id BIGINT        NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
  url_imagen  VARCHAR(1000) NOT NULL,
  orden       SMALLINT      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_imagen_producto ON imagenes_producto (producto_id);

-- ============================================================
-- TABLA: ofertas
-- ============================================================
CREATE TABLE IF NOT EXISTS ofertas (
  id                    BIGSERIAL PRIMARY KEY,
  proveedor_id          BIGINT        NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE RESTRICT,
  producto_id           BIGINT        NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
  titulo_oferta         VARCHAR(300)  NOT NULL,
  porcentaje_descuento  SMALLINT      NOT NULL CHECK (porcentaje_descuento BETWEEN 1 AND 99),
  precio_original       NUMERIC(10,2) NOT NULL,
  precio_promocional    NUMERIC(10,2) NOT NULL CHECK (precio_promocional > 0),
  moneda                VARCHAR(3)    NOT NULL DEFAULT 'USD',
  vigencia              VARCHAR(100)  NULL,
  terminos              TEXT          NULL,
  estado                estado_oferta NOT NULL DEFAULT 'ACTIVA',
  motivo_moderacion     TEXT          NULL,
  fecha_creacion        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  fecha_actualizacion   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  fecha_eliminacion     TIMESTAMPTZ   NULL,
  CONSTRAINT chk_precio_oferta CHECK (precio_promocional < precio_original)
);
CREATE INDEX IF NOT EXISTS idx_oferta_proveedor ON ofertas (proveedor_id); -- PRIORIDAD MEDIA
CREATE INDEX IF NOT EXISTS idx_oferta_producto ON ofertas (producto_id); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_oferta_estado ON ofertas (estado); -- PRIORIDAD ALTA
DROP TRIGGER IF EXISTS trg_ofertas_actualizacion ON ofertas;
CREATE TRIGGER trg_ofertas_actualizacion BEFORE UPDATE ON ofertas FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================
-- TABLA: planes_publicidad
-- ============================================================
CREATE TABLE IF NOT EXISTS planes_publicidad (
  id                  BIGSERIAL PRIMARY KEY,
  nombre              VARCHAR(200)  NOT NULL,
  dias_duracion       SMALLINT      NOT NULL CHECK (dias_duracion > 0),
  costo_usd           NUMERIC(10,2) NOT NULL,
  descripcion         TEXT          NULL,
  espacios_asignados  VARCHAR(300)  NULL,
  activo              BOOLEAN       NOT NULL DEFAULT TRUE,
  popular             BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_plan_activo ON planes_publicidad (activo);

-- ============================================================
-- TABLA: promociones
-- ============================================================
CREATE TABLE IF NOT EXISTS promociones (
  id                    BIGSERIAL PRIMARY KEY,
  proveedor_id          BIGINT        NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE RESTRICT,
  producto_id           BIGINT        NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
  plan_publicidad_id    BIGINT        NOT NULL REFERENCES planes_publicidad(id) ON DELETE RESTRICT,
  nombre_plan           VARCHAR(200)  NOT NULL,
  costo_usd             NUMERIC(10,2) NOT NULL,
  dias_duracion         SMALLINT      NOT NULL,
  espacios_asignados    VARCHAR(300)  NULL,
  fecha_inicio          TIMESTAMPTZ   NOT NULL,
  fecha_fin             TIMESTAMPTZ   NOT NULL CHECK (fecha_fin > fecha_inicio),
  estado                estado_promocion NOT NULL DEFAULT 'ACTIVA',
  nota_moderacion       TEXT          NULL,
  fecha_creacion        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  fecha_cancelacion     TIMESTAMPTZ   NULL
);
CREATE INDEX IF NOT EXISTS idx_promo_proveedor ON promociones (proveedor_id); -- PRIORIDAD MEDIA
CREATE INDEX IF NOT EXISTS idx_promo_producto ON promociones (producto_id); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_promo_estado ON promociones (estado); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_promo_fecha_fin ON promociones (fecha_fin); -- PRIORIDAD ALTA: expiración

-- ============================================================
-- TABLA: conversaciones (chat 1-1)
-- ============================================================
CREATE TABLE IF NOT EXISTS conversaciones (
  id                          BIGSERIAL PRIMARY KEY,
  uuid                        UUID          NOT NULL UNIQUE DEFAULT gen_random_uuid(),
  cliente_id                  BIGINT        NOT NULL REFERENCES perfiles_cliente(id) ON DELETE RESTRICT,
  proveedor_id                BIGINT        NOT NULL REFERENCES perfiles_proveedor(id) ON DELETE RESTRICT,
  producto_referenciado_id    BIGINT        NULL REFERENCES productos(id) ON DELETE SET NULL,
  fecha_ultimo_mensaje        TIMESTAMPTZ   NULL,
  vista_previa_ultimo         VARCHAR(500)  NULL,
  no_leidos_cliente           INT           NOT NULL DEFAULT 0,
  no_leidos_proveedor         INT           NOT NULL DEFAULT 0,
  fecha_creacion              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  UNIQUE (cliente_id, proveedor_id) -- PRIORIDAD ALTA: una sola conversación por par
);
CREATE INDEX IF NOT EXISTS idx_conv_proveedor ON conversaciones (proveedor_id);
CREATE INDEX IF NOT EXISTS idx_conv_ultimo_mensaje ON conversaciones (fecha_ultimo_mensaje DESC); -- PRIORIDAD ALTA

-- ============================================================
-- TABLA: mensajes
-- ============================================================
CREATE TABLE IF NOT EXISTS mensajes (
  id                      BIGSERIAL PRIMARY KEY,
  conversacion_id         BIGINT              NOT NULL REFERENCES conversaciones(id) ON DELETE CASCADE,
  usuario_remitente_id    BIGINT              NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
  rol_remitente           rol_remitente       NOT NULL,
  texto                   TEXT                NOT NULL,
  leido                   BOOLEAN             NOT NULL DEFAULT FALSE,
  fecha_envio             TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_mensaje_conversacion ON mensajes (conversacion_id); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_mensaje_fecha ON mensajes (fecha_envio); -- PRIORIDAD ALTA
CREATE INDEX IF NOT EXISTS idx_mensaje_leido ON mensajes (leido); -- PRIORIDAD MEDIA

-- ============================================================
-- TABLA: contenido_plataforma
-- ============================================================
CREATE TABLE IF NOT EXISTS contenido_plataforma (
  id                      BIGSERIAL PRIMARY KEY,
  clave_contenido         VARCHAR(50)   NOT NULL UNIQUE, -- 'nosotros' | 'contacto'
  contenido_json          JSONB         NOT NULL,
  fecha_actualizacion     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  admin_actualizador_id   BIGINT        NULL REFERENCES usuarios(id) ON DELETE SET NULL
);
DROP TRIGGER IF EXISTS trg_contenido_actualizacion ON contenido_plataforma;
CREATE TRIGGER trg_contenido_actualizacion BEFORE UPDATE ON contenido_plataforma FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================
-- TABLA: registros_auditoria
-- Historial inmutable de eliminaciones lógicas
-- ============================================================
CREATE TABLE IF NOT EXISTS registros_auditoria (
  id                      BIGSERIAL PRIMARY KEY,
  tipo_entidad            tipo_entidad_auditoria NOT NULL,
  id_entidad_original     BIGINT        NOT NULL,
  titulo_entidad          VARCHAR(500)  NOT NULL,
  usuario_eliminador_id   BIGINT        NULL REFERENCES usuarios(id) ON DELETE SET NULL,
  rol_eliminador          rol_eliminador NOT NULL,
  fecha_eliminacion       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  motivo                  TEXT          NULL,
  datos_snapshot          JSONB         NOT NULL,
  estado                  VARCHAR(50)   NOT NULL DEFAULT 'ARCHIVADO'
);
CREATE INDEX IF NOT EXISTS idx_auditoria_tipo ON registros_auditoria (tipo_entidad); -- PRIORIDAD MEDIA
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON registros_auditoria (fecha_eliminacion); -- PRIORIDAD BAJA
CREATE INDEX IF NOT EXISTS idx_auditoria_rol ON registros_auditoria (rol_eliminador);

-- ============================================================
-- DATOS INICIALES (Seed)
-- ============================================================

INSERT INTO categorias (id, nombre, icono, descripcion, estado) VALUES
('ram',         'Memorias RAM',            'Cpu',             'Módulos DDR4, DDR5, SODIMM para PC y Laptops', 'ACTIVA'),
('ssd',         'Discos SSD & NVMe',       'HardDrive',       'Unidades sólidas M.2 NVMe PCIe 4.0/5.0 y SATA III', 'ACTIVA'),
('gpu',         'Tarjetas Gráficas (GPU)', 'Layers',          'Tarjetas de video GeForce RTX y AMD Radeon', 'ACTIVA'),
('cpu',         'Procesadores',            'Zap',             'Procesadores Intel Core 13ª/14ª Gen y AMD Ryzen 7000/8000', 'ACTIVA'),
('motherboard', 'Placas Madre',            'Grid',            'Mainboards chipsets Z790, B760, X670, B650', 'ACTIVA'),
('hdd',         'Discos Duros (HDD)',      'Database',        'Discos mecánicos para almacenamiento masivo y NAS', 'ACTIVA'),
('psu',         'Fuentes de Poder',        'BatteryCharging', 'Fuentes 80 Plus Bronze, Gold y Platinum ATX 3.0', 'ACTIVA'),
('usb',         'Memorias USB & Flash',    'Usb',             'Pen drives USB 3.2 y tarjetas MicroSD por paquete máster', 'ACTIVA')
ON CONFLICT (id) DO NOTHING;

INSERT INTO subcategorias (categoria_id, nombre) VALUES
('ram', 'DDR4'), ('ram', 'DDR5'), ('ram', 'SODIMM Laptop'), ('ram', 'RAM Servidor'),
('ssd', 'M.2 PCIe 4.0'), ('ssd', 'M.2 PCIe 5.0'), ('ssd', 'SATA III 2.5"'), ('ssd', 'SSD Externo'),
('gpu', 'GeForce RTX 4000'), ('gpu', 'Radeon RX 7000'), ('gpu', 'Workstation Pro'),
('cpu', 'Intel Core 14ª Gen'), ('cpu', 'Intel Core 13ª Gen'), ('cpu', 'AMD Ryzen AM5'), ('cpu', 'AMD Ryzen AM4'),
('motherboard', 'Chipset Z790/B760'), ('motherboard', 'Chipset X670/B650'), ('motherboard', 'Micro-ATX'), ('motherboard', 'Mini-ITX'),
('hdd', 'HDD Surveillance 24/7'), ('hdd', 'HDD NAS Enterprise'), ('hdd', 'HDD Desktop 3.5"'),
('psu', '80+ Bronze'), ('psu', '80+ Gold Modular'), ('psu', '80+ Platinum ATX 3.0'),
('usb', 'USB 3.2 Gen 1'), ('usb', 'USB Tipo-C'), ('usb', 'Tarjetas MicroSD Clase 10')
ON CONFLICT DO NOTHING;

INSERT INTO planes_publicidad (nombre, dias_duracion, costo_usd, descripcion, espacios_asignados, activo, popular) VALUES
('Plan Impulso Básico',     7,  49.00, 'Mayor frecuencia en búsquedas y feed orgánico.', 'Búsqueda de Categoría + Rotación en Inicio', true, false),
('Plan Pro Mayorista',     15,  89.00, 'Prioridad alta en Bloque Promocionados en Inicio.', 'Bloque Promocionados en Inicio + Prioridad Máxima en Búsqueda', true, true),
('Plan Expansión Premium', 30, 149.00, 'Exposición continua 30 días para lanzamientos.', 'Bloque Promocionados en Inicio + Destacado en Categoría', true, false)
ON CONFLICT DO NOTHING;

INSERT INTO contenido_plataforma (clave_contenido, contenido_json) VALUES
('nosotros', jsonb_build_object(
  'titulo', 'El Ecosistema Mayorista Especializado en Hardware & Componentes de Computación',
  'subtitulo', 'Conectamos de forma ágil y directa a compradores de tiendas con importadores mayoristas.',
  'mision', 'Digitalizar y transparentar el comercio mayorista de hardware informático.',
  'pilares', jsonb_build_array(
    jsonb_build_object('titulo', 'Especialización Total', 'descripcion', 'Enfocados exclusivamente en componentes para PC.'),
    jsonb_build_object('titulo', 'Proveedores Verificados', 'descripcion', 'Cada empresa pasa por evaluación y verificación comercial manual sin requerir RUC sensible.'),
    jsonb_build_object('titulo', 'Trato Directo B2B', 'descripcion', 'Sin intermediarios. Acuerdos 1 a 1 en chat privado.')
  )
)),
('contacto', jsonb_build_object(
  'correo_soporte', 'soporte@compunexb2b.com',
  'correo_proveedores', 'proveedores@compunexb2b.com',
  'telefono_central', '+51 (01) 700-4500',
  'whatsapp_soporte', '+51 999 888 777',
  'direccion_oficina', 'Torre Empresarial Tecnológica, Piso 14, San Isidro, Lima, Perú',
  'horario_atencion', 'Lunes a Viernes: 8:30 AM - 6:30 PM | Sábados: 9:00 AM - 1:00 PM'
))
ON CONFLICT (clave_contenido) DO NOTHING;

-- Usuario Administrador inicial (hash BCrypt real debe generarse antes del deploy)
INSERT INTO usuarios (uuid, nombre, correo, hash_contrasena, rol, estado) VALUES
(gen_random_uuid(), 'Super Administrador', 'admin@compunexb2b.com', '$2a$12$PLACEHOLDER_BCRYPT_HASH_HERE', 'ADMIN', 'ACTIVO')
ON CONFLICT (correo) DO NOTHING;

-- ============================================================
-- RESUMEN DE RELACIONES NORMALIZADAS 3FN (PostgreSQL)
-- ============================================================
-- usuarios 1--1 perfiles_cliente
-- usuarios 1--1 perfiles_proveedor
-- perfiles_cliente 1--1 preferencias_cliente
-- preferencias_cliente 1--N preferencias_cliente_categorias / modelos / palabras
-- perfiles_proveedor 1--N proveedor_marcas / proveedor_especialidades
-- perfiles_proveedor 1--N productos / ofertas / promociones / conversaciones
-- solicitudes_proveedor N--1 perfiles_proveedor (proveedor_creado_id, nullable)
-- categorias 1--N productos + 1--N subcategorias
-- productos 1--N imagenes_producto + 1--N producto_especificaciones + 1--N ofertas + 1--N promociones
-- conversaciones 1--N mensajes
-- usuarios 1--N registros_auditoria / contenido_plataforma
--
-- ÍNDICES DE PRIORIDAD:
--   ALTA: correos, estados, categorías, precios, búsqueda GIN español, bandeja de chat
--   MEDIA: ciudades, fechas
--   BAJA: auditoría
-- ============================================================
-- FIN DEL SCRIPT POSTGRESQL
-- ============================================================
