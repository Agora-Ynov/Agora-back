package com.agora.dto.response.admin;

public record AdminDashboardStatsResponseDto(
        long todayReservations,
        long pendingDeposits,
        long pendingDocuments,
        long tutoredAccounts,
        long totalGroups
) {
}
