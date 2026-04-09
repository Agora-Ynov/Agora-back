package com.agora.controller.admin;

import com.agora.dto.response.admin.AdminDashboardStatsResponseDto;
import com.agora.service.admin.AdminDashboardStatsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Stats", description = "Indicateurs tableau de bord")
public class AdminStatsController {

    private final AdminDashboardStatsService adminDashboardStatsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminDashboardStatsResponseDto dashboard() {
        return adminDashboardStatsService.getDashboard();
    }
}
