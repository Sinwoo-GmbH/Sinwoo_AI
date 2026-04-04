package com.sinwoo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final SecurityProperties securityProperties;
    private final SecretKey secretKey;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.secretKey = Keys.hmacShaKeyFor(hashSecret(securityProperties.jwtSecret()));
    }

    public String issueAccessToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(securityProperties.accessTokenTtlSeconds());

        return Jwts.builder()
                .subject(String.valueOf(user.usrId()))
                .claim("typ", "ACCESS")
                .claim("usrId", user.usrId())
                .claim("tenantId", user.tenantId())
                .claim("coId", user.coId())
                .claim("lgnId", user.lgnId())
                .claim("eml", user.eml())
                .claim("dspNm", user.dspNm())
                .claim("authGrpCd", user.authGrpCd())
                .claim("authLvlCd", user.authLvlCd())
                .claim("roleCds", user.roleCds())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String issueRefreshToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(securityProperties.refreshTokenTtlSeconds());

        return Jwts.builder()
                .subject(String.valueOf(user.usrId()))
                .claim("typ", "REFRESH")
                .claim("usrId", user.usrId())
                .claim("tenantId", user.tenantId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"ACCESS".equals(claims.get("typ", String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }

        Object roleClaim = claims.get("roleCds");
        List<String> roleCds = roleClaim instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();

        return new AuthenticatedUser(
                claims.get("usrId", Long.class),
                claims.get("tenantId", Long.class),
                claims.get("coId", Long.class),
                claims.get("lgnId", String.class),
                claims.get("eml", String.class),
                claims.get("dspNm", String.class),
                claims.get("authGrpCd", String.class),
                claims.get("authLvlCd", String.class),
                roleCds
        );
    }

    public long getAccessTokenTtlSeconds() {
        return securityProperties.accessTokenTtlSeconds();
    }

    public long getRefreshTokenTtlSeconds() {
        return securityProperties.refreshTokenTtlSeconds();
    }

    private byte[] hashSecret(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build JWT signing key", ex);
        }
    }
}
