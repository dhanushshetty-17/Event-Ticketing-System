package com.dhanush.ticketing.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role
) {
}