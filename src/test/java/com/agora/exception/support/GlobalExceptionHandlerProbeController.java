package com.agora.exception.support;

import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur de test uniquement (classpath test) pour exercer {@link com.agora.exception.GlobalExceptionHandler}
 * via {@link org.springframework.test.web.servlet.MockMvc} standalone.
 */
@RestController
@RequestMapping("/__probe/exceptions")
public class GlobalExceptionHandlerProbeController {

    public record ValidBody(@NotBlank String name) {}

    @GetMapping("/business")
    public void business() {
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @PostMapping(value = "/valid", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void validPost(@Valid @RequestBody ValidBody body) {
        // non atteint si corps invalide
    }

    @GetMapping("/illegal-tag")
    public void illegalTag() {
        throw new IllegalArgumentException("Tag invalide: X");
    }

    @GetMapping("/illegal-other")
    public void illegalOther() {
        throw new IllegalArgumentException("autre");
    }

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("dup");
    }

    @GetMapping("/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("no");
    }

    @GetMapping("/auth")
    public void auth() {
        throw new BadCredentialsException("bad");
    }

    @GetMapping("/generic")
    public void generic() {
        throw new RuntimeException("boom");
    }
}
