package com.agora.service.admin;

import com.agora.dto.response.admin.AdminAuditEntryResponse;
import com.agora.dto.response.admin.AdminAuditPageResponse;
import com.agora.entity.audit.AuditLog;
import com.agora.entity.user.User;
import com.agora.repository.audit.AuditLogRepository;
import com.agora.repository.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditQueryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final ZoneId REPORTING_ZONE = ZoneId.of("Europe/Paris");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AdminAuditPageResponse list(
            int page,
            int size,
            String adminUserId,
            String targetUserId,
            Boolean impersonationOnly,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "performedAt"));

        String adminFilter = resolveUserFilter(adminUserId);
        String targetFilter = resolveUserFilter(targetUserId);

        Specification<AuditLog> spec = buildSpecification(adminFilter, targetFilter, impersonationOnly, dateFrom, dateTo);
        Page<AuditLog> p = auditLogRepository.findAll(spec, pageable);

        return new AdminAuditPageResponse(
                p.getContent().stream().map(this::toEntry).toList(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    /**
     * {@code adminUserId} / {@code targetUserId} (cahier) : email complet, sous-chaîne,
     * ou UUID utilisateur résolu vers l’email en base.
     */
    private String resolveUserFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String t = raw.trim();
        if (t.contains("@")) {
            return t;
        }
        try {
            UUID id = UUID.fromString(t);
            return userRepository.findById(id).map(User::getEmail).orElse(t);
        } catch (IllegalArgumentException e) {
            return t;
        }
    }

    private Specification<AuditLog> buildSpecification(
            String adminFilter,
            String targetFilter,
            Boolean impersonationOnly,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (adminFilter != null && !adminFilter.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("adminUser")), "%" + adminFilter.toLowerCase() + "%"));
            }
            if (targetFilter != null && !targetFilter.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("targetUser")), "%" + targetFilter.toLowerCase() + "%"));
            }
            if (Boolean.TRUE.equals(impersonationOnly)) {
                predicates.add(cb.isTrue(root.get("impersonation")));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("performedAt"),
                        dateFrom.atStartOfDay(REPORTING_ZONE).toInstant()
                ));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(
                        root.get("performedAt"),
                        dateTo.plusDays(1).atStartOfDay(REPORTING_ZONE).toInstant()
                ));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AdminAuditEntryResponse toEntry(AuditLog log) {
        Map<String, Object> detailsMap = parseDetails(log.getDetails());
        String target = log.getTargetUser() != null ? log.getTargetUser() : "";
        return new AdminAuditEntryResponse(
                log.getId().toString(),
                log.getAdminUser(),
                target.isBlank() ? null : target,
                log.getAction(),
                detailsMap,
                log.isImpersonation(),
                log.getPerformedAt()
        );
    }

    private Map<String, Object> parseDetails(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }
}
