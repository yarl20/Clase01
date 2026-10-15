package com.compunex.b2b.modules.catalogo.repository;

import com.compunex.b2b.modules.catalogo.entity.Categoria;
import com.compunex.b2b.modules.catalogo.entity.EstadoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    List<Categoria> findByEstado(EstadoCategoria estado);
}
