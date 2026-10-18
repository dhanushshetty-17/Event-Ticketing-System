
package com.dhanush.ticketing.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
