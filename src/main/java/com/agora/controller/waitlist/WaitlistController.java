package com.agora.controller.waitlist;

import com.agora.dto.request.waitlist.CreateWaitlistRequestDto;
import com.agora.dto.response.waitlist.WaitlistEntryResponseDto;
import com.agora.service.waitlist.WaitlistService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@Tag(name = "Waitlist", description = "Liste d'attente citoyenne")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<WaitlistEntryResponseDto> list(Authentication authentication) {
        return waitlistService.listMine(authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public WaitlistEntryResponseDto enroll(
            @Valid @RequestBody CreateWaitlistRequestDto body,
            Authentication authentication
    ) {
        return waitlistService.enroll(body, authentication);
    }

    @DeleteMapping("/{waitlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void cancel(@PathVariable UUID waitlistId, Authentication authentication) {
        waitlistService.cancel(waitlistId, authentication);
    }
}
