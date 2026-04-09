package com.agora.service.auth;

import com.agora.dto.response.auth.AuthMeResponseDto;
import com.agora.dto.response.auth.UserGroupSummaryDto;
import com.agora.dto.response.auth.UserGroupSummaryMapping;
import com.agora.entity.group.GroupMembership;
import com.agora.entity.user.User;
import com.agora.exception.auth.AuthUserNotFoundException;
import com.agora.config.SecurityUtils;
import com.agora.repository.group.GroupMembershipRepository;
import com.agora.repository.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthMeService {

    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final SecurityUtils securityUtils;

    public AuthMeService(
            UserRepository userRepository,
            GroupMembershipRepository groupMembershipRepository,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public AuthMeResponseDto getCurrentUserProfile(Authentication authentication) {
        String subject = securityUtils.getAuthenticatedEmail(authentication);

        User user = userRepository.findByJwtSubject(subject)
                .orElseThrow(() -> new AuthUserNotFoundException(subject));

        List<UserGroupSummaryDto> groups = groupMembershipRepository.findAllByUserIdWithGroup(user.getId()).stream()
                .map(this::toGroupSummary)
                .toList();

        return new AuthMeResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAccountType(),
                user.getAccountStatus(),
                user.getPhone(),
                user.getRoles().stream().toList(),
                groups,
                user.getCreatedAt(),
                user.isAdminSupport()
        );
    }

    private UserGroupSummaryDto toGroupSummary(GroupMembership membership) {
        return UserGroupSummaryMapping.fromGroup(membership.getGroup(), null);
    }
}
