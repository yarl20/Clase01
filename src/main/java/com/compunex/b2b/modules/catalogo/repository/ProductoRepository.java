package com.compunex.b2b.modules.catalogo.repository;

import com.compunex.b2b.modules.catalogo.entity.EstadoProducto;
import com.compunex.b2b.modules.catalogo.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findAllByProveedorIdAndEstadoNot(Long proveedorId, EstadoProducto estado, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.proveedor.id = :proveedorId AND p.estado <> :estado AND LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%'))")
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> buscarPorProveedorYTitulo(
            @Param("proveedorId") Long proveedorId,
            @Param("estado") EstadoProducto estado,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> findAllByEstadoNot(EstadoProducto estado, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.estado <> :estado AND LOWER(p.titulo) LIKE LOWER(CONCAT('%', :search, '%'))")
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> buscarTodosPorTitulo(
            @Param("estado") EstadoProducto estado,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<Producto> findByUuid(UUID uuid);

    Optional<Producto> findByIdAndProveedorId(Long id, Long proveedorId);
}
