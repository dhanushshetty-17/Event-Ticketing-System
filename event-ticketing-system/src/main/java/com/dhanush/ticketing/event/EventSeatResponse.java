
package com.dhanush.ticketing.event;

import java.util.UUID;

public record EventSeatResponse(
        UUID seatId,
        String seatLabel,
        EventSeatStatus status
) {
}
