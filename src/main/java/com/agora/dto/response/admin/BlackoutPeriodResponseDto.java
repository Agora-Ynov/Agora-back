package com.agora.dto.response.admin;

public record BlackoutPeriodResponseDto(
        String id,
        String resourceId,
        String resourceName,
        String dateFrom,
        String dateTo,
        String reason,
        String createdByName
) {
}
