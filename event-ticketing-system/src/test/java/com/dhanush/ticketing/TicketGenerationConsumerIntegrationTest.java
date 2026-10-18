
package com.dhanush.ticketing;

import com.dhanush.ticketing.booking.Booking;
import com.dhanush.ticketing.booking.BookingRepository;
import com.dhanush.ticketing.booking.BookingStatus;
import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.messaging.BookingConfirmedEvent;
import com.dhanush.ticketing.ticket.TicketRepository;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class TicketGenerationConsumerIntegrationTest extends SharedTestContainers {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Booking confirmedBooking;


    @BeforeEach
    void setUp() {
        User user = userRepository.findByEmail("user@example.com")
                .orElseThrow(() ->
                        new IllegalStateException("Seed user was not created"));

        EventSeat eventSeat = eventSeatRepository.findAll()
                .stream()
                .filter(seat ->
                        bookingRepository.findByEventSeatIdAndStatus(
                                seat.getId(),
                                BookingStatus.CONFIRMED
                        ).isEmpty()
                )
                .filter(seat ->
                        bookingRepository.findByEventSeatIdAndStatus(
                                seat.getId(),
                                BookingStatus.PENDING
                        ).isEmpty()
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No event seat available for integration test"
                        ));

        confirmedBooking = new Booking(
                user,
                eventSeat.getEvent(),
                eventSeat,
                BookingStatus.CONFIRMED,
                OffsetDateTime.now(ZoneOffset.UTC),
                null
        );

        confirmedBooking = bookingRepository.saveAndFlush(confirmedBooking);
    }


    @Test
    void shouldGenerateTicketWhenBookingConfirmedMessageIsReceived() {
        UUID bookingId = confirmedBooking.getId();

        rabbitTemplate.convertAndSend(
                "ticket.generation.queue",
                new BookingConfirmedEvent(bookingId)
        );

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(
                                1,
                                ticketRepository.existsByBookingId(bookingId) ? 1 : 0
                        )
                );

        assertNotNull(bookingId);
    }

    @Test
    void shouldNotGenerateDuplicateTicketForRepeatedMessage() {
        UUID bookingId = confirmedBooking.getId();
        BookingConfirmedEvent event = new BookingConfirmedEvent(bookingId);

        rabbitTemplate.convertAndSend("ticket.generation.queue", event);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(
                                true,
                                ticketRepository.existsByBookingId(bookingId)
                        )
                );

        rabbitTemplate.convertAndSend("ticket.generation.queue", event);

        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(
                                1,
                                ticketRepository.findAll().stream()
                                        .filter(ticket ->
                                                ticket.getBooking().getId().equals(bookingId))
                                        .count()
                        )
                );
    }
}
