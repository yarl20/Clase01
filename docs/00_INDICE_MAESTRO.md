# COMPUNEX B2B — Índice Maestro de Arquitectura y Documentación Técnica

> Plataforma B2B de Proveedores y Componentes de Computación (RAM, SSD, GPU, etc.)
> Arquitectura Multi-Tier: Frontend SPA (React) ➔ Backend Orquestador (Spring Boot) ➔ Persistencia (PostgreSQL 15+)

---

## 🏛️ Estructura del Sistema y Mapa de Navegación

El proyecto está organizado de forma modular siguiendo los principios de la arquitectura Contract-First y separación de responsabilidades:

```text
apis_y_db/
├── 00_INDICE_MAESTRO.md                     # Este mapa central de navegación
├── 01_analisis_frontend/                    # Análisis de UI y Data Journeys por rol
├── 02_contratos_api/                        # Especificaciones de endpoints (preparadas para consolidación)
├── 03_base_datos/                           # Diccionario de datos, modelo ER y DDL SQL
├── 04_arquitectura_y_reglas/                # Reglas de negocio, seguridad RBAC y arquitectura Spring Boot
└── assets/diagramas/                        # Diagramas visuales (secuencia, casos de uso, entidad-relación)
```

---

## 📚 Índice de Módulos y Documentos

### 1. Módulo de Análisis Frontend y Flujo de Datos (`01_analisis_frontend/`)
Contiene el desglose exhaustivo de las pantallas de las 3 demos frontend (Cliente, Proveedor y Administrador), componentes visuales y flujo de interacción.
- 📄 [01_Analisis_Frontend_y_Flujo_de_Datos.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/01_analisis_frontend/01_Analisis_Frontend_y_Flujo_de_Datos.md) — Análisis detallado por rol (A: Cliente, B: Proveedor, C: Administrador), vistas, widgets y datos requeridos.

---

### 2. Módulo de Contratos de API (`02_contratos_api/`)
Endpoints documentados para la plataforma. Clasificados en dos subdirectorios para facilitar su posterior unificación en el contrato oficial trilateral:

#### A. Especificaciones de API por Rol (`01_apis_por_rol/`):
- 📄 [05_API_AUTENTICACION.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/01_apis_por_rol/05_API_AUTENTICACION.md) — Registro, login, refresh token y sesiones.
- 📄 [06_API_CLIENTE.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/01_apis_por_rol/06_API_CLIENTE.md) — Endpoints consumidos por la SPA del Cliente (Home, catálogo, detalle, contacto).
- 📄 [07_API_PROVEEDOR.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/01_apis_por_rol/07_API_PROVEEDOR.md) — Endpoints de la SPA del Proveedor (Perfil, catálogo propio, campañas).
- 📄 [08_API_ADMINISTRADOR.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/01_apis_por_rol/08_API_ADMINISTRADOR.md) — Endpoints del panel administrativo (KPIs, moderación, auditoría).

#### B. Especificaciones de API por Recurso / Dominio (`02_apis_por_recurso/`):
- 📄 [09_API_PRODUCTOS.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/09_API_PRODUCTOS.md) — CRUD de productos, estados, filtros multicriterio y especificaciones.
- 📄 [10_API_OFERTAS.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/10_API_OFERTAS.md) — Ofertas flash, descuentos y vigencia temporal.
- 📄 [11_API_CHAT.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/11_API_CHAT.md) — Negociación B2B 1-a-1 entre Cliente y Proveedor con producto referenciado.
- 📄 [12_API_PUBLICIDAD.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/12_API_PUBLICIDAD.md) — Planes de publicidad (7, 15, 30 días) y contratación de campañas.
- 📄 [13_API_RECOMENDACIONES.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/13_API_RECOMENDACIONES.md) — Bloques destacados para la página de inicio.
- 📄 [14_API_CATEGORIAS.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/14_API_CATEGORIAS.md) — Categorías oficiales de hardware (RAM, SSD, GPU, etc.).
- 📄 [15_API_PROVEEDORES.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/15_API_PROVEEDORES.md) — Perfil público corporativo y catálogo de marca.
- 📄 [16_API_USUARIOS.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/16_API_USUARIOS.md) — Administración de cuentas, bloqueos y estados.
- 📄 [17_API_HISTORIAL.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/02_contratos_api/02_apis_por_recurso/17_API_HISTORIAL.md) — Registros de eliminaciones lógicas y auditoría.

---

### 3. Módulo de Base de Datos y Persistencia (`03_base_datos/`)
Diseño relacional normalizado en Tercera Forma Normal (3FN), scripts DDL y visor gráfico.
- 📄 [01_DICCIONARIO_DE_DATOS.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/03_base_datos/01_DICCIONARIO_DE_DATOS.md) — Catálogo de tablas, tipos de datos, restricciones e índices.
- 📄 [02_MODELO_RELACIONAL.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/03_base_datos/02_MODELO_RELACIONAL.md) — Modelo relacional textual y cardinalidades.
- 📄 [03_SCHEMA_COMPLETO.sql](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/03_base_datos/03_SCHEMA_COMPLETO.sql) — Script SQL completo DDL para PostgreSQL 15+ con enums, tablas, triggers y datos semilla.
- 🌐 [visor_diagrama_relacional.html](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/03_base_datos/visor_diagrama_relacional.html) — Visor web interactivo con soporte de Zoom/Pan del diagrama ER en Mermaid.js.

---

### 4. Módulo de Arquitectura y Reglas de Negocio (`04_arquitectura_y_reglas/`)
Gobernanza del sistema, lógica de negocio y arquitectura backend.
- 📄 [01_REGLAS_DE_NEGOCIO.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/04_arquitectura_y_reglas/01_REGLAS_DE_NEGOCIO.md) — 22 reglas de negocio formales (RN-01 a RN-22: Rubro tecnológico exclusivo, ausencia de control de stock, modelos de volumen, moneda USD única, ausencia de pasarela de pagos, etc.).
- 📄 [02_SEGURIDAD_Y_RBAC.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/04_arquitectura_y_reglas/02_SEGURIDAD_Y_RBAC.md) — Estrategia de seguridad, arquitectura JWT, matriz RBAC y configuración de Spring Security 6.
- 📄 [03_ARQUITECTURA_SPRING_BOOT.md](file:///c:/Users/Usuario/Downloads/apis_y_db/apis_y_db/04_arquitectura_y_reglas/03_ARQUITECTURA_SPRING_BOOT.md) — Estructura de paquetes de Java, capas monolíticas modulares y tratamiento global de excepciones.

---

### 5. Recursos Visuales (`assets/diagramas/`)
Diagramas de soporte arquitectónico:
- `contacto_v_empresa.png`
- `iniciosecion.png`
- `mode.png`
- `secuencia.png`
- `uso.png`

---

## 🚀 Próxima Etapa Técnica
Una vez ordenada la estructura física, el siguiente paso según la skill `trilateral_api_architecture` es generar:
* **`02_Contrato_Oficial_de_APIs_Trilateral.md`**: Documento canónico consolidado que fusionará los 13 archivos de APIs bajo la tríada oficial:
  1. 💻 **Contrato Frontend** (rutas, endpoints, payloads JSON exactos).
  2. ☕ **Contrato Backend Orquestador** (Spring Boot Controllers, DTOs, validaciones).
  3. 🗄️ **Persistencia en Base de Datos** (queries, auditoría y transaccionalidad).

