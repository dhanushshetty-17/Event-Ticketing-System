package com.dhanush.ticketing.booking;

import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.event.EventSeatStatus;
import com.dhanush.ticketing.ticket.TicketRepository;
import com.dhanush.ticketing.ticket.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class BookingCancellationTransactionService {

    private final BookingRepository bookingRepository;
    private final EventSeatRepository eventSeatRepository;
    private final TicketRepository ticketRepository;

    public BookingCancellationTransactionService(
            BookingRepository bookingRepository,
            EventSeatRepository eventSeatRepository,
            TicketRepository ticketRepository) {
        this.bookingRepository = bookingRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You are not allowed to cancel this booking");
        }

        if (bookingRepository.cancelIfActive(bookingId) != 1) {
            throw new IllegalStateException(
                    "Only PENDING or CONFIRMED bookings can be cancelled");
        }

        EventSeat eventSeat = eventSeatRepository
                .findWithLockByEventIdAndSeatId(
                        booking.getEvent().getId(),
                        booking.getEventSeat().getSeat().getId())
                .orElseThrow(() -> new IllegalStateException("Seat not found for booking"));

        eventSeat.setStatus(EventSeatStatus.AVAILABLE);
        eventSeat.setReservedBy(null);
        eventSeat.setReservedAt(null);
        eventSeat.setReservationExpiresAt(null);
        eventSeatRepository.save(eventSeat);

        ticketRepository.findWithDetailsByBookingId(bookingId)
                .ifPresent(ticket -> {
                    ticket.setStatus(TicketStatus.CANCELLED);
                    ticketRepository.save(ticket);
                });

        booking.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC));
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking findBooking(UUID bookingId) {
        return bookingRepository.findWithEventAndSeatById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }
}
