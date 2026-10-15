# 🖥️ COMPUNEX B2B — Backend Orquestador (Primer Avance)

> **Curso:** Desarrollo Web Integrado  
> **Proyecto:** COMPUNEX B2B — Plataforma Mayorista de Hardware  
> **IDE Recomendado:** **Apache NetBeans** (o IntelliJ IDEA / VS Code)  
> **Java:** **JDK 21** | **Base de Datos:** PostgreSQL  
> **Tablero Oficial de Trello:** [Tablero COMPUNEX B2B en Trello](https://trello.com/b/qpK2u3wg/compunex-b2b-m%C3%B3dulo-proveedor-cat%C3%A1logo)

---

## 📖 0. Documento Oficial Obligatorio (Contrato de APIs)

Antes de programar o mover tarjetas en Trello, **cada desarrollador debe guiarse obligatoriamente por el contrato oficial de APIs**:

📂 **Ruta del documento:**  
`docs/02_contratos_api/01_apis_por_rol/001-Apis-Formalizado-En-desarrollo/07_API_PROVEEDOR.md`

📄 **Archivo:** [07_API_PROVEEDOR.md](docs/02_contratos_api/01_apis_por_rol/001-Apis-Formalizado-En-desarrollo/07_API_PROVEEDOR.md)

📑 **Resumen de Cambios y Guía de Pruebas en Postman (Sesiones 6 a 9):**  
Ver el documento: [06_DOCUMENTACION_CAMBIOS_Y_PRUEBAS_POSTMAN.md](docs/06_DOCUMENTACION_CAMBIOS_Y_PRUEBAS_POSTMAN.md)

> ⚠️ **IMPORTANTE:** En este documento encontrarás la especificación canónica exacta de los endpoints que te tocan en Trello:
> - Nombres exactos de **Controllers**, **DTOs** (`Request` y `Response`), **Services** y **Repositories**.
> - Nombres de los métodos de prueba en JUnit para cada fase de **TDD (RED, GREEN, REFACTOR)**.
> - **JSON de Entrada y JSON de Salida** con sus códigos HTTP (`200 OK`, `201 Created`, `204 No Content`).
> - Parámetros de consulta y cabeceras (`Authorization: Bearer {{token}}`).

---

## ⚙️ 1. Requisitos Previos

Antes de abrir el proyecto, asegúrate de tener:
1. **JDK 21 (LTS) instalado:** Obligatorio para compilar (no compilará con Java 17 u 11).
2. **Motor de PostgreSQL:** En ejecución en tu máquina (puerto por defecto `5432`).
3. **IDE:** De preferencia **Apache NetBeans** (también puedes usar IntelliJ IDEA o VS Code).

---

## 🗄️ 2. Paso Previo: Base de Datos en PostgreSQL

1. Abre tu **pgAdmin**, **DBeaver** o consola de PostgreSQL.
2. Crea una base de datos vacía con el nombre exacto:
   ```text
   compunex_b2b
   ```
3. Abre el archivo `src/main/resources/application.properties` en el proyecto y verifica tu usuario y contraseña de postgres:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/compunex_b2b
   spring.datasource.username=postgres
   spring.datasource.password=TU_PASSWORD_AQUI
   ```
   *(Si tu postgres no tiene contraseña, déjalo vacío `spring.datasource.password=`)*

> 💡 **Nota:** No necesitas crear tablas ni scripts SQL manuales. Las 7 tablas se crearán solas la primera vez que inicies la aplicación gracias a JPA/Hibernate (`ddl-auto=update`).

---

## 🚀 3. ¿Cómo Inicializar y Ejecutar el Proyecto?

### Desde Apache NetBeans (Recomendado):
1. Abre NetBeans y dale a **Open Project** (`Ctrl + Shift + O`).
2. Selecciona la carpeta `compunex-backend`.
3. Navega en el árbol de paquetes a:  
   `Source Packages` ➔ `com.compunex.b2b` ➔ **`CompunexBackendApplication.java`**.
4. Haz **clic derecho** sobre `CompunexBackendApplication.java` y selecciona **Run File** (o presiona `Shift + F6`).
5. ¡Y ya está! El servidor iniciará y estará escuchando en **`http://localhost:8080`**.



## 🧪 4. ¿Cómo Correr las Pruebas TDD para el Informe Word?

Para la rúbrica 20/20 del informe, cada endpoint debe tener sus evidencias de **TDD**:

```
🔴 Fase 1: RED      ➔ Test que falla en rojo (Captura para el Word)
       ↓
🟢 Fase 2: GREEN    ➔ Código mínimo que pasa a verde (Captura para el Word)
       ↓
🔵 Fase 3: REFACTOR ➔ Pruebas de integración @WebMvcTest en verde (Captura para el Word)
       ↓
🚀 Fase 4: Postman  ➔ Petición con Status 200, 201 o 204 y JSON (Captura para el Word)
```

### Para correr un test en NetBeans:
* Haz **clic derecho** sobre la clase de prueba que te corresponde (ejemplo `AuthServiceTest.java`, `ProductoServiceTest.java`) y selecciona **Test File** (o presiona `Ctrl + F6`).
* Las pruebas usan base de datos en memoria **H2**, por lo que se ejecutan al instante sin afectar tu PostgreSQL.

---

## 👥 5. Reparto Oficial de Endpoints (Trello)

El tablero de Trello ya tiene asignadas las tarjetas y los JSONs de prueba para cada integrante:  
🔗 **[Ver Tablero COMPUNEX B2B en Trello](https://trello.com/b/qpK2u3wg/compunex-b2b-m%C3%B3dulo-proveedor-cat%C3%A1logo)**

| Integrante | Rol | Endpoints Asignados | Sección en `07_API_PROVEEDOR.md` |
|---|---|---|---|
| **Sivipaucar Quispe, Edison Edgar** | *Security Core Lead* | • `[0.1] POST /api/v1/auth/login` (Login JWT) *(Carga reducida)* | [0.1] Sección 0 |
| **Saravia Humana, José Manuel Elias** | *Catalog Queries Lead* | • `[2.1] GET /api/v1/providers/me/products` (Paginación y Filtros) *(Carga reducida)* | [2.1] Sección 2 |
| **Rosales Miranda, Owen Sebastian** | *Master Data Architect* | • `[1.1] GET /api/v1/categories`<br>• `[1.2] GET /api/v1/categories/{id}`<br>• *Transversal:* `DataInitializer.java` (Seeds) | [1.1] y [1.2] Sección 1 |
| **Rodríguez Lapa, Yussef Amir** | *Product Mutation Specialist* | • `[2.3] POST /api/v1/providers/me/products` (Crear en Cascada)<br>• `[2.4] PUT /api/v1/providers/me/products/{id}` (Actualizar) | [2.3] y [2.4] Sección 2 |
| **Rodríguez Guevara, Eudes** | *Tech Specs & Detail Specialist* | • `[2.2] GET /api/v1/providers/me/products/{id}` (Ficha Técnica)<br>• `[2.5] PATCH /api/v1/providers/me/products/{id}/status` (Activar/Desactivar) | [2.2] y [2.5] Sección 2 |
| **Guadalupe Jamile, Yarleque Valladolid** | *Session & Lifecycle Specialist* | • `[0.2] GET /api/v1/auth/me` (Rehidratar Sesión)<br>• `[0.3] PATCH /api/v1/auth/password` (Cambio Clave)<br>• `[2.6] DELETE /api/v1/providers/me/products/{id}` (Soft Delete 204) | [0.2], [0.3] y [2.6] Secciones 0 y 2 |

---

## 🛠️ 6. Solución Rápida a Problemas Frecuentes

1. **Error de versión de Java (`UnsupportedClassVersionError`):**  
   Asegúrate de que en NetBeans el proyecto esté configurado con **JDK 21** (`Project Properties` ➔ `Libraries` ➔ `Java Platform` = JDK 21).
2. **Error de conexión a PostgreSQL (`Connection refused`):**  
   Verifica que el servicio de PostgreSQL esté iniciado en tu computadora.
3. **Error de contraseña (`password authentication failed`):**  
   Cambia la contraseña en `src/main/resources/application.properties` por la tuya.
4. **Puerto 8080 ocupado (`Port 8080 was already in use`):**  
   Cierra cualquier otro proyecto que esté corriendo o cambia a `server.port=8081` en `application.properties`.

## Agrega nuevo producto en SQL

ROLLBACK;
DO $$
DECLARE
    v_proveedor_id BIGINT;
    v_producto_id BIGINT;
BEGIN

    -- Buscar proveedor por correo
    SELECT pp.id
    INTO v_proveedor_id
    FROM perfiles_proveedor pp
    INNER JOIN usuarios u ON pp.usuario_id = u.id
    WHERE u.correo = 'ventas.mayoristas@technova.com';

    IF v_proveedor_id IS NULL THEN
        RAISE EXCEPTION 'No existe el proveedor ventas.mayoristas@technova.com';
    END IF;


    -- Insertar producto
    INSERT INTO productos (
        uuid,
        proveedor_id,
        categoria_id,
        titulo,
        descripcion,
        modelo_comercial,
        tipo_formato,
        unidades_por_paquete,
        pedido_minimo,
        precio_unitario_ref,
        precio_total_ref,
        moneda,
        terminos_comerciales,
        estado,
        es_recomendado,
        es_promocionado,
        tiene_oferta,
        fecha_publicacion,
        fecha_actualizacion
    )
    VALUES (
        gen_random_uuid(),
        v_proveedor_id,
        'ram',
        'Memoria RAM Corsair Vengeance RGB 32GB DDR5 6000MHz (2x16GB)',
        'Kit dual channel DDR5 de alto rendimiento compuesto por dos módulos de 16 GB. Compatible con Intel XMP 3.0 y diseñado para equipos empresariales, estaciones de trabajo y sistemas de alto rendimiento.',
        'DISTRIBUIDOR_OFICIAL',
        'Kit Dual x2 Módulos',
        '2 unidades',
        '1 kit (2 unidades)',
        48.00,
        96.00,
        'USD',
        'Garantía de 3 años directa con fabricante. Entrega en almacén Lima o envío a provincia con flete por pagar.',
        'ACTIVO',
        FALSE,
        TRUE,
        TRUE,
        NOW(),
        NOW()
    )
    RETURNING id INTO v_producto_id;


    -- Especificaciones
    INSERT INTO producto_especificaciones (
        producto_id,
        clave,
        valor
    )
    VALUES
        (v_producto_id, 'Capacidad', '32 GB (2x16)'),
        (v_producto_id, 'Frecuencia', '6000 MHz'),
        (v_producto_id, 'Latencia CAS', 'CL36'),
        (v_producto_id, 'Voltaje', '1.35V');


    -- Imágenes
    INSERT INTO imagenes_producto (
        producto_id,
        url_imagen,
        orden
    )
    VALUES
        (
            v_producto_id,
            'https://img.compunex.com/products/ram-corsair-32gb-front.jpg',
            1
        ),
        (
            v_producto_id,
            'https://img.compunex.com/products/ram-corsair-32gb-angle.jpg',
            2
        );


    RAISE NOTICE 'Producto creado correctamente. ID: %', v_producto_id;

END $$;