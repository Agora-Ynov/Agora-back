package com.agora.service.reservation;

import com.agora.dto.response.reservation.ReservationDocumentResponseDto;
import com.agora.enums.reservation.ReservationDocType;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ReservationDocumentService {

    ReservationDocumentResponseDto uploadDocument(
            UUID reservationId,
            MultipartFile file,
            ReservationDocType docType,
            Authentication authentication
    );
}
