package com.agora.service.admin;

import com.agora.dto.request.admin.CreateBlackoutRequestDto;
import com.agora.dto.response.admin.BlackoutPeriodResponseDto;
import com.agora.entity.calendar.BlackoutPeriod;
import com.agora.entity.resource.Resource;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.config.SecurityUtils;
import com.agora.repository.calendar.BlackoutPeriodRepository;
import com.agora.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminBlackoutService {

    private final BlackoutPeriodRepository blackoutPeriodRepository;
    private final ResourceRepository resourceRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<BlackoutPeriodResponseDto> listAll() {
        return blackoutPeriodRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public BlackoutPeriodResponseDto create(CreateBlackoutRequestDto request) {
        if (request.dateTo().isBefore(request.dateFrom())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "dateTo doit être >= dateFrom");
        }

        BlackoutPeriod entity = new BlackoutPeriod();
        entity.setDateFrom(request.dateFrom());
        entity.setDateTo(request.dateTo());
        entity.setReason(request.reason().trim());

        if (request.resourceId() != null) {
            Resource resource = resourceRepository.findById(request.resourceId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Ressource introuvable"));
            entity.setResource(resource);
        }

        entity.setCreatedByName(
                securityUtils.tryGetAuthenticatedEmail().orElse("Admin")
        );

        BlackoutPeriod saved = blackoutPeriodRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!blackoutPeriodRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Fermeture introuvable");
        }
        blackoutPeriodRepository.deleteById(id);
    }

    private BlackoutPeriodResponseDto toDto(BlackoutPeriod b) {
        return new BlackoutPeriodResponseDto(
                b.getId().toString(),
                b.getResource() != null ? b.getResource().getId().toString() : null,
                b.getResource() != null ? b.getResource().getName() : null,
                b.getDateFrom().toString(),
                b.getDateTo().toString(),
                b.getReason(),
                b.getCreatedByName() != null ? b.getCreatedByName() : ""
        );
    }
}
