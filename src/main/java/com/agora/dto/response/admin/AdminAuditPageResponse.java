package com.agora.dto.response.admin;

import java.util.List;

public record AdminAuditPageResponse(
        List<AdminAuditEntryResponse> content,
        long totalElements,
        int totalPages
) {
}
