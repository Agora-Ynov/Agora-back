package com.agora.service.impl.reservation;

import com.agora.dto.response.reservation.ReservationDocumentResponseDto;
import com.agora.entity.reservation.Reservation;
import com.agora.entity.reservation.ReservationDocument;
import com.agora.enums.reservation.ReservationDocType;
import com.agora.enums.reservation.ReservationDocumentStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.config.SecurityUtils;
import com.agora.exception.auth.AuthUserNotFoundException;
import com.agora.repository.reservation.ReservationDocumentRepository;
import com.agora.repository.reservation.ReservationRepository;
import com.agora.repository.user.UserRepository;
import com.agora.entity.user.User;
import com.agora.service.impl.audit.AuditService;
import com.agora.service.reservation.ReservationAttachmentRelay;
import com.agora.service.reservation.ReservationDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDocumentServiceImpl implements ReservationDocumentService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_MIMES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final ReservationRepository reservationRepository;
    private final ReservationDocumentRepository reservationDocumentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ReservationAttachmentRelay reservationAttachmentRelay;
    private final AuditService auditService;

    @Override
    @Transactional
    public ReservationDocumentResponseDto uploadDocument(
            UUID reservationId,
            MultipartFile file,
            ReservationDocType docType,
            Authentication authentication
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Fichier obligatoire");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, "Le fichier dépasse 5 Mo");
        }

        String contentType = Optional.ofNullable(file.getContentType()).orElse("").trim().toLowerCase();
        if (!ALLOWED_MIMES.contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_MIME_TYPE,
                    "Format non accepté : " + contentType
            );
        }

        String subject = securityUtils.getAuthenticatedEmail(authentication);
        User user = userRepository.findByJwtSubject(subject)
                .orElseThrow(() -> new AuthUserNotFoundException(subject));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Réservation introuvable"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Accès interdit à cette réservation");
        }

        final byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Lecture du fichier impossible.");
        }

        ReservationDocument doc = new ReservationDocument();
        doc.setReservation(reservation);
        doc.setDocType(docType);
        doc.setOriginalFilename(Optional.ofNullable(file.getOriginalFilename()).orElse("document"));
        doc.setMimeType(contentType);
        doc.setSizeBytes(file.getSize());
        doc.setStatus(ReservationDocumentStatus.SENT);
        doc.setSentAt(Instant.now());

        ReservationDocument saved = reservationDocumentRepository.save(doc);

        reservationAttachmentRelay.relayAcceptedDocument(
                saved.getId(),
                reservation.getId(),
                user.getEmail(),
                docType,
                saved.getOriginalFilename(),
                saved.getMimeType(),
                content
        );

        auditService.log(
                "RESERVATION_DOCUMENT_UPLOADED",
                user.getEmail(),
                null,
                Map.of(
                        "reservationId", reservation.getId().toString(),
                        "reservationDocumentId", saved.getId().toString(),
                        "docType", docType.name(),
                        "originalFilename", saved.getOriginalFilename(),
                        "mimeType", saved.getMimeType(),
                        "sizeBytes", saved.getSizeBytes(),
                        "relayChannel", SimulatedBrevoReservationAttachmentRelay.CHANNEL
                ),
                false
        );

        return new ReservationDocumentResponseDto(
                saved.getId(),
                saved.getDocType(),
                saved.getOriginalFilename(),
                saved.getMimeType(),
                saved.getSizeBytes(),
                saved.getStatus(),
                saved.getSentAt()
        );
    }

}
