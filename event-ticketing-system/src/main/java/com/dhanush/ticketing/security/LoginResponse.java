package com.dhanush.ticketing.security;

public record LoginResponse(
        String token,
        String tokenType,
        String userId,
        String email,
        String role
) {
}