package com.agora.controller.auth;

import com.agora.dto.request.auth.ActivateAccountRequestDto;
import com.agora.dto.request.auth.LoginRequestDto;
import com.agora.dto.request.auth.RegisterRequestDto;
import com.agora.dto.response.auth.ActivationStatusResponseDto;
import com.agora.dto.response.auth.AuthMeResponseDto;
import com.agora.dto.response.auth.LoginResponseDto;
import com.agora.dto.response.auth.RegisterResponseDto;
import com.agora.exception.ApiError;
import com.agora.service.auth.AccountActivationService;
import com.agora.service.auth.AuthCookieService;
import com.agora.service.auth.AuthMeService;
import com.agora.service.auth.AuthService;
import com.agora.service.auth.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "AgoraAuth", description = "Authentification, inscription, activation, profil /me")
public class AuthController {

    private final AuthService authService;
    private final AuthMeService authMeService;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final AccountActivationService accountActivationService;

    public AuthController(
            AuthService authService,
            AuthMeService authMeService,
            JwtService jwtService,
            AuthCookieService authCookieService,
            AccountActivationService accountActivationService
    ) {
        this.authService = authService;
        this.authMeService = authMeService;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
        this.accountActivationService = accountActivationService;
    }

    @GetMapping("/activate")
    public ResponseEntity<ActivationStatusResponseDto> validateActivation(
            @RequestParam(name = "token", required = false) String token
    ) {
        return ResponseEntity.ok(accountActivationService.validateToken(token));
    }

    @PostMapping("/activate")
    public ResponseEntity<LoginResponseDto> activateAccount(@Valid @RequestBody ActivateAccountRequestDto request) {
        AuthService.LoginResult result = accountActivationService.activate(request);
        var refreshCookie = authCookieService.buildRefreshCookie(
                result.refreshToken(),
                jwtService.getRefreshExpiresInSeconds()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.response());
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthService.LoginResult result = authService.login(request);

        var refreshCookie = authCookieService.buildRefreshCookie(
                result.refreshToken(),
                jwtService.getRefreshExpiresInSeconds()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler l'access token (refresh en cookie HttpOnly)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renouvelé"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès refusé (cookie manquant/invalide)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        String cookieName = authCookieService.getCookieName();
        var cookie = WebUtils.getCookie(request, cookieName);
        String refreshToken = (cookie != null) ? cookie.getValue() : null;

        AuthService.LoginResult result = authService.refresh(refreshToken);
        var refreshCookie = authCookieService.buildRefreshCookie(
                result.refreshToken(),
                jwtService.getRefreshExpiresInSeconds()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion (supprime le cookie refresh)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Déconnecté"),
    })
    public ResponseEntity<Void> logout() {
        var clearCookie = authCookieService.clearRefreshCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
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
