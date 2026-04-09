package com.agora.controller.admin;

import com.agora.config.SecurityConfig;
import com.agora.dto.response.admin.AdminGroupResponseDto;
import com.agora.enums.group.DiscountAppliesTo;
import com.agora.enums.group.DiscountType;
import com.agora.service.admin.AdminGroupCatalogService;
import com.agora.service.auth.JwtService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminGroupCatalogController.class)
@Import({SecurityConfig.class, AdminGroupCatalogControllerWebTest.TestMethodSecurityConfig.class})
@Tag("security-web")
class AdminGroupCatalogControllerWebTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminGroupCatalogService adminGroupCatalogService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void list_withSecretary_returnsGroups() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AdminGroupResponseDto dto = new AdminGroupResponseDto(
                id.toString(),
                "Conseillers municipaux",
                false,
                true,
                true,
                true,
                true,
                DiscountType.NONE,
                0,
                DiscountAppliesTo.IMMOBILIER_ONLY,
                null,
                12,
                true
        );
        when(adminGroupCatalogService.listGroups()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Conseillers municipaux"))
                .andExpect(jsonPath("$[0].councilPowers").value(true));
    }

    @Test
    @WithMockUser(username = "sec@agora.local", roles = "SECRETARY_ADMIN")
    void getOne_withSecretary_returnsGroup() throws Exception {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        AdminGroupResponseDto dto = new AdminGroupResponseDto(
                id.toString(),
                "Test",
                true,
                false,
                false,
                false,
                false,
                DiscountType.NONE,
                0,
                DiscountAppliesTo.IMMOBILIER_ONLY,
                null,
                0,
                false
        );
        when(adminGroupCatalogService.getGroup(id)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/groups/{groupId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.councilPowers").value(false));
    }

    @Test
    @WithMockUser(username = "citoyen@agora.local", roles = "CITIZEN")
    void list_asCitizen_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/groups"))
                .andExpect(status().isForbidden());
    }
}
