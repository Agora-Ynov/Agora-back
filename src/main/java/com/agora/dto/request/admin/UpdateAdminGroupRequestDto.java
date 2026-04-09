package com.agora.dto.request.admin;

import com.agora.enums.group.DiscountAppliesTo;
import com.agora.enums.group.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAdminGroupRequestDto(
        @NotBlank String name,
        boolean canViewImmobilier,
        boolean canBookImmobilier,
        boolean canViewMobilier,
        boolean canBookMobilier,
        @NotNull DiscountType discountType,
        int discountValue,
        @NotNull DiscountAppliesTo discountAppliesTo
) {
}
