package com.agora.service.impl.superadmin;

import com.agora.dto.response.admin.AdminSupportUserDto;
import com.agora.entity.user.User;
import com.agora.enums.user.AccountStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.exception.resource.ResourceNotFountException;
import com.agora.repository.user.UserRepository;
import com.agora.service.superadmin.SuperadminAdminSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperadminAdminSupportServiceImpl implements SuperadminAdminSupportService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminSupportUserDto> listActiveAdminSupport() {
        return userRepository
                .findAllByAdminSupportIsTrueAndAccountStatus(AccountStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminSupportUserDto promote(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFountException("Utilisateur introuvable."));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE_ADMIN_PROMOTION);
        }
        if (user.isAdminSupport()) {
            throw new BusinessException(ErrorCode.ADMIN_SUPPORT_ALREADY);
        }
        user.setAdminSupport(true);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    @Transactional
    public void revoke(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFountException("Utilisateur introuvable."));
        if (!user.isAdminSupport()) {
            return;
        }
        user.setAdminSupport(false);
        userRepository.save(user);
    }

    private AdminSupportUserDto toDto(User user) {
        return new AdminSupportUserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAccountStatus());
    }
}
