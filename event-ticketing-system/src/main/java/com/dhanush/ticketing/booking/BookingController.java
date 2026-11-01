
package com.dhanush.ticketing.booking;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BookingCancellationService cancellationService;

    public BookingController(
            BookingRepository bookingRepository,
            BookingCancellationService cancellationService) {
        this.bookingRepository = bookingRepository;
        this.cancellationService = cancellationService;
    }

    @GetMapping
    public Page<BookingResponse> getMyBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());

        return bookingRepository.findAllByUser_IdOrderByCreatedAtDesc(
                        userId,
                        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
                .map(this::toResponse);
    }

    @GetMapping("/my-bookings")
    public Page<BookingResponse> getMyBookingsAlias(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getMyBookings(authentication, page, size);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable UUID bookingId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return toResponse(cancellationService.cancelBooking(bookingId, userId));
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
