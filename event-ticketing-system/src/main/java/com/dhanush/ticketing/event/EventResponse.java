package com.dhanush.ticketing.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        UUID venueId,
        String venueName,
        UUID organizerId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        EventStatus status
) {
}