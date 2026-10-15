package com.compunex.b2b.modules.catalogo.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compunex.b2b.modules.auth.repository.UsuarioRepository;
import com.compunex.b2b.modules.catalogo.dto.request.ActualizarProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CambiarEstadoProductoDTO;
import com.compunex.b2b.modules.catalogo.dto.request.CrearProductoRequestDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ActualizarProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CambioEstadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.CrearProductoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoDetalleResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoPaginadoResponseDTO;
import com.compunex.b2b.modules.catalogo.dto.response.ProductoResumenResponseDTO;
import com.compunex.b2b.modules.catalogo.entity.EstadoProducto;
import com.compunex.b2b.modules.catalogo.entity.ImagenProducto;
import com.compunex.b2b.modules.catalogo.entity.Producto;
import com.compunex.b2b.modules.catalogo.entity.ProductoEspecificacion;
import com.compunex.b2b.modules.catalogo.repository.CategoriaRepository;
import com.compunex.b2b.modules.catalogo.repository.ProductoRepository;
import com.compunex.b2b.modules.catalogo.service.ProductoService;
import com.compunex.b2b.modules.perfiles.repository.PerfilProveedorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilProveedorRepository perfilProveedorRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(UsuarioRepository usuarioRepository,
            PerfilProveedorRepository perfilProveedorRepository,
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilProveedorRepository = perfilProveedorRepository;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public ProductoPaginadoResponseDTO listarMisProductos(String correoAutenticado, String search, Pageable pageable) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        Page<Producto> pagina;
        if (search != null && !search.trim().isEmpty()) {
            pagina = productoRepository.buscarPorProveedorYTitulo(
                    perfil.getId(),
                    EstadoProducto.ELIMINADO_LOGICO,
                    search.trim(),
                    pageable
            );
        } else {
            pagina = productoRepository.findAllByProveedorIdAndEstadoNot(
                    perfil.getId(),
                    EstadoProducto.ELIMINADO_LOGICO,
                    pageable
            );
        }

        Page<ProductoResumenResponseDTO> paginaDto = pagina.map(ProductoResumenResponseDTO::from);

        return new ProductoPaginadoResponseDTO(
                paginaDto.getContent(),
                paginaDto.getNumber(),
                paginaDto.getSize(),
                paginaDto.getTotalElements(),
                paginaDto.getTotalPages(),
                paginaDto.isLast()
        );
    }

    @Override
    public ProductoDetalleResponseDTO obtenerFichaTecnica(String correoAutenticado, Long productoId) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        var producto = productoRepository.findByIdAndProveedorId(productoId, perfil.getId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        return ProductoDetalleResponseDTO.from(producto);
    }

    @Override
    @Transactional
    public CrearProductoResponseDTO crearProducto(String correoAutenticado, CrearProductoRequestDTO request) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        var categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada: " + request.categoriaId()));

        Producto producto = new Producto();
        producto.setProveedor(perfil);
        producto.setCategoria(categoria);
        producto.setTitulo(request.titulo());
        producto.setDescripcion(request.descripcion());
        producto.setModeloComercial(request.modeloComercial());
        producto.setTipoFormato(request.tipoFormato());
        producto.setUnidadesPorPaquete(request.unidadesPorPaquete());
        producto.setPedidoMinimo(request.pedidoMinimo());
        producto.setPrecioUnitarioRef(request.precioUnitarioRef());
        producto.setPrecioTotalRef(request.precioTotalRef() != null ? request.precioTotalRef() : request.precioUnitarioRef());
        producto.setMoneda(request.moneda() != null && !request.moneda().isBlank() ? request.moneda() : "USD");
        producto.setTerminosComerciales(request.terminosComerciales());
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.setEsRecomendado(false);
        producto.setEsPromocionado(false);
        producto.setTieneOferta(false);
        producto.setFechaPublicacion(Instant.now());
        producto.setFechaActualizacion(Instant.now());

        if (request.especificaciones() != null && !request.especificaciones().isEmpty()) {
            List<ProductoEspecificacion> specs = request.especificaciones().stream()
                    .map(dto -> new ProductoEspecificacion(producto, dto.clave(), dto.valor()))
                    .toList();
            producto.getEspecificaciones().addAll(specs);
        }

        if (request.imagenes() != null && !request.imagenes().isEmpty()) {
            short ordenFallback = 1;
            for (CrearProductoRequestDTO.ImagenItemDTO imgDto : request.imagenes()) {
                short orden = imgDto.orden() != null ? imgDto.orden() : ordenFallback++;
                producto.getImagenes().add(new ImagenProducto(producto, imgDto.urlImagen(), orden));
            }
        }

        categoria.setTotalProductos((categoria.getTotalProductos() != null ? categoria.getTotalProductos() : 0) + 1);
        categoriaRepository.save(categoria);

        Producto productoGuardado = productoRepository.save(producto);
        return CrearProductoResponseDTO.from(productoGuardado);
    }

    @Override
    @Transactional
    public ActualizarProductoResponseDTO actualizarProducto(String correoAutenticado, Long productoId, ActualizarProductoRequestDTO request) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        var producto = productoRepository.findByIdAndProveedorId(productoId, perfil.getId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        if (request.categoriaId() != null && !request.categoriaId().isBlank()) {
            var nuevaCategoria = categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada: " + request.categoriaId()));
            producto.setCategoria(nuevaCategoria);
        }

        if (request.titulo() != null) {
            producto.setTitulo(request.titulo());
        }
        if (request.descripcion() != null) {
            producto.setDescripcion(request.descripcion());
        }
        if (request.modeloComercial() != null) {
            producto.setModeloComercial(request.modeloComercial());
        }
        if (request.tipoFormato() != null) {
            producto.setTipoFormato(request.tipoFormato());
        }
        if (request.unidadesPorPaquete() != null) {
            producto.setUnidadesPorPaquete(request.unidadesPorPaquete());
        }
        if (request.pedidoMinimo() != null) {
            producto.setPedidoMinimo(request.pedidoMinimo());
        }
        if (request.precioUnitarioRef() != null) {
            producto.setPrecioUnitarioRef(request.precioUnitarioRef());
        }
        if (request.precioTotalRef() != null) {
            producto.setPrecioTotalRef(request.precioTotalRef());
        }
        if (request.moneda() != null && !request.moneda().isBlank()) {
            producto.setMoneda(request.moneda());
        }
        if (request.terminosComerciales() != null) {
            producto.setTerminosComerciales(request.terminosComerciales());
        }

        if (request.especificaciones() != null) {
            producto.getEspecificaciones().clear();
            List<ProductoEspecificacion> nuevasSpecs = request.especificaciones().stream()
                    .map(dto -> new ProductoEspecificacion(producto, dto.clave(), dto.valor()))
                    .toList();
            producto.getEspecificaciones().addAll(nuevasSpecs);
        }

        if (request.imagenes() != null) {
            producto.getImagenes().clear();
            short ordenFallback = 1;
            for (var imgDto : request.imagenes()) {
                short orden = imgDto.orden() != null ? imgDto.orden() : ordenFallback++;
                producto.getImagenes().add(new ImagenProducto(producto, imgDto.urlImagen(), orden));
            }
        }

        producto.setFechaActualizacion(Instant.now());
        Producto productoActualizado = productoRepository.save(producto);
        return ActualizarProductoResponseDTO.from(productoActualizado);
    }

    @Override
    @Transactional
    public CambioEstadoResponseDTO cambiarEstadoPublicacion(String correoAutenticado, Long productoId,
            CambiarEstadoProductoDTO request) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        var producto = productoRepository.findByIdAndProveedorId(productoId, perfil.getId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        EstadoProducto estadoAnterior = producto.getEstado();
        EstadoProducto nuevoEstado = request.estado();

        producto.setEstado(nuevoEstado);
        producto.setFechaActualizacion(Instant.now());
        productoRepository.save(producto);

        return new CambioEstadoResponseDTO(
                producto.getId(),
                producto.getUuid(),
                estadoAnterior.name(),
                nuevoEstado.name(),
                construirMensajeCambioEstado(nuevoEstado),
                producto.getFechaActualizacion()
        );
    }

    private String construirMensajeCambioEstado(EstadoProducto nuevoEstado) {
        return switch (nuevoEstado) {
            case ACTIVO ->
                "El producto ha sido activado nuevamente en el catálogo mayorista";
            case DESHABILITADO_POR_PROVEEDOR ->
                "El producto ha sido deshabilitado del catálogo mayorista temporalmente";
            case OCULTO_POR_ADMIN ->
                "El producto ha sido ocultado por un administrador del sistema";
            case ELIMINADO_LOGICO ->
                "El producto ha sido eliminado del catálogo";
        };
    }

    @Override
    @Transactional
    public void eliminarMiProducto(String correoAutenticado, Long productoId) {
        var usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correoAutenticado));

        var perfil = perfilProveedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil de proveedor no encontrado para el usuario: " + correoAutenticado));

        var producto = productoRepository.findByIdAndProveedorId(productoId, perfil.getId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        producto.setEstado(EstadoProducto.ELIMINADO_LOGICO);
        producto.setFechaEliminacion(java.time.Instant.now());
        producto.setFechaActualizacion(java.time.Instant.now());
        productoRepository.save(producto);
    }

    @Override
    public ProductoPaginadoResponseDTO listarProductosAdmin(String search, Pageable pageable) {
        Page<Producto> pagina;
        if (search != null && !search.trim().isEmpty()) {
            pagina = productoRepository.buscarTodosPorTitulo(
                    EstadoProducto.ELIMINADO_LOGICO,
                    search.trim(),
                    pageable
            );
        } else {
            pagina = productoRepository.findAllByEstadoNot(
                    EstadoProducto.ELIMINADO_LOGICO,
                    pageable
            );
        }

        Page<ProductoResumenResponseDTO> paginaDto = pagina.map(ProductoResumenResponseDTO::from);

        return new ProductoPaginadoResponseDTO(
                paginaDto.getContent(),
                paginaDto.getNumber(),
                paginaDto.getSize(),
                paginaDto.getTotalElements(),
                paginaDto.getTotalPages(),
                paginaDto.isLast()
        );
    }

    @Override
    @Transactional
    public CambioEstadoResponseDTO cambiarEstadoPublicacionAdmin(Long productoId,
            CambiarEstadoProductoDTO request) {
        var producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        EstadoProducto estadoAnterior = producto.getEstado();
        EstadoProducto nuevoEstado = request.estado();

        producto.setEstado(nuevoEstado);
        producto.setFechaActualizacion(Instant.now());
        productoRepository.save(producto);

        return new CambioEstadoResponseDTO(
                producto.getId(),
                producto.getUuid(),
                estadoAnterior.name(),
                nuevoEstado.name(),
                construirMensajeCambioEstado(nuevoEstado),
                producto.getFechaActualizacion()
        );
    }
}
