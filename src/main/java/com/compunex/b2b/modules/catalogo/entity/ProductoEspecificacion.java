package com.compunex.b2b.modules.catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_especificaciones")
public class ProductoEspecificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 100)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;

    public ProductoEspecificacion() {}

    public ProductoEspecificacion(Producto producto, String clave, String valor) {
        this.producto = producto;
        this.clave = clave;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
