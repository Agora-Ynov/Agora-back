package com.agora.controller.admin;

import com.agora.dto.request.admin.CreateBlackoutRequestDto;
import com.agora.dto.response.admin.BlackoutPeriodResponseDto;
import com.agora.service.admin.AdminBlackoutService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/blackouts")
@RequiredArgsConstructor
@Tag(name = "Admin Blackouts", description = "Fermetures exceptionnelles")
public class AdminBlackoutController {

    private final AdminBlackoutService adminBlackoutService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public List<BlackoutPeriodResponseDto> list() {
        return adminBlackoutService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public BlackoutPeriodResponseDto create(@Valid @RequestBody CreateBlackoutRequestDto request) {
        return adminBlackoutService.create(request);
    }

    @DeleteMapping("/{blackoutId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(@PathVariable UUID blackoutId) {
        adminBlackoutService.delete(blackoutId);
    }
}
