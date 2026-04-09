package com.agora.dto.request.admin;

import com.agora.enums.payment.PaymentMode;
import com.agora.enums.reservation.DepositStatus;
import jakarta.validation.constraints.NotNull;

public record AdminPatchPaymentRequestDto(
        @NotNull DepositStatus status,
        int amountCents,
        PaymentMode paymentMode,
        String checkNumber,
        String comment
) {
}
