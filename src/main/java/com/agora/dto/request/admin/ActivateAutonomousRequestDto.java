package com.agora.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActivateAutonomousRequestDto(
        @NotBlank @Email String email
) {
}
