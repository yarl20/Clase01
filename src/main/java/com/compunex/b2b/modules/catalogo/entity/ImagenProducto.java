package com.compunex.b2b.modules.catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    @Column(nullable = false)
    private Short orden = 0;

    public ImagenProducto() {}

    public ImagenProducto(Producto producto, String urlImagen, Short orden) {
        this.producto = producto;
        this.urlImagen = urlImagen;
        this.orden = orden != null ? orden : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    public Short getOrden() { return orden; }
    public void setOrden(Short orden) { this.orden = orden; }
}
