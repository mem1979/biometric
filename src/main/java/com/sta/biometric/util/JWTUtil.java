package com.sta.biometric.util;

import java.util.*;

import io.jsonwebtoken.*;

public class JWTUtil {

    private static final String SECRET_KEY = "mi_clave_super_secreta_123";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 horas

    public static String generarToken(String username) {
        String token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();

        System.out.println("Token generado: " + token);
        return token;
    }

    public static String validarTokenYObtenerUsuario(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
            System.out.println("Token válido. Usuario: " + claims.getSubject());
            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("Token inválido: " + e.getMessage());
            return null;
        }
    }
}
