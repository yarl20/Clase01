package com.compunex.b2b.modules.catalogo.entity;

import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import jakarta.persistence.*;

import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private PerfilProveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "modelo_comercial", nullable = false, length = 30)
    private ModeloComercial modeloComercial;

    @Column(name = "tipo_formato", nullable = false, length = 100)
    private String tipoFormato;

    @Column(name = "unidades_por_paquete", nullable = false, length = 100)
    private String unidadesPorPaquete;

    @Column(name = "pedido_minimo", nullable = false, length = 100)
    private String pedidoMinimo;

    @Column(name = "precio_unitario_ref", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioRef;

    @Column(name = "precio_total_ref", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotalRef;

    @Column(nullable = false, length = 10)
    private String moneda = "USD";

    @Column(name = "terminos_comerciales", columnDefinition = "TEXT")
    private String terminosComerciales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @Column(name = "motivo_moderacion", columnDefinition = "TEXT")
    private String motivoModeracion;

    @Column(name = "es_recomendado", nullable = false)
    private Boolean esRecomendado = false;

    @Column(name = "es_promocionado", nullable = false)
    private Boolean esPromocionado = false;

    @Column(name = "tiene_oferta", nullable = false)
    private Boolean tieneOferta = false;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoEspecificacion> especificaciones = new ArrayList<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private Instant fechaPublicacion = Instant.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion = Instant.now();

    @Column(name = "fecha_eliminacion")
    private Instant fechaEliminacion;

    @Column(name = "eliminado_por")
    private Long eliminadoPor;

    public Producto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public PerfilProveedor getProveedor() { return proveedor; }
    public void setProveedor(PerfilProveedor proveedor) { this.proveedor = proveedor; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public ModeloComercial getModeloComercial() { return modeloComercial; }
    public void setModeloComercial(ModeloComercial modeloComercial) { this.modeloComercial = modeloComercial; }
    public String getTipoFormato() { return tipoFormato; }
    public void setTipoFormato(String tipoFormato) { this.tipoFormato = tipoFormato; }
    public String getUnidadesPorPaquete() { return unidadesPorPaquete; }
    public void setUnidadesPorPaquete(String unidadesPorPaquete) { this.unidadesPorPaquete = unidadesPorPaquete; }
    public String getPedidoMinimo() { return pedidoMinimo; }
    public void setPedidoMinimo(String pedidoMinimo) { this.pedidoMinimo = pedidoMinimo; }
    public BigDecimal getPrecioUnitarioRef() { return precioUnitarioRef; }
    public void setPrecioUnitarioRef(BigDecimal precioUnitarioRef) { this.precioUnitarioRef = precioUnitarioRef; }
    public BigDecimal getPrecioTotalRef() { return precioTotalRef; }
    public void setPrecioTotalRef(BigDecimal precioTotalRef) { this.precioTotalRef = precioTotalRef; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getTerminosComerciales() { return terminosComerciales; }
    public void setTerminosComerciales(String terminosComerciales) { this.terminosComerciales = terminosComerciales; }
    public EstadoProducto getEstado() { return estado; }
    public void setEstado(EstadoProducto estado) { this.estado = estado; }
    public String getMotivoModeracion() { return motivoModeracion; }
    public void setMotivoModeracion(String motivoModeracion) { this.motivoModeracion = motivoModeracion; }
    public Boolean getEsRecomendado() { return esRecomendado; }
    public void setEsRecomendado(Boolean esRecomendado) { this.esRecomendado = esRecomendado; }
    public Boolean getEsPromocionado() { return esPromocionado; }
    public void setEsPromocionado(Boolean esPromocionado) { this.esPromocionado = esPromocionado; }
    public Boolean getTieneOferta() { return tieneOferta; }
    public void setTieneOferta(Boolean tieneOferta) { this.tieneOferta = tieneOferta; }
    public List<ProductoEspecificacion> getEspecificaciones() { return especificaciones; }
    public void setEspecificaciones(List<ProductoEspecificacion> especificaciones) { this.especificaciones = especificaciones; }
    public List<ImagenProducto> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenProducto> imagenes) { this.imagenes = imagenes; }
    public Instant getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(Instant fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Instant getFechaEliminacion() { return fechaEliminacion; }
    public void setFechaEliminacion(Instant fechaEliminacion) { this.fechaEliminacion = fechaEliminacion; }
    public Long getEliminadoPor() { return eliminadoPor; }
    public void setEliminadoPor(Long eliminadoPor) { this.eliminadoPor = eliminadoPor; }
}
