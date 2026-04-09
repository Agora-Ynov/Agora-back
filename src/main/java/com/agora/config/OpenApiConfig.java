package com.agora.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AGORA API")
                        .description("""
                                Backend de réservation de ressources pour une mairie.

                                Authentification : Bearer JWT (`Authorization: Bearer <accessToken>`), \
                                refresh HttpOnly sur `/api/auth/refresh`.

                                Contrat complet : routes citoyennes (`/api/reservations`, `/api/resources`, …), \
                                administration (`/api/admin/**`), superadmin (`/api/superadmin/**`), \
                                liste d’attente (`/api/waitlist`), activation (`/api/auth/activate`).

                                Documentation interactive : `/swagger-ui.html`, OpenAPI JSON : `/v3/api-docs`.

                                Si `/v3/api-docs` ne liste pas les chemins `/api/admin/**`, l’instance déployée \
                                n’exécute pas la même version du code que ce dépôt — reconstruire et redéployer.

                                Les balises OpenAPI (`tags`) sont définies sur les contrôleurs pour éviter les doublons \
                                et permettre la génération de clients (TypeScript, etc.).
                                """)
                        .version("1.0.0"));
    }
}
