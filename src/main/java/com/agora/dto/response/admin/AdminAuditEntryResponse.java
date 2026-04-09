package com.agora.dto.response.admin;

import java.time.Instant;
import java.util.Map;

public record AdminAuditEntryResponse(
        String id,
        String adminName,
        String targetName,
        String action,
        Map<String, Object> details,
        boolean isImpersonation,
        Instant performedAt
) {
}
