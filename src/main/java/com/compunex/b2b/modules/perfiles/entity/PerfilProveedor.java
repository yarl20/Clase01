package com.compunex.b2b.modules.perfiles.entity;

import com.compunex.b2b.modules.auth.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "perfiles_proveedor")
public class PerfilProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "identificacion_fiscal", nullable = false, unique = true, length = 20)
    private String identificacionFiscal;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(name = "telefono_contacto", nullable = false, length = 20)
    private String telefonoContacto;

    @Column(name = "url_logo", length = 500)
    private String urlLogo;

    @Column(name = "url_banner", length = 500)
    private String urlBanner;

    @Column(nullable = false, length = 300)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(name = "anos_mercado", length = 50)
    private String anosMercado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "politica_comercial", columnDefinition = "TEXT")
    private String politicaComercial;

    @Column(precision = 3, scale = 2)
    private BigDecimal calificacion = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean verificado = false;

    @Column(nullable = false)
    private Boolean destacado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion = Instant.now();

    public PerfilProveedor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getIdentificacionFiscal() { return identificacionFiscal; }
    public void setIdentificacionFiscal(String identificacionFiscal) { this.identificacionFiscal = identificacionFiscal; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
    public String getUrlLogo() { return urlLogo; }
    public void setUrlLogo(String urlLogo) { this.urlLogo = urlLogo; }
    public String getUrlBanner() { return urlBanner; }
    public void setUrlBanner(String urlBanner) { this.urlBanner = urlBanner; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getAnosMercado() { return anosMercado; }
    public void setAnosMercado(String anosMercado) { this.anosMercado = anosMercado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPoliticaComercial() { return politicaComercial; }
    public void setPoliticaComercial(String politicaComercial) { this.politicaComercial = politicaComercial; }
    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal calificacion) { this.calificacion = calificacion; }
    public Boolean getVerificado() { return verificado; }
    public void setVerificado(Boolean verificado) { this.verificado = verificado; }
    public Boolean getDestacado() { return destacado; }
    public void setDestacado(Boolean destacado) { this.destacado = destacado; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
