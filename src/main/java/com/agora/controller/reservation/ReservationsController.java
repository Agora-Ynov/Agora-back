package com.agora.controller.reservation;

import com.agora.dto.request.reservation.CreateReservationRequestDto;
import com.agora.dto.response.common.PagedResponse;
import com.agora.dto.response.reservation.ReservationDetailResponseDto;
import com.agora.dto.response.reservation.ReservationListItemDto;
import com.agora.service.reservation.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Création et suivi des réservations")
public class ReservationsController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Mes réservations", description = "Liste paginée des réservations de l'utilisateur connecté.")
    public PagedResponse<ReservationListItemDto> listMyReservations(
            Authentication authentication,
            @Parameter(description = "Index page (0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille (max 100)") @RequestParam(defaultValue = "20") int size
    ) {
        return reservationService.listMyReservations(authentication, page, size);
    }

    @PostMapping
    @Operation(summary = "Créer une réservation")
    public ResponseEntity<ReservationDetailResponseDto> createReservation(
            @Valid @RequestBody CreateReservationRequestDto request,
            Authentication authentication
    ) {
        ReservationDetailResponseDto response = reservationService.createReservation(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Annuler une réservation")
    public void cancelReservation(
            @PathVariable UUID reservationId,
            Authentication authentication
    ) {
        reservationService.cancelReservation(reservationId, authentication);
    }
}
