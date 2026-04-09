package com.agora.controller.reservation;

import com.agora.dto.response.reservation.ReservationDocumentResponseDto;
import com.agora.enums.reservation.ReservationDocType;
import com.agora.service.reservation.ReservationDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Documents", description = "Pièces jointes réservation")
public class ReservationDocumentsController {

    private final ReservationDocumentService reservationDocumentService;

    @PostMapping(value = "/{reservationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ajouter un document à une réservation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReservationDocumentResponseDto> uploadDocument(
            @PathVariable UUID reservationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") ReservationDocType docType,
            Authentication authentication
    ) {
        ReservationDocumentResponseDto body =
                reservationDocumentService.uploadDocument(reservationId, file, docType, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
