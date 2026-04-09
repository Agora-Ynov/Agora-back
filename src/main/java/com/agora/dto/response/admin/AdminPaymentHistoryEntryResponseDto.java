package com.agora.dto.response.admin;

import com.agora.enums.payment.PaymentMode;
import com.agora.enums.reservation.DepositStatus;

import java.time.Instant;

public record AdminPaymentHistoryEntryResponseDto(
        DepositStatus status,
        int amountCents,
        PaymentMode paymentMode,
        String checkNumber,
        String comment,
        String updatedByName,
        Instant updatedAt
) {
}
