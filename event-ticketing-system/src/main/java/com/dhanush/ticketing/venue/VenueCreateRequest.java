package com.dhanush.ticketing.venue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueCreateRequest(

        @NotBlank(message = "Venue name is required")
        @Size(max = 200, message = "Venue name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Venue address is required")
        @Size(max = 500, message = "Venue address must not exceed 500 characters")
        String address
) {
}