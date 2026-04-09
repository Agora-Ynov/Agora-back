package com.agora.dto.request.admin;

import jakarta.validation.constraints.NotBlank;

public record CreateTutoredUserRequestDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Integer birthYear,
        String phone,
        String notesAdmin
) {
}
