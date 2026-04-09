package com.agora.service.group;

import com.agora.dto.response.auth.UserGroupSummaryDto;
import com.agora.dto.response.auth.UserGroupSummaryMapping;
import com.agora.entity.group.GroupMembership;
import com.agora.entity.user.User;
import com.agora.exception.auth.AuthUserNotFoundException;
import com.agora.config.SecurityUtils;
import com.agora.exception.resource.ResourceNotFountException;
import com.agora.repository.group.GroupMembershipRepository;
import com.agora.repository.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserGroupsService {

    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final SecurityUtils securityUtils;

    public UserGroupsService(
            UserRepository userRepository,
            GroupMembershipRepository groupMembershipRepository,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<UserGroupSummaryDto> listForCurrentUser(Authentication authentication) {
        String subject = securityUtils.getAuthenticatedEmail(authentication);
        User user = userRepository.findByJwtSubject(subject)
                .orElseThrow(() -> new AuthUserNotFoundException(subject));

        return groupMembershipRepository.findAllByUserIdWithGroup(user.getId()).stream()
                .map(this::toDtoWithMemberCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserGroupSummaryDto getForCurrentUser(UUID groupId, Authentication authentication) {
        String subject = securityUtils.getAuthenticatedEmail(authentication);
        User user = userRepository.findByJwtSubject(subject)
                .orElseThrow(() -> new AuthUserNotFoundException(subject));
        GroupMembership membership = groupMembershipRepository
                .findByUserIdAndGroupIdWithGroup(user.getId(), groupId)
                .orElseThrow(() -> new ResourceNotFountException("Groupe introuvable ou accès refusé."));
        return toDtoWithMemberCount(membership);
    }

    private UserGroupSummaryDto toDtoWithMemberCount(GroupMembership membership) {
        int memberCount = (int) groupMembershipRepository.countByGroup_Id(membership.getGroup().getId());
        return UserGroupSummaryMapping.fromGroup(membership.getGroup(), memberCount);
    }
}
