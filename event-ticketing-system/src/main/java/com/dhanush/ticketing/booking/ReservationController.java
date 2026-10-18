
package com.dhanush.ticketing.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final SeatReservationService seatReservationService;

    public ReservationController(
            SeatReservationService seatReservationService) {
        this.seatReservationService = seatReservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse reserveSeat(
            @Valid @RequestBody SeatReservationRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Booking booking = seatReservationService.reserveSeat(
                userId,
                request.eventId(),
                request.seatId()
        );

        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEventSeat().getSeat().getId(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt()
        );
    }
}
