package com.agora.service.waitlist;

import com.agora.dto.request.waitlist.CreateWaitlistRequestDto;
import com.agora.dto.response.waitlist.WaitlistEntryResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface WaitlistService {

    List<WaitlistEntryResponseDto> listMine(Authentication authentication);

    WaitlistEntryResponseDto enroll(CreateWaitlistRequestDto request, Authentication authentication);

    void cancel(UUID waitlistId, Authentication authentication);
}
