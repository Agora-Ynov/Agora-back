package com.agora.controller.auth;

import com.agora.dto.request.auth.LoginRequestDto;
import com.agora.dto.request.auth.RegisterRequestDto;
import com.agora.dto.response.auth.AuthMeResponseDto;
import com.agora.dto.response.auth.LoginResponseDto;
import com.agora.dto.response.auth.RegisterResponseDto;
import com.agora.exception.ApiError;
import com.agora.service.auth.AuthMeService;
import com.agora.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthMeService authMeService;

    public AuthController(AuthService authService, AuthMeService authMeService) {
        this.authService = authService;
        this.authMeService = authMeService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur connecté", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil récupéré"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès refusé (token manquant/invalide)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<AuthMeResponseDto> me(Authentication authentication) {
        AuthMeResponseDto response = authMeService.getCurrentUserProfile(authentication);
        return ResponseEntity.ok(response);
    }
}
