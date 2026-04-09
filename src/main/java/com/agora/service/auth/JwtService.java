package com.agora.service.auth;

import com.agora.entity.user.ERole;
import com.agora.entity.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiresInSeconds;
    private final long refreshExpiresInSeconds;
    private final long impersonationExpiresInSeconds;
    private final String adminEmail;

    public JwtService(
            @Value("${agora.jwt.secret}") String secret,
            @Value("${agora.jwt.expires-in-seconds}") long expiresInSeconds,
            @Value("${agora.jwt.refresh-expires-in-seconds}") long refreshExpiresInSeconds,
            @Value("${agora.jwt.impersonation-expires-in-seconds:1800}") long impersonationExpiresInSeconds,
            @Value("${agora.auth.admin-email:admin@agora.local}") String adminEmail
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La configuration agora.jwt.secret est manquante");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("La configuration agora.jwt.secret doit faire au moins 32 caractères");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiresInSeconds = expiresInSeconds;
        this.refreshExpiresInSeconds = refreshExpiresInSeconds;
        this.impersonationExpiresInSeconds = Math.max(60, impersonationExpiresInSeconds);
        this.adminEmail = (adminEmail == null) ? "admin@agora.local" : adminEmail.trim();
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public long getRefreshExpiresInSeconds() {
        return refreshExpiresInSeconds;
    }

    public long getImpersonationExpiresInSeconds() {
        return impersonationExpiresInSeconds;
    }

    /**
     * Jeton court : {@code sub} = UUID du compte cible (usagers sans email). Claim {@code impersonated_by} = email admin.
     */
    public String generateImpersonationAccessToken(User targetUser, String adminEmail) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(impersonationExpiresInSeconds);
        List<String> roles = resolveRoles(targetUser);
        String emailClaim = targetUser.getEmail() != null ? targetUser.getEmail() : "";

        return Jwts.builder()
                .subject(targetUser.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", emailClaim)
                .claim("accountType", targetUser.getAccountType().name())
                .claim("accountStatus", targetUser.getAccountStatus().name())
                .claim("roles", roles)
                .claim("impersonated_by", adminEmail)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresInSeconds);
        List<String> roles = resolveRoles(user);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("accountType", user.getAccountType().name())
                .claim("accountStatus", user.getAccountStatus().name())
                .claim("roles", roles)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshExpiresInSeconds);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return Collections.emptyList();
    }

    /** Email de l’admin en cas de jeton d’impersonation (claim custom). */
    public Optional<String> extractImpersonatedBy(String token) {
        try {
            String v = parseClaims(token).get("impersonated_by", String.class);
            if (v == null || v.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(v.trim());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Object typ = claims.get("typ");
            return "refresh".equals(typ);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> resolveRoles(User user) {
        LinkedHashSet<String> roles = new LinkedHashSet<>();

        if (user.getRoles() != null) {
            if (user.getRoles().contains(ERole.SECRETARY_ADMIN)) {
                roles.add("ROLE_SECRETARY_ADMIN");
            }
            if (user.getRoles().contains(ERole.DELEGATE_ADMIN)) {
                roles.add("ROLE_DELEGATE_ADMIN");
            }
            if (user.getRoles().contains(ERole.SUPERADMIN)) {
                roles.add("ROLE_SUPERADMIN");
            }
        }

        if (isAdminEmail(user)) {
            roles.add("ROLE_SECRETARY_ADMIN");
            roles.add("ROLE_SUPERADMIN");
        }

        if (user.isAdminSupport()) {
            roles.add("ROLE_ADMIN_SUPPORT");
        }

        return List.copyOf(roles);
    }

    private boolean isAdminEmail(User user) {
        return user.getEmail() != null && user.getEmail().equalsIgnoreCase(adminEmail);
    }
}
