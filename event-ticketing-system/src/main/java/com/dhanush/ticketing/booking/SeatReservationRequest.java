package com.dhanush.ticketing.booking;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SeatReservationRequest(

        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "Seat ID is required")
        UUID seatId
) {
}