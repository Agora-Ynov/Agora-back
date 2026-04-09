package com.agora.dto.request.admin;

import jakarta.validation.constraints.NotBlank;

public record UpdateTutoredUserRequestDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Integer birthYear,
        String phone,
        String notesAdmin
) {
}
