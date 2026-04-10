package com.agora.controller.admin;

import com.agora.config.SecurityConfig;
import com.agora.dto.request.admin.AdminPatchReservationStatusRequestDto;
import com.agora.dto.response.common.PagedResponse;
import com.agora.dto.response.reservation.AdminReservationListStatsResponseDto;
import com.agora.dto.response.reservation.ReservationSummaryResponseDto;
import com.agora.enums.reservation.ReservationStatus;
import com.agora.service.admin.AdminReservationOperationsService;
import com.agora.service.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReservationsController.class)
@Import({SecurityConfig.class, AdminReservationsControllerWebTest.TestMethodSecurityConfig.class})
@Tag("security-web")
class AdminReservationsControllerWebTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminReservationOperationsService adminReservationOperationsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void list_withSecretaryRole_returnsOk() throws Exception {
        PagedResponse<ReservationSummaryResponseDto> page = new PagedResponse<>(List.of(), 0, 0, 0, 20);
        when(adminReservationOperationsService.listReservations(
                any(), any(), any(), any(), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void stats_withSecretaryRole_returnsOk() throws Exception {
        var stats = new AdminReservationListStatsResponseDto(1L, 0L, 1L, 0L, 0L, 0L, 0L);
        when(adminReservationOperationsService.reservationListStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/reservations/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.confirmed").value(1));
    }

    @Test
    @WithMockUser(username = "support@agora.local", roles = "ADMIN_SUPPORT")
    void patchStatus_withAdminSupport_delegatesToService() throws Exception {
        UUID reservationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        doNothing().when(adminReservationOperationsService)
                .patchStatus(eq(reservationId), any(AdminPatchReservationStatusRequestDto.class), any());

        mockMvc.perform(patch("/api/admin/reservations/{rid}/status", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AdminPatchReservationStatusRequestDto(ReservationStatus.CONFIRMED, null))))
                .andExpect(status().isOk());

        verify(adminReservationOperationsService)
                .patchStatus(eq(reservationId), any(AdminPatchReservationStatusRequestDto.class), any());
    }

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void list_asCitizen_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isForbidden());
    }
}
