package com.compunex.b2b.modules.catalogo.repository;

import com.compunex.b2b.modules.auth.entity.EstadoUsuario;
import com.compunex.b2b.modules.auth.entity.RolUsuario;
import com.compunex.b2b.modules.auth.entity.Usuario;
import com.compunex.b2b.modules.auth.repository.UsuarioRepository;
import com.compunex.b2b.modules.catalogo.entity.Categoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoCategoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoProducto;
import com.compunex.b2b.modules.catalogo.entity.ModeloComercial;
import com.compunex.b2b.modules.catalogo.entity.Producto;
import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import com.compunex.b2b.modules.perfiles.repository.PerfilProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilProveedorRepository perfilProveedorRepository;

    private PerfilProveedor proveedor;
    private Categoria categoria;
    private Producto productoSsd;
    private Producto productoRam;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario(
                "Proveedor Test S.A.C.",
                "proveedor.repo@test.com",
                "hash",
                RolUsuario.PROVEEDOR,
                EstadoUsuario.ACTIVO
        );
        usuario = usuarioRepository.save(usuario);

        proveedor = new PerfilProveedor();
        proveedor.setUsuario(usuario);
        proveedor.setIdentificacionFiscal("20111222333");
        proveedor.setRazonSocial("Proveedor Test S.A.C.");
        proveedor.setNombreComercial("Test Store");
        proveedor.setTelefonoContacto("+51 900000000");
        proveedor.setDireccion("Av. Test 123, San Isidro");
        proveedor.setCiudad("Lima");
        proveedor = perfilProveedorRepository.save(proveedor);

        categoria = new Categoria("almacenamiento", "Almacenamiento", "hard-drive", "Discos y memorias", EstadoCategoria.ACTIVA, 2);
        categoria = categoriaRepository.save(categoria);

        productoSsd = new Producto();
        productoSsd.setProveedor(proveedor);
        productoSsd.setCategoria(categoria);
        productoSsd.setTitulo("Disco SSD NVMe Kingston 1TB");
        productoSsd.setDescripcion("SSD de alta velocidad PCIe 4.0");
        productoSsd.setModeloComercial(ModeloComercial.DISTRIBUIDOR_OFICIAL);
        productoSsd.setTipoFormato("Caja");
        productoSsd.setUnidadesPorPaquete("5 unidades");
        productoSsd.setPedidoMinimo("1 caja");
        productoSsd.setPrecioUnitarioRef(new BigDecimal("75.00"));
        productoSsd.setPrecioTotalRef(new BigDecimal("375.00"));
        productoSsd.setMoneda("USD");
        productoSsd.setEstado(EstadoProducto.ACTIVO);
        productoSsd = productoRepository.save(productoSsd);

        productoRam = new Producto();
        productoRam.setProveedor(proveedor);
        productoRam.setCategoria(categoria);
        productoRam.setTitulo("Memoria RAM Corsair 16GB DDR5");
        productoRam.setDescripcion("RAM de alto rendimiento");
        productoRam.setModeloComercial(ModeloComercial.MAYORISTA_PURO);
        productoRam.setTipoFormato("Blister");
        productoRam.setUnidadesPorPaquete("10 unidades");
        productoRam.setPedidoMinimo("1 caja");
        productoRam.setPrecioUnitarioRef(new BigDecimal("60.00"));
        productoRam.setPrecioTotalRef(new BigDecimal("600.00"));
        productoRam.setMoneda("USD");
        productoRam.setEstado(EstadoProducto.ACTIVO);
        productoRam = productoRepository.save(productoRam);
    }

    @Test
    @DisplayName("JPQL: Debe buscar productos por proveedor y coincidencia en título ignorando mayúsculas")
    void debeBuscarPorProveedorYTituloConJPQL() {
        Page<Producto> resultado = productoRepository.buscarPorProveedorYTitulo(
                proveedor.getId(),
                EstadoProducto.ELIMINADO_LOGICO,
                "ssd",
                PageRequest.of(0, 10)
        );

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Disco SSD NVMe Kingston 1TB", resultado.getContent().get(0).getTitulo());
    }

    @Test
    @DisplayName("JPQL: No debe incluir productos eliminados lógicamente en la búsqueda")
    void noDebeIncluirProductosEliminadosEnBusqueda() {
        productoSsd.setEstado(EstadoProducto.ELIMINADO_LOGICO);
        productoRepository.save(productoSsd);

        Page<Producto> resultado = productoRepository.buscarPorProveedorYTitulo(
                proveedor.getId(),
                EstadoProducto.ELIMINADO_LOGICO,
                "SSD",
                PageRequest.of(0, 10)
        );

        assertEquals(0, resultado.getTotalElements());
    }

    @Test
    @DisplayName("JPQL: Debe buscar productos de todos los proveedores para administración")
    void debeBuscarTodosPorTituloParaAdmin() {
        Page<Producto> resultado = productoRepository.buscarTodosPorTitulo(
                EstadoProducto.ELIMINADO_LOGICO,
                "corsair",
                PageRequest.of(0, 10)
        );

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Memoria RAM Corsair 16GB DDR5", resultado.getContent().get(0).getTitulo());
    }
}
