package com.agora.service.resource;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Grille horaire par défaut alignée sur {@code GET /api/resources/{id}/slots} (MVP).
 */
public final class ResourceSlotTemplate {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private ResourceSlotTemplate() {
    }

    public record FixedSlot(LocalTime start, LocalTime end) {
        public String startLabel() {
            return HM.format(start);
        }

        public String endLabel() {
            return HM.format(end);
        }
    }

    public static List<FixedSlot> defaultSlots() {
        return List.of(
                new FixedSlot(LocalTime.of(8, 0), LocalTime.of(9, 0)),
                new FixedSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new FixedSlot(LocalTime.of(10, 0), LocalTime.of(11, 0))
        );
    }
}
