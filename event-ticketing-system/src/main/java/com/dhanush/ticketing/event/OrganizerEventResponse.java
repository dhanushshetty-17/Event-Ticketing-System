package com.dhanush.ticketing.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizerEventResponse(
        UUID id,
        String name,
        String description,
        UUID venueId,
        String venueName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        EventStatus status,
        long totalSeats,
        long reservedSeats,
        long soldSeats,
        BigDecimal revenue
) {
}
