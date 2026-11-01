package com.dhanush.ticketing.ticket;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID ticketId,
        UUID bookingId,
        String eventName,
        String venueName,
        String venueAddress,
        String venueCity,
        String gateDetails,
        OffsetDateTime startsAt,
        String seatIdentifier,
        TicketStatus status,
        String ticketCode,
        String qrPayload,
        java.time.Instant scannedAt,
        String scannedBy
) {
}
