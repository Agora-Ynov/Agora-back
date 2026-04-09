package com.agora.service.auth;

import com.agora.dto.request.auth.ActivateAccountRequestDto;
import com.agora.dto.response.auth.ActivationStatusResponseDto;
import com.agora.dto.response.auth.LoginResponseDto;
import com.agora.entity.auth.AccountActivationToken;
import com.agora.entity.user.User;
import com.agora.enums.user.AccountType;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.mapper.auth.UserMapper;
import com.agora.repository.auth.AccountActivationTokenRepository;
import com.agora.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final AccountActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ActivationStatusResponseDto validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return new ActivationStatusResponseDto(false, null);
        }
        return tokenRepository.findByToken(rawToken.trim())
                .filter(t -> t.getUsedAt() == null)
                .filter(t -> !t.getExpiresAt().isBefore(Instant.now()))
                .map(t -> {
                    String email = t.getUser().getEmail();
                    if (email == null || email.isBlank()) {
                        return new ActivationStatusResponseDto(false, null);
                    }
                    return new ActivationStatusResponseDto(true, email);
                })
                .orElse(new ActivationStatusResponseDto(false, null));
    }

    @Transactional
    public AuthService.LoginResult activate(ActivateAccountRequestDto request) {
        AccountActivationToken t = tokenRepository
                .findByToken(request.token().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "Token d'activation invalide."));
        if (t.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.ACTIVATION_TOKEN_USED);
        }
        if (t.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.ACTIVATION_TOKEN_EXPIRED);
        }
        User user = t.getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Aucun email n'est associé à ce compte.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        if (user.getAccountType() == AccountType.TUTORED) {
            user.setAccountType(AccountType.AUTONOMOUS);
        }
        userRepository.save(user);

        t.setUsedAt(Instant.now());
        tokenRepository.save(t);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        LoginResponseDto response = new LoginResponseDto(
                accessToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                userMapper.toUserSummary(user)
        );
        return new AuthService.LoginResult(response, refreshToken);
    }

    /**
     * Invalide les jetons ouverts et en crée un nouveau (lien valable 72h). Retourne le jeton en clair.
     */
    @Transactional
    public String issueFreshToken(User user) {
        tokenRepository.markAllUnusedAsRevoked(user.getId(), Instant.now());
        AccountActivationToken t = new AccountActivationToken();
        t.setToken(UUID.randomUUID().toString());
        t.setUser(user);
        t.setExpiresAt(Instant.now().plus(72, ChronoUnit.HOURS));
        tokenRepository.save(t);
        return t.getToken();
    }
}
