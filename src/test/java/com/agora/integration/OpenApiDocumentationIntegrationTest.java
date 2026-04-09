package com.agora.integration;

import com.agora.testutil.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garantit que l’OpenAPI Springdoc couvre bien l’API publique et admin (contrat front).
 */
class OpenApiDocumentationIntegrationTest extends IntegrationTestBase {

    @Test
    void v3ApiDocs_exposesAdminAuthAndCitizenRoutes() {
        ResponseEntity<JsonNode> response = get("/v3/api-docs", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();

        JsonNode paths = body.path("paths");
        assertThat(paths.isObject()).isTrue();

        assertThat(paths.has("/api/admin/reservations")).isTrue();
        assertThat(paths.has("/api/admin/reservations/{reservationId}/status")).isTrue();
        assertThat(paths.has("/api/admin/stats/dashboard")).isTrue();
        assertThat(paths.has("/api/admin/users")).isTrue();
        assertThat(paths.has("/api/admin/payments")).isTrue();
        assertThat(paths.has("/api/admin/audit")).isTrue();
        assertThat(paths.has("/api/superadmin/admin-support")).isTrue();

        assertThat(paths.has("/api/auth/me")).isTrue();
        assertThat(paths.has("/api/auth/activate")).isTrue();
        assertThat(paths.has("/api/waitlist")).isTrue();
        assertThat(paths.has("/api/calendar")).isTrue();

        JsonNode schemas = body.path("components").path("schemas");
        assertThat(schemas.path("AuthMeResponseDto").path("properties").has("adminSupport"))
                .as("AuthMeResponseDto.adminSupport pour le front")
                .isTrue();
    }
}
