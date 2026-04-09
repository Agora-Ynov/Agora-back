package com.agora.controller.admin;

import com.agora.dto.request.admin.AdminPatchPaymentRequestDto;
import com.agora.dto.response.admin.AdminPaymentHistoryEntryResponseDto;
import com.agora.dto.response.admin.AdminPaymentRowResponseDto;
import com.agora.dto.response.common.PagedResponse;
import com.agora.enums.reservation.DepositStatus;
import com.agora.service.admin.AdminPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Admin Payments", description = "Cautions et paiements")
public class AdminPaymentsController {

    private final AdminPaymentService adminPaymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public PagedResponse<AdminPaymentRowResponseDto> list(
            @RequestParam(required = false) DepositStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminPaymentService.listPayments(status, dateFrom, dateTo, page, size);
    }

    @PatchMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminPaymentRowResponseDto patch(
            @PathVariable UUID reservationId,
            @Valid @RequestBody AdminPatchPaymentRequestDto body,
            Authentication authentication
    ) {
        return adminPaymentService.patchPayment(reservationId, body, authentication);
    }

    @GetMapping("/{reservationId}/history")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdminPaymentHistoryEntryResponseDto> history(@PathVariable UUID reservationId) {
        return adminPaymentService.history(reservationId);
    }
}
