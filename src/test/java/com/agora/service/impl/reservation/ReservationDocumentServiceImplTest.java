package com.agora.service.impl.reservation;

import com.agora.config.SecurityUtils;
import com.agora.dto.response.reservation.ReservationDocumentResponseDto;
import com.agora.entity.reservation.Reservation;
import com.agora.entity.reservation.ReservationDocument;
import com.agora.entity.user.User;
import com.agora.enums.reservation.ReservationDocType;
import com.agora.enums.reservation.ReservationDocumentStatus;
import com.agora.enums.reservation.ReservationStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.repository.reservation.ReservationDocumentRepository;
import com.agora.repository.reservation.ReservationRepository;
import com.agora.repository.user.UserRepository;
import com.agora.service.impl.audit.AuditService;
import com.agora.service.reservation.ReservationAttachmentRelay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationDocumentServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationDocumentRepository reservationDocumentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private ReservationAttachmentRelay reservationAttachmentRelay;
    @Mock
    private AuditService auditService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationDocumentServiceImpl service;

    private UUID reservationId;
    private UUID userId;
    private User owner;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        owner = new User();
        owner.setId(userId);
        owner.setEmail("owner@example.com");

        reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setUser(owner);
        reservation.setReservationDate(LocalDate.of(2026, 6, 15));
        reservation.setSlotStart(LocalTime.of(10, 0));
        reservation.setSlotEnd(LocalTime.of(11, 0));
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    @Test
    void uploadDocument_success_persistsAndRelays() throws Exception {
        when(securityUtils.getAuthenticatedEmail(authentication)).thenReturn("owner@example.com");
        when(userRepository.findByJwtSubject("owner@example.com")).thenReturn(Optional.of(owner));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statuts.pdf",
                "application/pdf",
                "hello-pdf".getBytes(StandardCharsets.UTF_8)
        );

        when(reservationDocumentRepository.save(any(ReservationDocument.class)))
                .thenAnswer(inv -> {
                    ReservationDocument d = inv.getArgument(0);
                    d.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
                    return d;
                });

        ReservationDocumentResponseDto dto = service.uploadDocument(
                reservationId,
                file,
                ReservationDocType.ASSOCIATION_PROOF,
                authentication
        );

        assertThat(dto.getDocType()).isEqualTo(ReservationDocType.ASSOCIATION_PROOF);
        assertThat(dto.getOriginalFilename()).isEqualTo("statuts.pdf");
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
        assertThat(dto.getSizeBytes()).isEqualTo(file.getSize());
        assertThat(dto.getStatus()).isEqualTo(ReservationDocumentStatus.SENT);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(reservationAttachmentRelay).relayAcceptedDocument(
                eq(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
                eq(reservationId),
                eq("owner@example.com"),
                eq(ReservationDocType.ASSOCIATION_PROOF),
                eq("statuts.pdf"),
                eq("application/pdf"),
                bytesCaptor.capture()
        );
        assertThat(bytesCaptor.getValue()).isEqualTo("hello-pdf".getBytes(StandardCharsets.UTF_8));

        verify(auditService).log(
                eq("RESERVATION_DOCUMENT_UPLOADED"),
                eq("owner@example.com"),
                isNull(),
                argThat(m ->
                        SimulatedBrevoReservationAttachmentRelay.CHANNEL.equals(m.get("relayChannel"))
                                && "cccccccc-cccc-cccc-cccc-cccccccccccc".equals(m.get("reservationDocumentId"))
                                && reservationId.toString().equals(m.get("reservationId"))),
                eq(false)
        );
    }

    @Test
    void uploadDocument_emptyFile_rejects() {
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.uploadDocument(reservationId, file, ReservationDocType.OTHER, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verifyNoInteractions(reservationAttachmentRelay);
        verifyNoInteractions(auditService);
    }

    @Test
    void uploadDocument_oversized_rejects() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", big);

        assertThatThrownBy(() -> service.uploadDocument(reservationId, file, ReservationDocType.OTHER, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCode.FILE_TOO_LARGE);

        verifyNoInteractions(reservationAttachmentRelay);
        verifyNoInteractions(auditService);
    }

    @Test
    void uploadDocument_badMime_rejects() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "x.exe",
                "application/x-msdownload",
                new byte[] {1, 2, 3}
        );

        assertThatThrownBy(() -> service.uploadDocument(reservationId, file, ReservationDocType.OTHER, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCode.INVALID_MIME_TYPE);

        verifyNoInteractions(reservationAttachmentRelay);
        verifyNoInteractions(auditService);
    }

    @Test
    void uploadDocument_notOwner_rejects() {
        when(securityUtils.getAuthenticatedEmail(authentication)).thenReturn("intrus@example.com");
        User intrus = new User();
        intrus.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        intrus.setEmail("intrus@example.com");
        when(userRepository.findByJwtSubject("intrus@example.com")).thenReturn(Optional.of(intrus));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[] {1});

        assertThatThrownBy(() -> service.uploadDocument(reservationId, file, ReservationDocType.OTHER, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verifyNoInteractions(reservationAttachmentRelay);
        verifyNoInteractions(auditService);
    }
}
