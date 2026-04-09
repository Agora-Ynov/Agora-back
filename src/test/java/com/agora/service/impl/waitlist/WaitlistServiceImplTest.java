package com.agora.service.impl.waitlist;

import com.agora.dto.request.waitlist.CreateWaitlistRequestDto;
import com.agora.dto.response.waitlist.WaitlistEntryResponseDto;
import com.agora.entity.resource.Resource;
import com.agora.entity.user.User;
import com.agora.entity.waitlist.WaitlistEntry;
import com.agora.enums.resource.ResourceType;
import com.agora.enums.user.AccountStatus;
import com.agora.enums.user.AccountType;
import com.agora.enums.waitlist.WaitlistStatus;
import com.agora.exception.BusinessException;
import com.agora.exception.ErrorCode;
import com.agora.config.SecurityUtils;
import com.agora.repository.resource.ResourceRepository;
import com.agora.repository.user.UserRepository;
import com.agora.repository.waitlist.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceImplTest {

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private WaitlistServiceImpl service;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("x@y.fr");
        user.setFirstName("A");
        user.setLastName("B");
        user.setAccountType(AccountType.AUTONOMOUS);
        user.setAccountStatus(AccountStatus.ACTIVE);
        when(securityUtils.getAuthenticatedEmail(authentication)).thenReturn("x@y.fr");
        when(userRepository.findByJwtSubject("x@y.fr")).thenReturn(Optional.of(user));
    }

    @Test
    void listMine_shouldMapDtos() {
        Resource res = new Resource();
        res.setId(UUID.randomUUID());
        res.setName("Salle A");
        WaitlistEntry w = new WaitlistEntry();
        w.setId(UUID.randomUUID());
        w.setUser(user);
        w.setResource(res);
        w.setSlotDate(LocalDate.of(2026, 4, 1));
        w.setSlotStart("09:00");
        w.setSlotEnd("10:00");
        w.setPosition(1);
        w.setStatus(WaitlistStatus.WAITING);
        when(waitlistEntryRepository.findAllByUserIdWithResource(userId)).thenReturn(List.of(w));

        List<WaitlistEntryResponseDto> out = service.listMine(authentication);

        assertThat(out).hasSize(1);
        assertThat(out.getFirst().resourceName()).isEqualTo("Salle A");
        assertThat(out.getFirst().status()).isEqualTo("WAITING");
    }

    @Test
    void enroll_success() {
        UUID rid = UUID.randomUUID();
        Resource res = new Resource();
        res.setId(rid);
        res.setName("R");
        res.setActive(true);
        res.setResourceType(ResourceType.IMMOBILIER);
        when(resourceRepository.findById(rid)).thenReturn(Optional.of(res));
        when(waitlistEntryRepository.existsByUser_IdAndResource_IdAndSlotDateAndSlotStartAndSlotEnd(
                eq(userId), eq(rid), eq(LocalDate.of(2026, 5, 1)), eq("09:00"), eq("10:00")))
                .thenReturn(false);
        when(waitlistEntryRepository.maxPositionForSlot(rid, LocalDate.of(2026, 5, 1), "09:00", "10:00", WaitlistStatus.WAITING))
                .thenReturn(0);
        WaitlistEntry saved = new WaitlistEntry();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setResource(res);
        saved.setSlotDate(LocalDate.of(2026, 5, 1));
        saved.setSlotStart("09:00");
        saved.setSlotEnd("10:00");
        saved.setPosition(1);
        saved.setStatus(WaitlistStatus.WAITING);
        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenAnswer(inv -> {
            WaitlistEntry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateWaitlistRequestDto req = new CreateWaitlistRequestDto(rid, LocalDate.of(2026, 5, 1), "09:00", "10:00");
        WaitlistEntryResponseDto dto = service.enroll(req, authentication);

        assertThat(dto.slotStart()).isEqualTo("09:00");
        verify(waitlistEntryRepository).save(any(WaitlistEntry.class));
    }

    @Test
    void enroll_duplicateSlot_shouldThrow() {
        UUID rid = UUID.randomUUID();
        Resource res = resource(rid, true);
        when(resourceRepository.findById(rid)).thenReturn(Optional.of(res));
        when(waitlistEntryRepository.existsByUser_IdAndResource_IdAndSlotDateAndSlotStartAndSlotEnd(
                eq(userId), eq(rid), eq(LocalDate.of(2026, 5, 1)), eq("09:00"), eq("10:00")))
                .thenReturn(true);

        CreateWaitlistRequestDto req = new CreateWaitlistRequestDto(rid, LocalDate.of(2026, 5, 1), "09:00", "10:00");
        assertThatThrownBy(() -> service.enroll(req, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DATA_CONFLICT);
    }

    @Test
    void enroll_inactiveResource_shouldThrowNotFound() {
        UUID rid = UUID.randomUUID();
        Resource res = resource(rid, false);
        when(resourceRepository.findById(rid)).thenReturn(Optional.of(res));

        CreateWaitlistRequestDto req = new CreateWaitlistRequestDto(rid, LocalDate.of(2026, 5, 1), "09:00", "10:00");
        assertThatThrownBy(() -> service.enroll(req, authentication))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void cancel_shouldDelete() {
        UUID wid = UUID.randomUUID();
        WaitlistEntry e = new WaitlistEntry();
        e.setId(wid);
        when(waitlistEntryRepository.findByIdAndUser_Id(wid, userId)).thenReturn(Optional.of(e));

        service.cancel(wid, authentication);

        verify(waitlistEntryRepository).delete(e);
    }

    private static Resource resource(UUID id, boolean active) {
        Resource res = new Resource();
        res.setId(id);
        res.setName("R");
        res.setActive(active);
        res.setResourceType(ResourceType.IMMOBILIER);
        res.setDepositAmountCents(0);
        return res;
    }
}
