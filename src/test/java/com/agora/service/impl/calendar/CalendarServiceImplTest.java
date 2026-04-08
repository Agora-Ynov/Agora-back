package com.agora.service.impl.calendar;

import com.agora.dto.response.calendar.CalendarResponseDto;
import com.agora.dto.response.calendar.CalendarSlotDto;
import com.agora.entity.reservation.Reservation;
import com.agora.entity.resource.Resource;
import com.agora.entity.user.User;
import com.agora.enums.reservation.ReservationStatus;
import com.agora.enums.resource.ResourceType;
import com.agora.exception.BusinessException;
import com.agora.repository.reservation.ReservationRepository;
import com.agora.repository.resource.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Test
    void getMonthlyCalendar_marksSlotUnavailableWhenReservationOverlaps() {
        UUID resourceId = UUID.randomUUID();
        Resource resource = Resource.builder()
                .id(resourceId)
                .name("Salle A")
                .resourceType(ResourceType.IMMOBILIER)
                .active(true)
                .build();

        LocalDate day = LocalDate.of(2026, 4, 10);

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setReservationDate(day);
        reservation.setSlotStart(LocalTime.of(9, 0));
        reservation.setSlotEnd(LocalTime.of(10, 0));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        User user = new User();
        user.setId(UUID.randomUUID());
        reservation.setUser(user);

        when(resourceRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(resource));
        when(reservationRepository.findBlockingReservationsForCalendar(any(), any(), any(), any()))
                .thenReturn(List.of(reservation));

        CalendarResponseDto response = calendarService.getMonthlyCalendar(2026, 4, null);

        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(4);
        assertThat(response.days()).hasSize(30);

        List<CalendarSlotDto> slotsForDay = response.days().stream()
                .filter(d -> d.date().equals(day))
                .findFirst()
                .orElseThrow()
                .slots();

        CalendarSlotDto nineToTen = slotsForDay.stream()
                .filter(s -> s.resourceId().equals(resourceId) && "09:00".equals(s.slotStart()))
                .findFirst()
                .orElseThrow();
        assertThat(nineToTen.isAvailable()).isFalse();

        CalendarSlotDto eightToNine = slotsForDay.stream()
                .filter(s -> s.resourceId().equals(resourceId) && "08:00".equals(s.slotStart()))
                .findFirst()
                .orElseThrow();
        assertThat(eightToNine.isAvailable()).isTrue();
    }

    @Test
    void getMonthlyCalendar_unknownResource_throws() {
        UUID missing = UUID.randomUUID();
        when(resourceRepository.findByIdAndActiveTrue(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.getMonthlyCalendar(2026, 4, missing))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMonthlyCalendar_invalidMonth_throws() {
        assertThatThrownBy(() -> calendarService.getMonthlyCalendar(2026, 13, null))
                .isInstanceOf(BusinessException.class);
    }
}
