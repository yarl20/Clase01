# 02. Estrategia de Seguridad, JWT y Control de Acceso (RBAC)

> Plataforma COMPUNEX B2B — Arquitectura de Seguridad Spring Boot 3 + PostgreSQL + JWT

---

## 1. Arquitectura de Autenticación y Tokens

### Mecanismo de Autenticación
- **Estándar:** JSON Web Token (JWT) firmado con algoritmo HMAC-SHA256 (`HS256`) o RSA-256 (`RS256`).
- **Transporte:** Cabecera HTTP estándar:
  ```http
  Authorization: Bearer <TOKEN_JWT>
  ```
- **Duración de Access Token:** 24 horas (recomendado para entorno B2B web).
- **Almacenamiento en Cliente (React):** Memoria de aplicación (React Context) con respaldo en `sessionStorage` o cookie `HttpOnly` para prevenir ataques XSS.

### Estructura de Claims del JWT
```json
{
  "sub": "compras@tecnostore.com",
  "userId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "perfilId": "f7e8d9c0-b1a2-3c4d-5e6f-7a8b9c0d1e2f",
  "role": "CLIENTE",
  "status": "ACTIVO",
  "iat": 1772640000,
  "exp": 1772726400
}
```

### Hash de Contraseñas
- **Algoritmo:** BCrypt con factor de costo (work factor) 12.
- Se ejecuta en el registro (`POST /api/auth/register`) y en el cambio de credenciales. Las contraseñas en texto plano nunca se registran en logs ni se persisten en base de datos.

---

## 2. Matriz de Roles y Permisos (RBAC)

El sistema opera con 3 roles formales definidos en el enum PostgreSQL `rol_usuario`:

| Módulo / Recurso | Público (Sin Auth) | CLIENTE | PROVEEDOR | ADMIN |
|---|:---:|:---:|:---:|:---:|
| **Registro de Clientes** (`POST /api/auth/register`) | ✅ | ❌ | ❌ | ❌ |
| **Login y Refresh Token** (`/api/auth/*`) | ✅ | ✅ | ✅ | ✅ |
| **Catálogo Público de Productos** (`GET /api/productos`) | ✅ | ✅ | ✅ | ✅ |
| **Detalle de Producto** (`GET /api/productos/{id}`) | ✅ | ✅ | ✅ | ✅ |
| **Perfiles Públicos Proveedores** (`GET /api/proveedores/{id}`) | ✅ | ✅ | ✅ | ✅ |
| **Planes de Publicidad** (`GET /api/planes-publicidad`) | ✅ | ✅ | ✅ | ✅ |
| **Gestión de Mi Perfil Cliente** (`/api/clientes/yo`) | ❌ | ✅ | ❌ | ❌ |
| **Chat con Proveedores** (`/api/chat/**`) | ❌ | ✅ | ✅ | ❌ (auditoría) |
| **Gestión de Mis Productos** (`/api/proveedores/yo/productos/**`) | ❌ | ❌ | ✅ | ❌ |
| **Creación/Edición de Mis Ofertas** (`/api/proveedores/yo/ofertas/**`) | ❌ | ❌ | ✅ | ❌ |
| **Contratación de Publicidad** (`/api/proveedores/yo/promociones/**`) | ❌ | ❌ | ✅ | ❌ |
| **Dashboard y Métricas Globales** (`/api/admin/dashboard/**`) | ❌ | ❌ | ❌ | ✅ |
| **Moderación de Productos** (`/api/admin/productos/**`) | ❌ | ❌ | ❌ | ✅ |
| **Gestión y Bloqueo de Usuarios** (`/api/admin/usuarios/**`) | ❌ | ❌ | ❌ | ✅ |
| **Historial y Auditoría Global** (`/api/admin/auditoria/**`) | ❌ | ❌ | ❌ | ✅ |

---

## 3. Configuración de Spring Security 6 (Java 17 / 21)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/proveedores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/planes-publicidad/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Endpoints Exclusivos por Rol
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/proveedores/yo/**").hasRole("PROVEEDOR")
                .requestMatchers("/api/v1/clientes/yo/**").hasRole("CLIENTE")
                .requestMatchers("/api/v1/chat/**").hasAnyRole("CLIENTE", "PROVEEDOR")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 4. Políticas de Protección Contra Amenazas (OWASP)

1. **Inyección SQL:** 
   - 100% mitigada mediante Spring Data JPA y parámetros nombrados en `@Query`. 
   - Prohibida la concatenación de cadenas en sentencias SQL dinámicas.
2. **Control de Acceso Horizontal (IDOR):**
   - Un usuario proveedor solo puede modificar sus propios productos. El backend extrae el `proveedor_id` del token JWT autenticado, ignorando cualquier ID que el frontend envíe en el body para evitar suplantación:
     ```java
     Long proveedorId = authUser.getPerfilProveedorId();
     productoRepository.findByIdAndProveedorId(productoId, proveedorId)
         .orElseThrow(() -> new AccessDeniedException("No autorizado para modificar este producto"));
     ```
3. **CORS Restringido:**
   - Orígenes permitidos configurados explícitamente en el API Gateway / Backend:
     `http://localhost:3000` (Cliente), `http://localhost:3001` (Proveedor), `http://localhost:3002` (Admin).
4. **Protección de Auditoría:**
   - Toda acción destructiva o de moderación registra automáticamente un snapshot en la tabla `registros_auditoria` con el `usuario_id`, IP del cliente y datos anteriores en formato JSONB.

