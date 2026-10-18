
package com.dhanush.ticketing.booking;

import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.event.EventSeatStatus;
import com.dhanush.ticketing.outbox.OutboxEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class BookingConfirmationTransactionService {

    private final BookingRepository bookingRepository;
    private final EventSeatRepository eventSeatRepository;
    private final OutboxEventService outboxEventService;

    public BookingConfirmationTransactionService(
            BookingRepository bookingRepository,
            EventSeatRepository eventSeatRepository,
            OutboxEventService outboxEventService) {
        this.bookingRepository = bookingRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public Booking confirmBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You are not allowed to confirm this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING bookings can be confirmed");
        }

        EventSeat eventSeat = eventSeatRepository
                .findWithLockByEventIdAndSeatId(
                        booking.getEvent().getId(),
                        booking.getEventSeat().getSeat().getId())
                .orElseThrow(() ->
                        new IllegalStateException("Seat not found for booking"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (booking.getExpiresAt() != null
                && !booking.getExpiresAt().isAfter(now)) {

            booking.setStatus(BookingStatus.EXPIRED);

            if (eventSeat.getStatus() == EventSeatStatus.RESERVED
                    && userId.equals(eventSeat.getReservedBy())) {
                eventSeat.setStatus(EventSeatStatus.AVAILABLE);
                eventSeat.setReservedBy(null);
                eventSeat.setReservedAt(null);
                eventSeat.setReservationExpiresAt(null);

                eventSeatRepository.save(eventSeat);
            }

            bookingRepository.save(booking);

            return null;
        }

        if (eventSeat.getStatus() != EventSeatStatus.RESERVED) {
            throw new IllegalStateException(
                    "Seat is not currently reserved");
        }

        if (!userId.equals(eventSeat.getReservedBy())) {
            throw new IllegalStateException(
                    "Seat is reserved by another user");
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        eventSeat.setStatus(EventSeatStatus.BOOKED);
        eventSeat.setReservedBy(null);
        eventSeat.setReservedAt(null);
        eventSeat.setReservationExpiresAt(null);

        eventSeatRepository.save(eventSeat);
        bookingRepository.save(booking);

        outboxEventService.createBookingConfirmedEvent(booking.getId());

        return booking;
    }

    @Transactional(readOnly = true)
    public Booking findBooking(UUID bookingId) {
        return bookingRepository.findWithEventAndSeatById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));
    }
}
