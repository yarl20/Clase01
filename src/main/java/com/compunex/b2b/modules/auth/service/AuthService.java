package com.compunex.b2b.modules.auth.service;

import com.compunex.b2b.modules.auth.dto.request.CambiarPasswordDTO;
import com.compunex.b2b.modules.auth.dto.request.LoginRequestDTO;
import com.compunex.b2b.modules.auth.dto.request.RegistroProveedorRequestDTO;
import com.compunex.b2b.modules.auth.dto.response.AuthResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.CambioPasswordResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.RegistroProveedorResponseDTO;
import com.compunex.b2b.modules.auth.dto.response.UsuarioSesionDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    UsuarioSesionDTO obtenerSesion();
    CambioPasswordResponseDTO cambiarPassword(CambiarPasswordDTO request);
    RegistroProveedorResponseDTO registrarProveedor(RegistroProveedorRequestDTO request);
}
