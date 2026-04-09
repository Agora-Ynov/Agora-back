package com.agora.dto.response.reservation;

import com.agora.enums.reservation.ReservationDocType;
import com.agora.enums.reservation.ReservationDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationDocumentResponseDto {

    private final UUID id;
    private final ReservationDocType docType;
    private final String originalFilename;
    private final String mimeType;
    private final long sizeBytes;
    private final ReservationDocumentStatus status;
    private final Instant sentAt;
}
