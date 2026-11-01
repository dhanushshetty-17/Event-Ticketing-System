package com.dhanush.ticketing.booking;

import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.event.EventSeatStatus;
import com.dhanush.ticketing.event.EventStatus;
import com.dhanush.ticketing.event.EventRepository;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class SeatReservationTransactionService {

    private final UserRepository userRepository;
    private final EventSeatRepository eventSeatRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public SeatReservationTransactionService(
            UserRepository userRepository,
            EventSeatRepository eventSeatRepository,
            BookingRepository bookingRepository,
            EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Booking reserveSeat(UUID userId, UUID eventId, UUID seatId) {
        if (eventRepository.findById(eventId)
                .map(event -> event.getStatus() != EventStatus.CANCELLED)
                .orElse(false) == false) {
            throw new IllegalStateException(
                    "Cannot reserve seats for a cancelled or missing event");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EventSeat eventSeat = eventSeatRepository
                .findWithLockByEventIdAndSeatId(eventId, seatId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat not found for this event"));

        if (eventSeat.getEvent().getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Seats can only be reserved for published events");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (eventSeat.getStatus() == EventSeatStatus.RESERVED
                && eventSeat.getReservationExpiresAt() != null
                && !eventSeat.getReservationExpiresAt().isAfter(now)) {

            bookingRepository
                    .findByEventSeatIdAndStatus(
                            eventSeat.getId(),
                            BookingStatus.PENDING)
                    .ifPresent(booking -> {
                        booking.setStatus(BookingStatus.EXPIRED);
                        bookingRepository.saveAndFlush(booking);
                    });

            eventSeat.setStatus(EventSeatStatus.AVAILABLE);
            eventSeat.setReservedBy(null);
            eventSeat.setReservedAt(null);
            eventSeat.setReservationExpiresAt(null);
        }

        if (eventSeat.getStatus() != EventSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat is not available");
        }

        OffsetDateTime expiresAt = now.plusMinutes(10);

        eventSeat.setStatus(EventSeatStatus.RESERVED);
        eventSeat.setReservedBy(userId);
        eventSeat.setReservedAt(now);
        eventSeat.setReservationExpiresAt(expiresAt);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(eventSeat.getEvent());
        booking.setEventSeat(eventSeat);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(now);
        booking.setExpiresAt(expiresAt);

        return bookingRepository.save(booking);
    }
}