package com.agora.repository.calendar;

import com.agora.entity.calendar.BlackoutPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlackoutPeriodRepository extends JpaRepository<BlackoutPeriod, UUID> {
}
