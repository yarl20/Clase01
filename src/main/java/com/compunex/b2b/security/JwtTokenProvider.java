package com.compunex.b2b.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b}")
    private String secretKeyString;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Authentication authentication) {
        String username;
        String rol = null;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
            rol = userDetails.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        } else {
            username = authentication.getName();
            if (authentication.getAuthorities() != null) {
                rol = authentication.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse(null);
            }
        }
        if (rol != null && rol.startsWith("ROLE_")) {
            rol = rol.substring(5);
        }
        return generarTokenConRol(username, rol);
    }

    public String generarTokenDesdeCorreo(String correo) {
        return generarTokenConRol(correo, null);
    }

    public String generarTokenConRol(String correo, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(correo)
                .issuedAt(ahora)
                .expiration(expiracion);

        if (rol != null && !rol.isBlank()) {
            builder.claim("rol", rol);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String obtenerRolDesdeToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("rol", String.class);
    }

    public String obtenerCorreoDesdeToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
