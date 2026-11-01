
package com.dhanush.ticketing.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByEventSeatIdAndStatus(
            UUID eventSeatId,
            BookingStatus status
    );

    @EntityGraph(attributePaths = {
            "event",
            "eventSeat",
            "eventSeat.seat"
    })
    Optional<Booking> findWithEventAndSeatById(UUID bookingId);

    @EntityGraph(attributePaths = {
            "event",
            "eventSeat",
            "eventSeat.seat"
    })
    List<Booking> findAllByUser_Id(UUID userId);

    @EntityGraph(attributePaths = {
            "event",
            "eventSeat",
            "eventSeat.seat"
    })
    Page<Booking> findAllByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Booking b
        SET b.status = com.dhanush.ticketing.booking.BookingStatus.CONFIRMED
        WHERE b.id = :bookingId
          AND b.status = com.dhanush.ticketing.booking.BookingStatus.PENDING
        """)
    int confirmIfPending(@Param("bookingId") UUID bookingId);

    @Modifying
    @Query("""
        UPDATE Booking b
        SET b.status = com.dhanush.ticketing.booking.BookingStatus.CANCELLED
        WHERE b.id = :bookingId
          AND b.status IN (
            com.dhanush.ticketing.booking.BookingStatus.PENDING,
            com.dhanush.ticketing.booking.BookingStatus.CONFIRMED
          )
        """)
    int cancelIfActive(@Param("bookingId") UUID bookingId);

    @Modifying
    @Query("""
        UPDATE Booking b
        SET b.status = com.dhanush.ticketing.booking.BookingStatus.EXPIRED
        WHERE b.id = :bookingId
          AND b.status = com.dhanush.ticketing.booking.BookingStatus.PENDING
          AND b.expiresAt < :now
        """)
    int expireIfPast(@Param("bookingId") UUID bookingId, @Param("now") java.time.OffsetDateTime now);

    List<Booking> findAllByEvent_Id(UUID eventId);
}
