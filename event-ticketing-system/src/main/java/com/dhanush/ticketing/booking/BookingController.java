
package com.dhanush.ticketing.booking;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<BookingResponse> getMyBookings(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return bookingRepository.findAllByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Booking booking = bookingRepository
                .findWithEventAndSeatById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's booking");
        }

        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEventSeat().getSeat().getId(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt());
    }
}
