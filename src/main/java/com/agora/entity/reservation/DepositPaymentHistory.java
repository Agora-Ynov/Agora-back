package com.agora.entity.reservation;

import com.agora.enums.payment.PaymentMode;
import com.agora.enums.reservation.DepositStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deposit_payment_history")
@Getter
@Setter
@NoArgsConstructor
public class DepositPaymentHistory {

    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DepositStatus status;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 50)
    private PaymentMode paymentMode;

    @Column(name = "check_number", length = 100)
    private String checkNumber;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "updated_by_name", length = 255)
    private String updatedByName;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private Instant updatedAt;
}
