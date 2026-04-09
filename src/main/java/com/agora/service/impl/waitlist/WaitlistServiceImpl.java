package com.agora.service.impl.waitlist;

import com.agora.dto.request.waitlist.CreateWaitlistRequestDto;
import com.agora.dto.response.waitlist.WaitlistEntryResponseDto;
import com.agora.entity.resource.Resource;
import com.agora.entity.user.User;
import com.agora.entity.waitlist.WaitlistEntry;
import com.agora.enums.waitlist.WaitlistStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.exception.auth.AuthUserNotFoundException;
import com.agora.exception.resource.ResourceNotFountException;
import com.agora.config.SecurityUtils;
import com.agora.repository.resource.ResourceRepository;
import com.agora.repository.user.UserRepository;
import com.agora.repository.waitlist.WaitlistEntryRepository;
import com.agora.service.waitlist.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistEntryResponseDto> listMine(Authentication authentication) {
        User user = resolveUser(authentication);
        return waitlistEntryRepository.findAllByUserIdWithResource(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WaitlistEntryResponseDto enroll(CreateWaitlistRequestDto request, Authentication authentication) {
        User user = resolveUser(authentication);
        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResourceNotFountException("Ressource introuvable."));
        if (!resource.isActive()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Ressource inactive.");
        }

        String start = request.slotStart().trim();
        String end = request.slotEnd().trim();

        if (waitlistEntryRepository.existsByUser_IdAndResource_IdAndSlotDateAndSlotStartAndSlotEnd(
                user.getId(),
                request.resourceId(),
                request.slotDate(),
                start,
                end
        )) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "Déjà inscrit sur ce créneau.");
        }

        int nextPosition = waitlistEntryRepository.maxPositionForSlot(
                request.resourceId(),
                request.slotDate(),
                start,
                end,
                WaitlistStatus.WAITING
        ) + 1;

        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(user);
        entry.setResource(resource);
        entry.setSlotDate(request.slotDate());
        entry.setSlotStart(start);
        entry.setSlotEnd(end);
        entry.setPosition(nextPosition);
        entry.setStatus(WaitlistStatus.WAITING);

        WaitlistEntry saved = waitlistEntryRepository.save(entry);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void cancel(UUID waitlistId, Authentication authentication) {
        User user = resolveUser(authentication);
        WaitlistEntry entry = waitlistEntryRepository
                .findByIdAndUser_Id(waitlistId, user.getId())
                .orElseThrow(() -> new ResourceNotFountException("Entrée liste d'attente introuvable."));
        waitlistEntryRepository.delete(entry);
    }

    private User resolveUser(Authentication authentication) {
        String subject = securityUtils.getAuthenticatedEmail(authentication);
        return userRepository.findByJwtSubject(subject)
                .orElseThrow(() -> new AuthUserNotFoundException(subject));
    }

    private WaitlistEntryResponseDto toDto(WaitlistEntry w) {
        return new WaitlistEntryResponseDto(
                w.getId().toString(),
                w.getResource().getName(),
                w.getSlotDate().toString(),
                w.getSlotStart(),
                w.getSlotEnd(),
                w.getPosition(),
                w.getStatus().name(),
                w.getNotifiedAt() != null ? w.getNotifiedAt().toString() : null
        );
    }
}
