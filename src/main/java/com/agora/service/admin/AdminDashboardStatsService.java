package com.agora.service.admin;

import com.agora.dto.response.admin.AdminDashboardStatsResponseDto;
import com.agora.enums.reservation.DepositStatus;
import com.agora.enums.reservation.ReservationStatus;
import com.agora.enums.user.AccountStatus;
import com.agora.enums.user.AccountType;
import com.agora.repository.group.GroupRepository;
import com.agora.repository.reservation.ReservationRepository;
import com.agora.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminDashboardStatsService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponseDto getDashboard() {
        LocalDate today = LocalDate.now();
        return new AdminDashboardStatsResponseDto(
                reservationRepository.countByReservationDate(today),
                reservationRepository.countByDepositStatus(DepositStatus.DEPOSIT_PENDING),
                reservationRepository.countByStatus(ReservationStatus.PENDING_DOCUMENT),
                userRepository.countByAccountTypeAndAccountStatus(AccountType.TUTORED, AccountStatus.ACTIVE),
                groupRepository.count()
        );
    }
}
