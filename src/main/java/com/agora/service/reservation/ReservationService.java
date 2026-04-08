package com.agora.service.reservation;

import com.agora.dto.request.reservation.CreateReservationRequestDto;
import com.agora.dto.response.common.PagedResponse;
import com.agora.dto.response.reservation.ReservationDetailResponseDto;
import com.agora.dto.response.reservation.ReservationListItemDto;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface ReservationService {

    ReservationDetailResponseDto createReservation(CreateReservationRequestDto request, Authentication authentication);

    PagedResponse<ReservationListItemDto> listMyReservations(Authentication authentication, int page, int size);

    void cancelReservation(UUID reservationId, Authentication authentication);
}
