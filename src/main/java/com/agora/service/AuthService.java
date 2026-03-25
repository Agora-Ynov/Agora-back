package com.agora.service;

import com.agora.dto.request.RegisterRequestDto;
import com.agora.dto.response.RegisterResponseDto;
import com.agora.entity.Group;
import com.agora.entity.GroupMembership;
import com.agora.entity.User;
import com.agora.enums.AccountStatus;
import com.agora.enums.AccountType;
import com.agora.exception.EmailAlreadyExistsException;
import com.agora.mapper.UserMapper;
import com.agora.repository.GroupMembershipRepository;
import com.agora.repository.GroupRepository;
import com.agora.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private static final String PUBLIC_GROUP_NAME = "Public";

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            GroupMembershipRepository groupMembershipRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        String email = request.getEmail().trim();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        Group publicGroup = groupRepository.findByName(PUBLIC_GROUP_NAME)
                .orElseThrow(() -> new IllegalStateException("Le groupe preset 'Public' est introuvable"));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone());
        user.setAccountType(AccountType.AUTONOMOUS);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        GroupMembership membership = new GroupMembership();
        membership.setUser(savedUser);
        membership.setGroup(publicGroup);
        membership.setJoinedAt(Instant.now());
        groupMembershipRepository.save(membership);

        return userMapper.toRegisterResponse(savedUser);
    }
}
