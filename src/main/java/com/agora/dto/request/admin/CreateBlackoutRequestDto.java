package com.agora.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBlackoutRequestDto(
        UUID resourceId,
        @NotNull LocalDate dateFrom,
        @NotNull LocalDate dateTo,
        @NotBlank String reason
) {
}
