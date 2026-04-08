package com.agora.dto.response.calendar;

import com.agora.enums.resource.ResourceType;

import java.util.UUID;

public record CalendarSlotDto(
        UUID resourceId,
        String resourceName,
        ResourceType resourceType,
        String slotStart,
        String slotEnd,
        boolean isAvailable
) {
}
