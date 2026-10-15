package com.compunex.b2b.modules.perfiles.repository;

import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilProveedorRepository extends JpaRepository<PerfilProveedor, Long> {

    Optional<PerfilProveedor> findByUsuarioId(Long usuarioId);

    Optional<PerfilProveedor> findByIdentificacionFiscal(String identificacionFiscal);

    boolean existsByIdentificacionFiscal(String identificacionFiscal);
}
