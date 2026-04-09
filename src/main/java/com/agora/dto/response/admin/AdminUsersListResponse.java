package com.agora.dto.response.admin;

import java.util.List;

public record AdminUsersListResponse(
        List<AdminUserRowDto> content,
        long totalElements,
        int totalPages
) {
}
