package com.saude.cardio.service;

import com.saude.cardio.config.JwtProperties;
import com.saude.cardio.exception.ExcecaoNegocio;
import com.saude.cardio.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtProperties jwtProperties;

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(usuario.getId() + ":" + usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("email", usuario.getEmail())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(obterChaveSecreta())
                .compact();
    }

    public Long extrairUsuarioIdDoToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ExcecaoNegocio(401, "Token de autenticação ausente ou inválido.");
        }

        if (!authorization.startsWith(PREFIXO_BEARER)) {
            throw new ExcecaoNegocio(401, "Token de autenticação ausente ou inválido.");
        }

        String token = authorization.substring(PREFIXO_BEARER.length()).trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(obterChaveSecreta())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("id", Long.class);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ExcecaoNegocio(401, "Token de autenticação ausente ou inválido.");
        }
    }

    private SecretKey obterChaveSecreta() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
