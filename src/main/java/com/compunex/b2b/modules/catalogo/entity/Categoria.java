package com.compunex.b2b.modules.catalogo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String icono;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCategoria estado = EstadoCategoria.ACTIVA;

    @Column(name = "total_productos")
    private Integer totalProductos = 0;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subcategoria> subcategorias = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion = Instant.now();

    public Categoria() {}

    public Categoria(String id, String nombre, String icono, String descripcion, EstadoCategoria estado, Integer totalProductos) {
        this.id = id;
        this.nombre = nombre;
        this.icono = icono;
        this.descripcion = descripcion;
        this.estado = estado != null ? estado : EstadoCategoria.ACTIVA;
        this.totalProductos = totalProductos != null ? totalProductos : 0;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoCategoria getEstado() { return estado; }
    public void setEstado(EstadoCategoria estado) { this.estado = estado; }
    public Integer getTotalProductos() { return totalProductos; }
    public void setTotalProductos(Integer totalProductos) { this.totalProductos = totalProductos; }
    public List<Subcategoria> getSubcategorias() { return subcategorias; }
    public void setSubcategorias(List<Subcategoria> subcategorias) { this.subcategorias = subcategorias; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
