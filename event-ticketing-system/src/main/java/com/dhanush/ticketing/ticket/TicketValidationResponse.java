package com.dhanush.ticketing.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketValidationResponse(
        UUID ticketId,
        TicketStatus status,
        Instant scannedAt,
        String scannedBy,
        boolean valid
) {
}
