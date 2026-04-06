package com.climbe.api_climbe.service;

import com.climbe.api_climbe.config.PropriedadesJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class TokenJwtService {

    private final PropriedadesJwt propriedadesJwt;

    public TokenJwtService(PropriedadesJwt propriedadesJwt) {
        this.propriedadesJwt = propriedadesJwt;
    }

    public String gerarToken(String subject, Map<String, Object> claimsExtras, List<String> authorities) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plusSeconds(propriedadesJwt.getExpiracaoMinutos() * 60L);

        return Jwts.builder()
                .subject(subject)
                .issuer(propriedadesJwt.getIssuer())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .claim("authorities", authorities)
                .claims(claimsExtras)
                .signWith(chaveAssinatura())
                .compact();
    }

    public boolean tokenValido(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extrairSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims extrairClaims(String token) {
        return parseClaims(token);
    }

    public long segundosExpiracao() {
        return propriedadesJwt.getExpiracaoMinutos() * 60L;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey chaveAssinatura() {
        byte[] original = propriedadesJwt.getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] segura = Arrays.copyOf(original, 32);
        return Keys.hmacShaKeyFor(segura);
    }
}
