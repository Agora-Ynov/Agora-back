package com.agora.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountRequestDto(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 128) String newPassword
) {
}
