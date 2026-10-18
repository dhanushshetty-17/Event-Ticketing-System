package com.dhanush.ticketing.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingConfirmationController {

    private final BookingConfirmationService confirmationService;

    public BookingConfirmationController(
            BookingConfirmationService confirmationService) {

        this.confirmationService = confirmationService;
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID bookingId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Booking booking = confirmationService.confirmBooking(
                bookingId,
                userId
        );

        BookingResponse response = new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEventSeat().getSeat().getId(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt()
        );

        return ResponseEntity.ok(response);
    }
}