package com.agora.service.impl.superadmin;

import com.agora.dto.response.admin.AdminSupportUserDto;
import com.agora.entity.user.User;
import com.agora.enums.user.AccountStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperadminAdminSupportServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SuperadminAdminSupportServiceImpl service;

    @Test
    void listActiveAdminSupport_shouldSortAndMap() {
        User a = buildUser("a@x.fr", "Paul", "A");
        User b = buildUser("b@x.fr", "Jean", "B");
        when(userRepository.findAllByAdminSupportIsTrueAndAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(b, a));

        List<AdminSupportUserDto> out = service.listActiveAdminSupport();

        assertThat(out).hasSize(2);
        assertThat(out.getFirst().lastName()).isEqualTo("A");
        assertThat(out.get(1).lastName()).isEqualTo("B");
    }

    @Test
    void promote_shouldSetAdminSupport() {
        User u = buildUser("u@x.fr", "Jean", "Dupont");
        UUID id = u.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(u));
        when(userRepository.save(u)).thenReturn(u);

        AdminSupportUserDto dto = service.promote(id);

        assertThat(u.isAdminSupport()).isTrue();
        assertThat(dto.email()).isEqualTo("u@x.fr");
        verify(userRepository).save(u);
    }

    @Test
    void promote_inactive_shouldThrow() {
        User u = buildUser("u@x.fr", "Jean", "Dupont");
        u.setAccountStatus(AccountStatus.DELETED);
        when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.promote(u.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_ACTIVE_ADMIN_PROMOTION);
        verify(userRepository, never()).save(any());
    }

    @Test
    void promote_alreadyAdminSupport_shouldThrow() {
        User u = buildUser("u@x.fr", "Jean", "Dupont");
        u.setAdminSupport(true);
        when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.promote(u.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.ADMIN_SUPPORT_ALREADY);
    }

    @Test
    void revoke_notAdminSupport_shouldNoop() {
        User u = buildUser("u@x.fr", "Jean", "Dupont");
        when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

        service.revoke(u.getId());

        verify(userRepository, never()).save(any());
    }

    @Test
    void revoke_adminSupport_shouldClear() {
        User u = buildUser("u@x.fr", "Jean", "Dupont");
        u.setAdminSupport(true);
        when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));
        when(userRepository.save(u)).thenReturn(u);

        service.revoke(u.getId());

        assertThat(u.isAdminSupport()).isFalse();
        verify(userRepository).save(u);
    }

    private static User buildUser(String email, String first, String last) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail(email);
        u.setFirstName(first);
        u.setLastName(last);
        u.setAccountStatus(AccountStatus.ACTIVE);
        u.setAdminSupport(false);
        return u;
    }
}
