
package com.dhanush.ticketing;

import com.dhanush.ticketing.booking.Booking;
import com.dhanush.ticketing.booking.BookingConfirmationTransactionService;
import com.dhanush.ticketing.booking.BookingRepository;
import com.dhanush.ticketing.booking.BookingStatus;
import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.event.EventSeatStatus;
import com.dhanush.ticketing.outbox.OutboxEvent;
import com.dhanush.ticketing.outbox.OutboxEventRepository;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import com.dhanush.ticketing.messaging.BookingConfirmedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OutboxEventIntegrationTest extends SharedTestContainers {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private BookingConfirmationTransactionService confirmationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void confirmingBookingCreatesOutboxEvent() throws Exception {
        User user = userRepository.findByEmail("user@example.com")
                .orElseThrow();

        EventSeat eventSeat = eventSeatRepository.findAll()
                .stream()
                .filter(seat -> seat.getStatus() == EventSeatStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No available event seat"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        eventSeat.setStatus(EventSeatStatus.RESERVED);
        eventSeat.setReservedBy(user.getId());
        eventSeat.setReservedAt(now);
        eventSeat.setReservationExpiresAt(now.plusMinutes(10));
        eventSeatRepository.saveAndFlush(eventSeat);

        Booking booking = new Booking(
                user,
                eventSeat.getEvent(),
                eventSeat,
                BookingStatus.PENDING,
                now,
                now.plusMinutes(10)
        );

        booking = bookingRepository.saveAndFlush(booking);

        UUID bookingId = booking.getId();

        Booking confirmedBooking =
                confirmationService.confirmBooking(bookingId, user.getId());

        assertNotNull(confirmedBooking);
        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());

        OutboxEvent outboxEvent = outboxEventRepository.findAll()
                .stream()
                .filter(event -> bookingId.equals(event.getAggregateId()))
                .filter(event -> "BOOKING_CONFIRMED".equals(event.getEventType()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Booking confirmation outbox event not found"));

        assertEquals("BOOKING", outboxEvent.getAggregateType());
        assertNull(outboxEvent.getPublishedAt());

        BookingConfirmedEvent payload = objectMapper.readValue(
                outboxEvent.getPayload(),
                BookingConfirmedEvent.class
        );

        assertEquals(bookingId, payload.bookingId());
        assertNotNull(outboxEvent.getCreatedAt());
    }
}
