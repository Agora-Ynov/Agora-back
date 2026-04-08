package com.agora.service.calendar;

import com.agora.dto.response.calendar.CalendarResponseDto;

import java.util.UUID;

public interface CalendarService {

    CalendarResponseDto getMonthlyCalendar(int year, int month, UUID resourceId);
}
