package com.agora.dto.request.waitlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateWaitlistRequestDto(
        @NotNull UUID resourceId,
        @NotNull LocalDate slotDate,
        @NotBlank String slotStart,
        @NotBlank String slotEnd
) {
}
