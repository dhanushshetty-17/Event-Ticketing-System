package com.dhanush.ticketing.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventCreateRequest(

        @NotBlank(message = "Event name is required")
        @Size(max = 200, message = "Event name must not exceed 200 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Venue ID is required")
        UUID venueId,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        OffsetDateTime startsAt,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        OffsetDateTime endsAt
) {
}