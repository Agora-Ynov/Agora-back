package com.agora.dto.response.calendar;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayDto(
        LocalDate date,
        boolean isBlackout,
        String blackoutReason,
        List<CalendarSlotDto> slots
) {
}
