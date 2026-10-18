package com.dhanush.ticketing.booking;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        UUID eventId,
        UUID seatId,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {
}