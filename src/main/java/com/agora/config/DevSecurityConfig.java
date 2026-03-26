package com.agora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resources/**").permitAll()
                        // Admin only (via @PreAuthorize sur controller)
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * Comptes DEV uniquement (Basic Auth).
     * Rôle aligné avec les @PreAuthorize des controllers.
     */
    @Bean
    public UserDetailsService devUserDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${DEV_ADMIN_USERNAME:admin}") String username,
            @Value("${DEV_ADMIN_PASSWORD:Password123!}") String password
    ) {
        UserDetails admin = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("SECRETARY_ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}

