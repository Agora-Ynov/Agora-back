package com.agora.dto.response.calendar;

import java.util.List;

public record CalendarResponseDto(
        int year,
        int month,
        List<CalendarDayDto> days
) {
}
