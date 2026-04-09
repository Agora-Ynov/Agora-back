package com.agora.dto.response.admin;

import com.agora.enums.payment.PaymentMode;
import com.agora.enums.reservation.DepositStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminPaymentRowResponseDto(
        UUID reservationId,
        DepositStatus status,
        int amountCents,
        PaymentMode paymentMode,
        String checkNumber,
        String comment,
        String updatedByName,
        Instant updatedAt
) {
}
