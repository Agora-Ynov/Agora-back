package com.agora.dto.request.reservation;

import com.agora.config.FlexibleLocalTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateReservationRequestDto(
        @NotNull
        UUID resourceId,
        @NotNull
        LocalDate date,
        @NotNull
        @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
        LocalTime slotStart,
        @NotNull
        @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
        LocalTime slotEnd,
        @NotBlank
        String purpose,
        UUID groupId
) {
}
