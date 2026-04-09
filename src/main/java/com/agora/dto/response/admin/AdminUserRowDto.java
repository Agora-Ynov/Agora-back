package com.agora.dto.response.admin;

import java.util.List;

public record AdminUserRowDto(
        String id,
        String email,
        String firstName,
        String lastName,
        String accountType,
        String status,
        String phone,
        String internalRef,
        String notesAdmin,
        List<String> exemptions,
        String createdAt
) {
}
