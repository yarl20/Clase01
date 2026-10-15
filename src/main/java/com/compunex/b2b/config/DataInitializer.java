package com.compunex.b2b.config;

import com.compunex.b2b.modules.auth.entity.EstadoUsuario;
import com.compunex.b2b.modules.auth.entity.RolUsuario;
import com.compunex.b2b.modules.auth.entity.Usuario;
import com.compunex.b2b.modules.auth.repository.UsuarioRepository;
import com.compunex.b2b.modules.catalogo.entity.*;
import com.compunex.b2b.modules.catalogo.repository.CategoriaRepository;
import com.compunex.b2b.modules.catalogo.repository.ProductoRepository;
import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import com.compunex.b2b.modules.perfiles.repository.PerfilProveedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // =========================================================================
    // 🔑 CREDENCIALES Y DATOS DE PRUEBA (VISIBLES PARA POSTMAN Y FRONTEND)
    // =========================================================================
    public static final String PROVEEDOR_EMAIL = "ventas.mayoristas@technova.com";
    public static final String PROVEEDOR_PASSWORD = "PasswordMayorista123!";
    public static final String PROVEEDOR_RAZON_SOCIAL = "TechNova Mayorista S.A.C.";
    public static final String PROVEEDOR_RUC = "20608945612";
    public static final String PROVEEDOR_TELEFONO = "+51 987 654 321";
    public static final String PROVEEDOR_CIUDAD = "Lima";

    // Credenciales de prueba para Rol Administrador
    public static final String ADMIN_EMAIL = "admin@compunex.com";
    public static final String ADMIN_PASSWORD = "AdminPassword2026!";
    public static final String ADMIN_NOMBRE = "Administrador General COMPUNEX";
    // =========================================================================

    private final UsuarioRepository usuarioRepository;
    private final PerfilProveedorRepository perfilProveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
            PerfilProveedorRepository perfilProveedorRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilProveedorRepository = perfilProveedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Asegurar que el usuario Administrador de prueba exista para desarrollo
        if (usuarioRepository.findByCorreo(ADMIN_EMAIL).isEmpty()) {
            Usuario usuarioAdmin = new Usuario(
                    ADMIN_NOMBRE,
                    ADMIN_EMAIL,
                    passwordEncoder.encode(ADMIN_PASSWORD),
                    RolUsuario.ADMIN,
                    EstadoUsuario.ACTIVO
            );
            usuarioRepository.save(usuarioAdmin);
            log.info("[COMPUNEX-INIT] ✅ Usuario Administrador de desarrollo creado: {}", ADMIN_EMAIL);
        }

        if (usuarioRepository.findByCorreo(PROVEEDOR_EMAIL).isPresent()) {
            Usuario uExistente = usuarioRepository.findByCorreo(PROVEEDOR_EMAIL).get();
            if (!passwordEncoder.matches(PROVEEDOR_PASSWORD, uExistente.getHashContrasena())) {
                uExistente.setHashContrasena(passwordEncoder.encode(PROVEEDOR_PASSWORD));
                usuarioRepository.save(uExistente);
                log.info("[COMPUNEX-INIT] 🔄 Contraseña del proveedor de desarrollo sincronizada.");
            }
            log.info("[COMPUNEX-INIT] La base de datos ya contiene registros del proveedor. Omitiendo seed data de catálogo.");
            return;
        }

        log.info("[COMPUNEX-INIT] 🚀 Sembrando datos iniciales de prueba (Seed Data)...");

        // 1. Crear Usuario Proveedor (Para login según contrato oficial)
        Usuario usuarioProveedor = new Usuario(
                PROVEEDOR_RAZON_SOCIAL,
                PROVEEDOR_EMAIL,
                passwordEncoder.encode(PROVEEDOR_PASSWORD),
                RolUsuario.PROVEEDOR,
                EstadoUsuario.ACTIVO
        );
        usuarioProveedor = usuarioRepository.save(usuarioProveedor);

        // 2. Crear Perfil de Proveedor
        PerfilProveedor perfil = new PerfilProveedor();
        perfil.setUsuario(usuarioProveedor);
        perfil.setIdentificacionFiscal(PROVEEDOR_RUC);
        perfil.setRazonSocial(PROVEEDOR_RAZON_SOCIAL);
        perfil.setNombreComercial("TechNova Hardware B2B");
        perfil.setTelefonoContacto(PROVEEDOR_TELEFONO);
        perfil.setUrlLogo("https://img.compunex.com/logos/technova.png");
        perfil.setUrlBanner("https://img.compunex.com/banners/technova.jpg");
        perfil.setDireccion("Av. Los Conquistadores 1234, San Isidro");
        perfil.setCiudad(PROVEEDOR_CIUDAD);
        perfil.setAnosMercado("8 años");
        perfil.setDescripcion("Importador y distribuidor mayorista autorizado de componentes de PC.");
        perfil.setPoliticaComercial("Despachos a nivel nacional. Pedido mínimo 500 USD.");
        perfil.setCalificacion(new BigDecimal("4.85"));
        perfil.setVerificado(true);
        perfil.setDestacado(true);
        perfil = perfilProveedorRepository.save(perfil);

        // 3. Crear Categorías y Subcategorías
        Categoria catRam = new Categoria("ram", "Memorias RAM", "memory", "Módulos de memoria DDR4 y DDR5 para PC y Servidores", EstadoCategoria.ACTIVA, 45);
        catRam.getSubcategorias().addAll(List.of(
                new Subcategoria(catRam, "DDR5 Desktop"),
                new Subcategoria(catRam, "DDR4 Desktop"),
                new Subcategoria(catRam, "DDR5 SO-DIMM Laptop")
        ));
        categoriaRepository.save(catRam);

        Categoria catSsd = new Categoria("ssd", "Almacenamiento SSD", "storage", "Unidades de estado sólido NVMe M.2 y SATA III", EstadoCategoria.ACTIVA, 38);
        catSsd.getSubcategorias().addAll(List.of(
                new Subcategoria(catSsd, "NVMe M.2 PCIe 4.0"),
                new Subcategoria(catSsd, "NVMe M.2 PCIe 3.0"),
                new Subcategoria(catSsd, "SATA III 2.5\"")
        ));
        categoriaRepository.save(catSsd);

        Categoria catGpu = new Categoria("gpu", "Tarjetas Gráficas (GPU)", "Layers", "Tarjetas de video GeForce RTX y AMD Radeon", EstadoCategoria.ACTIVA, 24);
        catGpu.getSubcategorias().addAll(List.of(
                new Subcategoria(catGpu, "GeForce RTX 4000"),
                new Subcategoria(catGpu, "Radeon RX 7000"),
                new Subcategoria(catGpu, "Workstation Pro")
        ));
        categoriaRepository.save(catGpu);

        Categoria catCpu = new Categoria("cpu", "Procesadores", "Zap", "Procesadores Intel Core 13ª/14ª Gen y AMD Ryzen 7000/8000", EstadoCategoria.ACTIVA, 30);
        catCpu.getSubcategorias().addAll(List.of(
                new Subcategoria(catCpu, "Intel Core 14ª Gen"),
                new Subcategoria(catCpu, "Intel Core 13ª Gen"),
                new Subcategoria(catCpu, "AMD Ryzen AM5"),
                new Subcategoria(catCpu, "AMD Ryzen AM4")
        ));
        categoriaRepository.save(catCpu);

        Categoria catMb = new Categoria("motherboard", "Placas Madre", "Grid", "Mainboards chipsets Z790, B760, X670, B650", EstadoCategoria.ACTIVA, 18);
        catMb.getSubcategorias().addAll(List.of(
                new Subcategoria(catMb, "Chipset Z790/B760"),
                new Subcategoria(catMb, "Chipset X670/B650"),
                new Subcategoria(catMb, "Micro-ATX"),
                new Subcategoria(catMb, "Mini-ITX")
        ));
        categoriaRepository.save(catMb);

        Categoria catHdd = new Categoria("hdd", "Discos Duros (HDD)", "Database", "Discos mecánicos para almacenamiento masivo y NAS", EstadoCategoria.ACTIVA, 15);
        catHdd.getSubcategorias().addAll(List.of(
                new Subcategoria(catHdd, "HDD Surveillance 24/7"),
                new Subcategoria(catHdd, "HDD NAS Enterprise"),
                new Subcategoria(catHdd, "HDD Desktop 3.5\"")
        ));
        categoriaRepository.save(catHdd);

        Categoria catPsu = new Categoria("psu", "Fuentes de Poder", "BatteryCharging", "Fuentes 80 Plus Bronze, Gold y Platinum ATX 3.0", EstadoCategoria.ACTIVA, 20);
        catPsu.getSubcategorias().addAll(List.of(
                new Subcategoria(catPsu, "80+ Bronze"),
                new Subcategoria(catPsu, "80+ Gold Modular"),
                new Subcategoria(catPsu, "80+ Platinum ATX 3.0")
        ));
        categoriaRepository.save(catPsu);

        Categoria catUsb = new Categoria("usb", "Memorias USB & Flash", "Usb", "Pen drives USB 3.2 y tarjetas MicroSD por paquete máster", EstadoCategoria.ACTIVA, 12);
        catUsb.getSubcategorias().addAll(List.of(
                new Subcategoria(catUsb, "USB 3.2 Gen 1"),
                new Subcategoria(catUsb, "USB Tipo-C"),
                new Subcategoria(catUsb, "Tarjetas MicroSD Clase 10")
        ));
        categoriaRepository.save(catUsb);

        // 4. Crear Producto de Muestra
        Producto prod = new Producto();
        prod.setProveedor(perfil);
        prod.setCategoria(catRam);
        prod.setTitulo("Memoria RAM Kingston Fury Beast 16GB DDR5 5600MHz");
        prod.setDescripcion("Módulo de memoria DDR5 de alto rendimiento optimizado para plataformas Intel XMP 3.0 y AMD EXPO. Ideal para ensamblajes corporativos y gaming de gama alta.");
        prod.setModeloComercial(ModeloComercial.DISTRIBUIDOR_OFICIAL);
        prod.setTipoFormato("Caja Master x10 Blísteres");
        prod.setUnidadesPorPaquete("10 unidades");
        prod.setPedidoMinimo("1 caja (10 unidades)");
        prod.setPrecioUnitarioRef(new BigDecimal("52.50"));
        prod.setPrecioTotalRef(new BigDecimal("525.00"));
        prod.setMoneda("USD");
        prod.setTerminosComerciales("Garantía de 3 años directa con fabricante. Entrega en almacén Lima o envío a provincia con flete por pagar.");
        prod.setEstado(EstadoProducto.ACTIVO);
        prod.setEsRecomendado(true);
        prod.setEsPromocionado(false);
        prod.setTieneOferta(false);

        ProductoEspecificacion spec1 = new ProductoEspecificacion(prod, "Capacidad", "16 GB");
        ProductoEspecificacion spec2 = new ProductoEspecificacion(prod, "Frecuencia", "5600 MHz");
        ProductoEspecificacion spec3 = new ProductoEspecificacion(prod, "Latencia CAS", "CL36");
        ProductoEspecificacion spec4 = new ProductoEspecificacion(prod, "Voltaje", "1.25V");
        prod.getEspecificaciones().addAll(List.of(spec1, spec2, spec3, spec4));

        ImagenProducto img1 = new ImagenProducto(prod, "https://img.compunex.com/products/ram-kingston-16gb-front.jpg", (short) 1);
        ImagenProducto img2 = new ImagenProducto(prod, "https://img.compunex.com/products/ram-kingston-16gb-angle.jpg", (short) 2);
        prod.getImagenes().addAll(List.of(img1, img2));

        productoRepository.save(prod);

        log.info("[COMPUNEX-INIT] ✅ Base de datos sembrada con éxito: 1 Proveedor, 8 Categorías completas y 1 Producto con ficha técnica.");
    }
}
