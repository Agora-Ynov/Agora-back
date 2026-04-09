package com.agora.controller.reservation;

import com.agora.config.SecurityConfig;
import com.agora.dto.response.reservation.ReservationDocumentResponseDto;
import com.agora.enums.reservation.ReservationDocType;
import com.agora.enums.reservation.ReservationDocumentStatus;
import com.agora.service.auth.JwtService;
import com.agora.service.reservation.ReservationDocumentService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationDocumentsController.class)
@Import(SecurityConfig.class)
@Tag("security-web")
class ReservationDocumentsControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationDocumentService reservationDocumentService;

    @MockBean
    private JwtService jwtService;

    @Test
    void uploadDocument_withoutAuth_shouldBeUnauthorized() throws Exception {
        UUID reservationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 test".getBytes()
        );

        mockMvc.perform(multipart("/api/reservations/{reservationId}/documents", reservationId)
                        .file(file)
                        .param("docType", "OTHER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void uploadDocument_withAuth_shouldReturn201() throws Exception {
        UUID reservationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID docId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statuts.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 test".getBytes()
        );

        when(reservationDocumentService.uploadDocument(
                eq(reservationId),
                any(),
                eq(ReservationDocType.ASSOCIATION_PROOF),
                any()
        )).thenReturn(new ReservationDocumentResponseDto(
                docId,
                ReservationDocType.ASSOCIATION_PROOF,
                "statuts.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                12,
                ReservationDocumentStatus.SENT,
                Instant.parse("2026-06-01T10:00:00Z")
        ));

        mockMvc.perform(multipart("/api/reservations/{reservationId}/documents", reservationId)
                        .file(file)
                        .param("docType", "ASSOCIATION_PROOF"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .andExpect(jsonPath("$.docType").value("ASSOCIATION_PROOF"))
                .andExpect(jsonPath("$.originalFilename").value("statuts.pdf"))
                .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(12))
                .andExpect(jsonPath("$.status").value("SENT"));
    }
}
