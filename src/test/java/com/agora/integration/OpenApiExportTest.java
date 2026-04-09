package com.agora.integration;

import com.agora.testutil.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Export optionnel du JSON OpenAPI pour le client Angular (dépôt frère {@code Agora-front}).
 * <pre>
 *   ./mvnw test -Dtest=OpenApiExportTest -Dagora.export.openapi=true
 * </pre>
 * Fichier par défaut : {@code target/agora-openapi-export.json} (surcharge avec
 * {@code -Dagora.openapi.output=/chemin/vers/agora-openapi.json}).
 */
@Tag("integration-business")
@EnabledIfSystemProperty(named = "agora.export.openapi", matches = "true")
class OpenApiExportTest extends IntegrationTestBase {

    @Test
    void writeOpenApiJson_contractForFront() throws IOException {
        ResponseEntity<JsonNode> response = get("/v3/api-docs", JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String outProp = System.getProperty("agora.openapi.output", "target/agora-openapi-export.json");
        Path out = Paths.get(outProp);
        if (!out.isAbsolute()) {
            out = Paths.get(System.getProperty("user.dir")).resolve(out);
        }
        Files.createDirectories(out.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), response.getBody());

        assertThat(out).exists();
    }
}
