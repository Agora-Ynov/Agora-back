package com.agora.dto.request.reservation;

import com.agora.config.FlexibleLocalTimeDeserializer;
import com.agora.enums.reservation.RecurrenceFrequency;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateRecurringReservationRequestDto(
        @NotNull UUID resourceId,
        @NotNull
        @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
        LocalTime slotStart,
        @NotNull
        @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
        LocalTime slotEnd,
        @NotBlank String purpose,
        UUID groupId,
        @NotNull RecurrenceFrequency frequency,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        List<LocalDate> excludedDates
) {
}
