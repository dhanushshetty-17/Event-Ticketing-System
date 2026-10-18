package com.dhanush.ticketing.messaging;

import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId
) {
}