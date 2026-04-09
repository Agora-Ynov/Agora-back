package com.agora.repository.reservation;

import com.agora.entity.reservation.DepositPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepositPaymentHistoryRepository extends JpaRepository<DepositPaymentHistory, UUID> {

    List<DepositPaymentHistory> findByReservation_IdOrderByUpdatedAtDesc(UUID reservationId);
}
