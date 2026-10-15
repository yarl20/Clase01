package com.compunex.b2b.modules.auth.service.impl;

import com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO;
import com.compunex.b2b.modules.auth.dto.request.LoginRequestDTO;
import com.compunex.b2b.modules.auth.dto.response.AuthResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.CambioPasswordResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.UsuarioSesionDTO;
import com.compunex.b2b.modules.auth.service.AuthService;
import com.compunex.b2b.modules.auth.repository.UsuarioRepository;
import com.compunex.b2b.modules.auth.exception.ContrasenaActualIncorrectaException;
import com.compunex.b2b.security.JwtTokenProvider;
import com.compunex.b2b.modules.auth.dto.request.RegistroProveedorRequestDTO;
import com.compunex.b2b.modules.auth.dto.response.RegistroProveedorResponseDTO;
import com.compunex.b2b.modules.auth.entity.EstadoUsuario;
import com.compunex.b2b.modules.auth.entity.RolUsuario;
import com.compunex.b2b.modules.auth.entity.Usuario;
import com.compunex.b2b.modules.perfiles.entity.PerfilProveedor;
import com.compunex.b2b.modules.perfiles.repository.PerfilProveedorRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final PerfilProveedorRepository perfilProveedorRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                           UsuarioRepository usuarioRepository, PerfilProveedorRepository perfilProveedorRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.perfilProveedorRepository = perfilProveedorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        /*
         * =========================================================================
         * 🔴 REGISTRO HISTÓRICO - TDD FASE 1: RED (Fallo inicial obligatorio)
         * =========================================================================
         * En la Fase 1, este método lanzaba intencionalmente una excepción para
         * verificar que la prueba unitaria en AuthServiceTest fallara en rojo (Barra Roja):
         *
         * throw new UnsupportedOperationException("Fase 1 RED: Operación login no implementada aún");
         * =========================================================================
         */

        // 🟢 TDD FASE 2: GREEN - Implementación funcional que hace pasar la prueba en verde
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.contrasena())
        );

        String token = jwtTokenProvider.generarToken(authentication);

        return new AuthResponseDTO(token, "Bearer", 86400L);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioSesionDTO obtenerSesion() {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return new UsuarioSesionDTO(usuario.getCorreo(), usuario.getNombre(), usuario.getRol().name(), true);
    }

    @Override
    @Transactional
    public CambioPasswordResponseDTO cambiarPassword(CambiarPasswordDTO request) {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getHashContrasena())) {
            throw new ContrasenaActualIncorrectaException("La contraseña actual es incorrecta");
        }
        usuario.setHashContrasena(passwordEncoder.encode(request.nuevaContrasena()));
        usuario.setFechaActualizacion(Instant.now());
        usuarioRepository.save(usuario);
        return new CambioPasswordResponseDTO("Contraseña actualizada exitosamente", usuario.getFechaActualizacion());
    }

    @Override
    @Transactional
    public RegistroProveedorResponseDTO registrarProveedor(RegistroProveedorRequestDTO request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado: " + request.correo());
        }
        if (perfilProveedorRepository.existsByIdentificacionFiscal(request.ruc())) {
            throw new IllegalArgumentException("El RUC ya se encuentra registrado en la plataforma: " + request.ruc());
        }

        Usuario usuario = new Usuario(
                request.nombre(),
                request.correo(),
                passwordEncoder.encode(request.contrasena()),
                RolUsuario.PROVEEDOR,
                EstadoUsuario.ACTIVO
        );
        usuario = usuarioRepository.save(usuario);

        PerfilProveedor perfil = new PerfilProveedor();
        perfil.setUsuario(usuario);
        perfil.setIdentificacionFiscal(request.ruc());
        perfil.setRazonSocial(request.razonSocial());
        perfil.setNombreComercial(request.nombreComercial() != null && !request.nombreComercial().isBlank()
                ? request.nombreComercial()
                : request.razonSocial());
        perfil.setTelefonoContacto(request.telefonoContacto());
        perfil.setCiudad(request.ciudad());
        perfil.setDireccion(request.direccion());
        perfil.setAnosMercado(request.anosMercado() != null && !request.anosMercado().isBlank() ? request.anosMercado() : "1 año");
        perfil.setDescripcion(request.descripcion());
        perfil.setVerificado(false);
        perfil.setDestacado(false);
        perfil = perfilProveedorRepository.save(perfil);

        String token = jwtTokenProvider.generarTokenConRol(usuario.getCorreo(), RolUsuario.PROVEEDOR.name());

        return new RegistroProveedorResponseDTO(
                usuario.getId(),
                perfil.getId(),
                usuario.getUuid(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                perfil.getIdentificacionFiscal(),
                perfil.getRazonSocial(),
                token,
                "Bearer",
                86400L,
                "Proveedor registrado exitosamente en la plataforma COMPUNEX."
        );
    }
}
