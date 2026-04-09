package com.agora.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Jeton d'accès court pour session d'impersonation (comptes TUTORED)")
public record ImpersonationTokenResponseDto(
        @Schema(description = "JWT access ; le sub est l'UUID du compte cible")
        String accessToken,
        @Schema(description = "Durée de vie du jeton en secondes")
        long expiresInSeconds
) {
}
