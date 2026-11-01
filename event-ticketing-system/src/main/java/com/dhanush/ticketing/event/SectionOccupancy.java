package com.dhanush.ticketing.event;

public record SectionOccupancy(
        String section,
        long totalSeats,
        long reservedCount,
        long soldCount
) {
}
