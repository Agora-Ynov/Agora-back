package com.agora.config;

import com.agora.exception.ApiError;
import com.agora.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sécurité DEV uniquement.
 *
 * Objectif: permettre au front de tester l'API tant que le JWT n'est pas branché
 * (pas de filtre d'auth, pas de validation du Bearer).
 *
 * À retirer / durcir quand le ticket JWT sera prêt.
 */
@Profile({"dev", "local", "seed"})
@Configuration
public class DevSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain devSecurityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resources/**").permitAll()
                        // Admin only (via @PreAuthorize sur controller), auth via Bearer JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String traceId = MDC.get("traceId");
                    String correlationId = MDC.get("correlationId");
                    ApiError body = new ApiError(
                            ErrorCode.ACCESS_DENIED,
                            null,
                            request.getRequestURI(),
                            traceId,
                            correlationId
                    );
                    objectMapper.writeValue(response.getOutputStream(), body);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String traceId = MDC.get("traceId");
                    String correlationId = MDC.get("correlationId");
                    ApiError body = new ApiError(
                            ErrorCode.ACCESS_DENIED,
                            null,
                            request.getRequestURI(),
                            traceId,
                            correlationId
                    );
                    objectMapper.writeValue(response.getOutputStream(), body);
                })
        );

        return http.build();
    }
}

