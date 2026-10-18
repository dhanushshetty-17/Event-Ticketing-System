
package com.dhanush.ticketing.booking;

import com.dhanush.ticketing.event.*;
import com.dhanush.ticketing.user.*;
import com.dhanush.ticketing.venue.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SeatReservationConcurrencyIntegrationTest
        extends com.dhanush.ticketing.SharedTestContainers {



    @Autowired
    private SeatReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingConfirmationService confirmationService;

    @Test
    void onlyOneUserCanReserveTheSameSeatConcurrently() throws Exception {
        User user1 = createUser("concurrency1");
        User user2 = createUser("concurrency2");

        Venue venue = venueRepository.saveAndFlush(
                new Venue("Concurrency Test Venue", "Bengaluru"));

        Seat seat = seatRepository.saveAndFlush(
                new Seat(venue, "T-" + UUID.randomUUID(),
                        "T", 1, SeatStatus.ACTIVE));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Event event = eventRepository.saveAndFlush(
                new Event(
                        "Concurrency Test Event",
                        "Integration test",
                        venue,
                        user1,
                        now.plusDays(1),
                        now.plusDays(1).plusHours(2),
                        EventStatus.PUBLISHED
                ));

        EventSeat eventSeat = eventSeatRepository.saveAndFlush(
                new EventSeat(event, seat, EventSeatStatus.AVAILABLE));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> firstAttempt = executor.submit(() ->
                    attemptReservation(user1.getId(), event.getId(),
                            seat.getId(), ready, start));

            Future<Boolean> secondAttempt = executor.submit(() ->
                    attemptReservation(user2.getId(), event.getId(),
                            seat.getId(), ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Both workers should be ready");

            start.countDown();

            boolean firstSucceeded =
                    firstAttempt.get(15, TimeUnit.SECONDS);
            boolean secondSucceeded =
                    secondAttempt.get(15, TimeUnit.SECONDS);

            assertNotEquals(firstSucceeded, secondSucceeded,
                    "Exactly one reservation attempt must succeed");

            EventSeat savedSeat = eventSeatRepository
                    .findByEventIdAndSeatId(event.getId(), seat.getId())
                    .orElseThrow();

            assertEquals(EventSeatStatus.RESERVED, savedSeat.getStatus());

            Integer activeBookings = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM bookings
                    WHERE event_id = ?
                      AND event_seat_id = ?
                      AND status IN ('PENDING', 'CONFIRMED')
                    """,
                    Integer.class,
                    event.getId(),
                    eventSeat.getId()
            );

            assertEquals(1, activeBookings,
                    "There must be exactly one active booking for the seat");

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void anotherUserCanReserveSeatAfterPreviousReservationExpires() {
        User user1 = createUser("expiry1");
        User user2 = createUser("expiry2");

        Venue venue = venueRepository.saveAndFlush(
                new Venue("Expiry Test Venue", "Bengaluru"));

        Seat seat = seatRepository.saveAndFlush(
                new Seat(
                        venue,
                        "E-" + UUID.randomUUID(),
                        "E",
                        1,
                        SeatStatus.ACTIVE
                ));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Event event = eventRepository.saveAndFlush(
                new Event(
                        "Expiry Test Event",
                        "Reservation expiry integration test",
                        venue,
                        user1,
                        now.plusDays(1),
                        now.plusDays(1).plusHours(2),
                        EventStatus.PUBLISHED
                ));

        EventSeat eventSeat = eventSeatRepository.saveAndFlush(
                new EventSeat(event, seat, EventSeatStatus.AVAILABLE));

        // User 1 reserves the seat.
        Booking firstBooking = reservationService.reserveSeat(
                user1.getId(), event.getId(), seat.getId());

        assertEquals(BookingStatus.PENDING, firstBooking.getStatus());

        // Simulate expiry without waiting 10 minutes.
        jdbcTemplate.update(
                """
                UPDATE event_seats
                SET reservation_expires_at = ?
                WHERE id = ?
                """,
                java.sql.Timestamp.from(
                        now.minusMinutes(1).toInstant()),
                eventSeat.getId()
        );

        // User 2 tries to reserve the now-expired seat.
        Booking secondBooking = reservationService.reserveSeat(
                user2.getId(), event.getId(), seat.getId());

        assertNotEquals(firstBooking.getId(), secondBooking.getId());
        assertEquals(BookingStatus.PENDING, secondBooking.getStatus());

        // The first booking should now be expired.
        Booking savedFirstBooking = bookingRepository
                .findById(firstBooking.getId())
                .orElseThrow();

        assertEquals(BookingStatus.EXPIRED, savedFirstBooking.getStatus());

        // The seat should now belong to the second reservation.
        EventSeat savedSeat = eventSeatRepository
                .findByEventIdAndSeatId(event.getId(), seat.getId())
                .orElseThrow();

        assertEquals(EventSeatStatus.RESERVED, savedSeat.getStatus());
        assertEquals(user2.getId(), savedSeat.getReservedBy());

        // Exactly one active booking should remain.
        Integer activeBookings = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM bookings
                WHERE event_id = ?
                  AND event_seat_id = ?
                  AND status IN ('PENDING', 'CONFIRMED')
                """,
                Integer.class,
                event.getId(),
                eventSeat.getId()
        );

        assertEquals(1, activeBookings);
    }

    @Test
    void confirmingExpiredBookingMarksItExpiredAndReleasesSeat() {
        User user = createUser("confirm-expiry");

        Venue venue = venueRepository.saveAndFlush(
                new Venue("Confirmation Expiry Venue", "Bengaluru"));

        Seat seat = seatRepository.saveAndFlush(
                new Seat(
                        venue,
                        "CE-" + UUID.randomUUID(),
                        "CE",
                        1,
                        SeatStatus.ACTIVE
                )
        );

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Event event = eventRepository.saveAndFlush(
                new Event(
                        "Confirmation Expiry Event",
                        "Tests expired booking confirmation",
                        venue,
                        user,
                        now.plusDays(1),
                        now.plusDays(1).plusHours(2),
                        EventStatus.PUBLISHED
                )
        );

        eventSeatRepository.saveAndFlush(
                new EventSeat(event, seat, EventSeatStatus.AVAILABLE)
        );

        // Reserve the seat.
        Booking booking = reservationService.reserveSeat(
                user.getId(),
                event.getId(),
                seat.getId()
        );

        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Simulate the reservation expiring.
        // Simulate the reservation expiring in both records.
        java.sql.Timestamp expiredAt = java.sql.Timestamp.from(
                now.minusMinutes(1).toInstant()
        );

        jdbcTemplate.update(
                """
                UPDATE event_seats
                SET reservation_expires_at = ?
                WHERE event_id = ? AND seat_id = ?
                """,
                expiredAt,
                event.getId(),
                seat.getId()
        );

        jdbcTemplate.update(
                """
                UPDATE bookings
                SET expires_at = ?
                WHERE id = ?
                """,
                expiredAt,
                booking.getId()
        );

        // Confirmation should be rejected because the reservation expired.
        assertThrows(
                IllegalStateException.class,
                () -> confirmationService.confirmBooking(
                        booking.getId(),
                        user.getId()
                )
        );

        // Verify the booking's EXPIRED status was committed to the database.
        Booking savedBooking = bookingRepository.findById(booking.getId())
                .orElseThrow();

        assertEquals(
                BookingStatus.EXPIRED,
                savedBooking.getStatus(),
                "The expired booking status must persist after confirmation is rejected"
        );

        // Verify that the seat was released.
        EventSeat savedSeat = eventSeatRepository
                .findByEventIdAndSeatId(event.getId(), seat.getId())
                .orElseThrow();

        assertEquals(EventSeatStatus.AVAILABLE, savedSeat.getStatus());
        assertNull(savedSeat.getReservedBy());
        assertNull(savedSeat.getReservedAt());
        assertNull(savedSeat.getReservationExpiresAt());
    }

    private boolean attemptReservation(
            UUID userId,
            UUID eventId,
            UUID seatId,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {

        ready.countDown();

        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "Timed out waiting for concurrent start");
        }

        try {
            reservationService.reserveSeat(userId, eventId, seatId);
            return true;
        } catch (IllegalStateException expectedRejection) {
            return false;
        }
    }

    private User createUser(String prefix) {
        User user = new User(
                prefix + "-" + UUID.randomUUID() + "@example.com",
                "test-password",
                UserRole.USER
        );

        return userRepository.saveAndFlush(user);
    }
}
