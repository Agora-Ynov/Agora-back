package com.agora.exception;

import com.agora.exception.support.GlobalExceptionHandlerProbeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerWebTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GlobalExceptionHandlerProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleBusiness_shouldMapCode() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.code()));
    }

    @Test
    void handleValidation_shouldReturnFieldErrors() throws Exception {
        mockMvc.perform(post("/__probe/exceptions/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.code()));
    }

    @Test
    void handleIllegalArgument_tagInvalide_shouldUseResourceTagInvalid() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/illegal-tag"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_TAG_INVALID.code()));
    }

    @Test
    void handleIllegalArgument_other_shouldUseValidationError() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/illegal-other"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.code()));
    }

    @Test
    void handleDataIntegrity_shouldMapConflict() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_CONFLICT.code()));
    }

    @Test
    void handleAccessDenied_shouldMapForbidden() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.code()));
    }

    @Test
    void handleAuthentication_shouldMapUnauthorized() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_REQUIRED.code()));
    }

    @Test
    void handleGeneric_shouldMapApiUnavailable() throws Exception {
        mockMvc.perform(get("/__probe/exceptions/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.API_UNAVAILABLE.code()));
    }
}
