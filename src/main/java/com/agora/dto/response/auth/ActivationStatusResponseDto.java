package com.agora.dto.response.auth;

/**
 * GET /api/auth/activate — conforme cahier.
 */
public record ActivationStatusResponseDto(boolean valid, String targetEmail) {
}
