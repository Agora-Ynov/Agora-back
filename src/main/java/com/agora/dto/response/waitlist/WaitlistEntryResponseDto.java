package com.agora.dto.response.waitlist;

public record WaitlistEntryResponseDto(
        String id,
        String resourceName,
        String slotDate,
        String slotStart,
        String slotEnd,
        int position,
        String status,
        String notifiedAt
) {
}
