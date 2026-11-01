package com.dhanush.ticketing.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrganizerEventSummaryResponse(
        UUID eventId,
        long totalSeats,
        long reservedCount,
        long soldCount,
        BigDecimal grossRevenue,
        List<SectionOccupancy> sections
) {
}
