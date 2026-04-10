package com.agora.controller.waitlist;

import com.agora.config.SecurityConfig;
import com.agora.dto.request.waitlist.CreateWaitlistRequestDto;
import com.agora.dto.response.waitlist.WaitlistEntryResponseDto;
import com.agora.service.auth.JwtService;
import com.agora.service.waitlist.WaitlistService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitlistController.class)
@Import({SecurityConfig.class, WaitlistControllerWebTest.TestMethodSecurityConfig.class})
@Tag("security-web")
class WaitlistControllerWebTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void list_withCitizen_returnsMine() throws Exception {
        when(waitlistService.listMine(any())).thenReturn(List.of(
                new WaitlistEntryResponseDto(
                        "w1", "Salle X", "2026-05-01", "09:00", "12:00", 1, "ACTIVE", null)));

        mockMvc.perform(get("/api/waitlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1));
    }

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void enroll_withCitizen_returnsCreated() throws Exception {
        UUID resourceId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        var req = new CreateWaitlistRequestDto(resourceId, LocalDate.of(2026, 6, 1), "14:00", "16:00");
        var created = new WaitlistEntryResponseDto(
                "new", "Salle Y", "2026-06-01", "14:00", "16:00", 2, "ACTIVE", null);
        when(waitlistService.enroll(any(CreateWaitlistRequestDto.class), any())).thenReturn(created);

        mockMvc.perform(post("/api/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void cancel_withCitizen_returnsNoContent() throws Exception {
        UUID waitlistId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        doNothing().when(waitlistService).cancel(eq(waitlistId), any());

        mockMvc.perform(delete("/api/waitlist/{waitlistId}", waitlistId))
                .andExpect(status().isNoContent());
    }
}
