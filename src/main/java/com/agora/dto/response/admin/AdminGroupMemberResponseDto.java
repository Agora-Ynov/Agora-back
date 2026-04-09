package com.agora.dto.response.admin;

public record AdminGroupMemberResponseDto(
        String userId,
        String firstName,
        String lastName,
        String email,
        String role,
        String joinedAt
) {
}
