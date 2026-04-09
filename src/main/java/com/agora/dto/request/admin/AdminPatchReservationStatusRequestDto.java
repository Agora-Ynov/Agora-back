package com.agora.dto.request.admin;

import com.agora.enums.reservation.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record AdminPatchReservationStatusRequestDto(
        @NotNull ReservationStatus status,
        String comment
) {
}
