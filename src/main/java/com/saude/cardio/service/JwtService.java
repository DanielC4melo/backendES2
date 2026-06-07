package com.saude.cardio.service;

import com.saude.cardio.config.JwtProperties;
import com.saude.cardio.model.Usuario;
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

    private SecretKey obterChaveSecreta() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
