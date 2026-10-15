package com.compunex.b2b.modules.catalogo.service;

import com.compunex.b2b.modules.auth.entity.EstadoUsuario;
import com.compunex.b2b.modules.auth.entity.RolUsuario;
import com.compunex.b2b.modules.auth.entity.Usuario;
import com.compunex.b2b.modules.auth.repository.UsuarioRepository;
import com.compunex.b2b.modules.catalogo.dto.request.ActualizarProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CrearProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.entity.Categoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoCategoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoProducto;
import com.compunex.b2b.modules.catalogo.entity.ImagenProducto;
import com.compunex.b2b.modules.catalogo.entity.ModeloComercial;
import com.compunex.b2b.modules.catalogo.entity.Producto;
import com.compunex.b2b.modules.catalogo.entity.ProductoEspecificacion;
import com.compunex.b2b.modules.catalogo.repository.CategoriaRepository;
import com.compunex.b2b.modules.catalogo.repository.ProductoRepository;
import com.compunex.b2b.modules.catalogo.service.impl.ProductoServiceImpl;
import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import com.compunex.b2b.modules.perfiles.repository.PerfilProveedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilProveedorRepository perfilProveedorRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    @DisplayName("[2.6] debeEjecutarEliminacionLogicaSinBorrarRegistroFisico")
    void debeEjecutarEliminacionLogicaSinBorrarRegistroFisico() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);
        var producto = new Producto();
        producto.setId(5L);
        producto.setEstado(EstadoProducto.ACTIVO);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(5L, 10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        productoService.eliminarMiProducto(correo, 5L);

        assertEquals(EstadoProducto.ELIMINADO_LOGICO, producto.getEstado());
        assertNotNull(producto.getFechaEliminacion());
        verify(productoRepository, times(1)).save(producto);
        verify(productoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("[2.6] debeLanzar404CuandoProductoNoEsDelProveedor")
    void debeLanzar404CuandoProductoNoEsDelProveedor() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productoService.eliminarMiProducto(correo, 99L));
        verify(productoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("[2.2] debeRetornarFichaTecnicaCompletaConEspecificacionesEImagenes")
    void debeRetornarFichaTecnicaCompletaConEspecificacionesEImagenes() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);

        var perfil = new PerfilProveedor();
        perfil.setId(10L);
        perfil.setRazonSocial("TechNova Mayorista S.A.C.");

        var categoria = new Categoria("ram", "Memorias RAM", null, null, EstadoCategoria.ACTIVA, 0);

        var producto = new Producto();
        producto.setId(5L);
        producto.setProveedor(perfil);
        producto.setCategoria(categoria);
        producto.setTitulo("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz");
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.setEspecificaciones(List.of(new ProductoEspecificacion(producto, "Capacidad", "16 GB")));
        producto.setImagenes(List.of(new ImagenProducto(producto, "https://img.compunex.com/products/ram.jpg", (short) 1)));

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(5L, 10L)).thenReturn(Optional.of(producto));

        ProductoDetalleResponseDTO resultado = productoService.obtenerFichaTecnica(correo, 5L);

        assertEquals(5L, resultado.id());
        assertEquals("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz", resultado.titulo());
        assertEquals("TechNova Mayorista S.A.C.", resultado.proveedorRazonSocial());
        assertEquals(1, resultado.especificaciones().size());
        assertEquals("Capacidad", resultado.especificaciones().get(0).clave());
        assertEquals(1, resultado.imagenes().size());
        assertEquals("https://img.compunex.com/products/ram.jpg", resultado.imagenes().get(0).urlImagen());
    }

    @Test
    @DisplayName("[2.2] debeLanzar404CuandoProductoNoExisteAlConsultarFichaTecnica")
    void debeLanzar404CuandoProductoNoExisteAlConsultarFichaTecnica() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productoService.obtenerFichaTecnica(correo, 99L));
    }

    @Test
    @DisplayName("[2.5] debeCambiarEstadoDePublicacionDelProducto")
    void debeCambiarEstadoDePublicacionDelProducto() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);
        var producto = new Producto();
        producto.setId(5L);
        producto.setEstado(EstadoProducto.ACTIVO);

        var request = new CambiarEstadoProductoDTO(EstadoProducto.DESHABILITADO_POR_PROVEEDOR);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(5L, 10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        CambioEstadoResponseDTO resultado = productoService.cambiarEstadoPublicacion(correo, 5L, request);

        assertEquals("ACTIVO", resultado.estadoAnterior());
        assertEquals("DESHABILITADO_POR_PROVEEDOR", resultado.nuevoEstado());
        assertEquals(EstadoProducto.DESHABILITADO_POR_PROVEEDOR, producto.getEstado());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("[2.5] debeLanzar404CuandoProductoNoEsDelProveedorAlCambiarEstado")
    void debeLanzar404CuandoProductoNoEsDelProveedorAlCambiarEstado() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);
        var request = new CambiarEstadoProductoDTO(EstadoProducto.ACTIVO);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productoService.cambiarEstadoPublicacion(correo, 99L, request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("[2.3] debeGuardarProductoConRelacionesEnCascada")
    void debeGuardarProductoConRelacionesEnCascada() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);

        var perfil = new PerfilProveedor();
        perfil.setId(10L);
        perfil.setRazonSocial("TechNova Mayorista S.A.C.");

        var categoria = new Categoria("ram", "Memorias RAM", null, null, EstadoCategoria.ACTIVA, 0);

        var request = new CrearProductoRequestDTO(
                "ram",
                "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz",
                "Módulo de memoria DDR5 de alto rendimiento...",
                ModeloComercial.DISTRIBUIDOR_OFICIAL,
                "Caja Master x10 Blísteres",
                "10 unidades",
                "1 caja (10 unidades)",
                new BigDecimal("52.50"),
                new BigDecimal("525.00"),
                "USD",
                "Garantía de 3 años directa con fabricante.",
                List.of(
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Capacidad", "16 GB"),
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Frecuencia", "5600 MHz"),
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Latencia CAS", "CL36"),
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Voltaje", "1.25V")
                ),
                List.of(
                        new CrearProductoRequestDTO.ImagenItemDTO("https://img.compunex.com/products/ram-kingston-16gb-front.jpg", (short) 1),
                        new CrearProductoRequestDTO.ImagenItemDTO("https://img.compunex.com/products/ram-kingston-16gb-angle.jpg", (short) 2)
                )
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(categoriaRepository.findById("ram")).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> {
            Producto p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        CrearProductoResponseDTO resultado = productoService.crearProducto(correo, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertNotNull(resultado.uuid());
        assertEquals(10L, resultado.proveedorId());
        assertEquals("ram", resultado.categoriaId());
        assertEquals("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz", resultado.titulo());
        assertEquals(new BigDecimal("52.50"), resultado.precioUnitarioRef());
        assertEquals(new BigDecimal("525.00"), resultado.precioTotalRef());
        assertEquals("USD", resultado.moneda());
        assertEquals("ACTIVO", resultado.estado());
        assertNotNull(resultado.fechaPublicacion());

        verify(productoRepository, times(1)).save(argThat(p -> {
            assertEquals(4, p.getEspecificaciones().size());
            assertEquals(2, p.getImagenes().size());
            assertEquals("Capacidad", p.getEspecificaciones().get(0).getClave());
            assertEquals(p, p.getEspecificaciones().get(0).getProducto());
            assertEquals("https://img.compunex.com/products/ram-kingston-16gb-front.jpg", p.getImagenes().get(0).getUrlImagen());
            assertEquals(p, p.getImagenes().get(0).getProducto());
            return true;
        }));
        verify(categoriaRepository, times(1)).save(categoria);
        assertEquals(1, categoria.getTotalProductos());
    }

    @Test
    @DisplayName("[2.4] debeActualizarDatosComercialesDelProducto")
    void debeActualizarDatosComercialesDelProducto() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);

        var perfil = new PerfilProveedor();
        perfil.setId(10L);

        var producto = new Producto();
        producto.setId(1L);
        UUID fixedUuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        producto.setUuid(fixedUuid);
        producto.setProveedor(perfil);
        producto.setTitulo("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz");
        producto.setPrecioUnitarioRef(new BigDecimal("52.50"));
        producto.setPrecioTotalRef(new BigDecimal("525.00"));
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.getEspecificaciones().add(new ProductoEspecificacion(producto, "Capacidad", "16 GB"));
        producto.getImagenes().add(new ImagenProducto(producto, "https://img.compunex.com/products/ram-old.jpg", (short) 1));

        var request = new ActualizarProductoRequestDTO(
                null,
                "Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)",
                "Descripción actualizada con nuevo stock certificado y empaque sellado.",
                null,
                null,
                null,
                "5 unidades",
                new BigDecimal("49.90"),
                new BigDecimal("249.50"),
                null,
                "Precio rebajado por compra directa a partir de 5 piezas.",
                List.of(
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Capacidad", "16 GB"),
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Frecuencia", "5600 MHz"),
                        new CrearProductoRequestDTO.EspecificacionItemDTO("Disipador", "Aluminio Negro Anodizado")
                ),
                List.of(
                        new CrearProductoRequestDTO.ImagenItemDTO("https://img.compunex.com/products/ram-kingston-16gb-new.jpg", (short) 1)
                )
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.findByIdAndProveedorId(1L, 10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        ActualizarProductoResponseDTO resultado = productoService.actualizarProducto(correo, 1L, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals(fixedUuid, resultado.uuid());
        assertEquals("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz (Lote 2026)", resultado.titulo());
        assertEquals(new BigDecimal("49.90"), resultado.precioUnitarioRef());
        assertEquals(new BigDecimal("249.50"), resultado.precioTotalRef());
        assertEquals("ACTIVO", resultado.estado());
        assertNotNull(resultado.fechaActualizacion());

        // Verificar mutación en la entidad Producto
        assertEquals("5 unidades", producto.getPedidoMinimo());
        assertEquals("Precio rebajado por compra directa a partir de 5 piezas.", producto.getTerminosComerciales());
        assertEquals(3, producto.getEspecificaciones().size());
        assertEquals(1, producto.getImagenes().size());
        assertEquals("Disipador", producto.getEspecificaciones().get(2).getClave());
        assertEquals("Aluminio Negro Anodizado", producto.getEspecificaciones().get(2).getValor());
        assertEquals("https://img.compunex.com/products/ram-kingston-16gb-new.jpg", producto.getImagenes().get(0).getUrlImagen());

        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe buscar productos del proveedor usando JPQL cuando search no es nulo")
    void debeFiltrarProductosPorNombreConJPQL() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(productoRepository.buscarPorProveedorYTitulo(eq(10L), eq(EstadoProducto.ELIMINADO_LOGICO), eq("SSD"), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var resultado = productoService.listarMisProductos(correo, "SSD", pageable);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).buscarPorProveedorYTitulo(10L, EstadoProducto.ELIMINADO_LOGICO, "SSD", pageable);
        verify(productoRepository, never()).findAllByProveedorIdAndEstadoNot(any(), any(), any());
    }

    @Test
    @DisplayName("Admin: Debe listar productos de todos los proveedores sin filtro")
    void debeListarProductosParaAdminSinFiltro() {
        when(productoRepository.findAllByEstadoNot(eq(EstadoProducto.ELIMINADO_LOGICO), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var resultado = productoService.listarProductosAdmin(null, pageable);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).findAllByEstadoNot(EstadoProducto.ELIMINADO_LOGICO, pageable);
    }

    @Test
    @DisplayName("Admin: Debe listar productos con búsqueda por título")
    void debeListarProductosParaAdminConFiltro() {
        when(productoRepository.buscarTodosPorTitulo(eq(EstadoProducto.ELIMINADO_LOGICO), eq("RTX"), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var resultado = productoService.listarProductosAdmin("RTX", pageable);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).buscarTodosPorTitulo(EstadoProducto.ELIMINADO_LOGICO, "RTX", pageable);
    }

    @Test
    @DisplayName("Admin: Debe cambiar estado de cualquier producto sin restricción de proveedor")
    void debeCambiarEstadoPublicacionAdminExitosamente() {
        var producto = new Producto();
        producto.setId(5L);
        producto.setUuid(UUID.randomUUID());
        producto.setEstado(EstadoProducto.ACTIVO);

        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        var req = new CambiarEstadoProductoDTO(EstadoProducto.OCULTO_POR_ADMIN);
        var resultado = productoService.cambiarEstadoPublicacionAdmin(5L, req);

        assertNotNull(resultado);
        assertEquals(5L, resultado.id());
        assertEquals("ACTIVO", resultado.estadoAnterior());
        assertEquals("OCULTO_POR_ADMIN", resultado.nuevoEstado());
        assertEquals(EstadoProducto.OCULTO_POR_ADMIN, producto.getEstado());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Transaccional: Debe fallar atómicamente y propagar excepción si categoría no existe")
    void debeLanzarExcepcionYNoPersistirCuandoCategoriaNoExiste() {
        String correo = "ventas.mayoristas@technova.com";
        var usuario = new Usuario("TechNova", correo, "hash", RolUsuario.PROVEEDOR, EstadoUsuario.ACTIVO);
        usuario.setId(1L);
        var perfil = new PerfilProveedor();
        perfil.setId(10L);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(perfilProveedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(categoriaRepository.findById("categoria-fantasma")).thenReturn(Optional.empty());

        var request = new CrearProductoRequestDTO(
                "categoria-fantasma", "Titulo", "Descripcion",
                ModeloComercial.DISTRIBUIDOR_OFICIAL, "Caja", "10", "1",
                new BigDecimal("50.00"), new BigDecimal("500.00"), "USD",
                "Terminos", List.of(), List.of(new CrearProductoRequestDTO.ImagenItemDTO("http://img.jpg", (short) 1))
        );

        assertThrows(EntityNotFoundException.class, () -> productoService.crearProducto(correo, request));
        verify(productoRepository, never()).save(any());
    }
}
