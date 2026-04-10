package com.agora.controller.admin;

import com.agora.config.SecurityConfig;
import com.agora.dto.request.admin.CreateBlackoutRequestDto;
import com.agora.dto.response.admin.BlackoutPeriodResponseDto;
import com.agora.service.admin.AdminBlackoutService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBlackoutController.class)
@Import({SecurityConfig.class, AdminBlackoutControllerWebTest.TestMethodSecurityConfig.class})
@Tag("security-web")
class AdminBlackoutControllerWebTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminBlackoutService adminBlackoutService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void list_withSecretary_returnsOk() throws Exception {
        when(adminBlackoutService.listAll()).thenReturn(List.of(
                new BlackoutPeriodResponseDto(
                        "b1", "r1", "Salle", "2026-04-10", "2026-04-12", "Travaux", "Admin")));

        mockMvc.perform(get("/api/admin/blackouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Travaux"));
    }

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void create_withSecretary_returnsCreated() throws Exception {
        UUID resourceId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        var body = new CreateBlackoutRequestDto(resourceId, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 2), "Férié");
        var created = new BlackoutPeriodResponseDto(
                "new-id", resourceId.toString(), "Salle A", "2026-04-01", "2026-04-02", "Férié", "Secrétaire");
        when(adminBlackoutService.create(any(CreateBlackoutRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/blackouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("Férié"));
    }

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void delete_withSecretary_returnsNoContent() throws Exception {
        UUID blackoutId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        doNothing().when(adminBlackoutService).delete(blackoutId);

        mockMvc.perform(delete("/api/admin/blackouts/{blackoutId}", blackoutId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void list_asCitizen_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/blackouts"))
                .andExpect(status().isForbidden());
    }
}
