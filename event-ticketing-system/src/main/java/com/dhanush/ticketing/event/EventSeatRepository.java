package com.dhanush.ticketing.event;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface EventSeatRepository extends JpaRepository<EventSeat, UUID> {

    @Query("""
    SELECT es
    FROM EventSeat es
    JOIN FETCH es.seat
    WHERE es.event.id = :eventId
    """)
    List<EventSeat> findAllByEventIdWithSeat(@Param("eventId") UUID eventId);

    Optional<EventSeat> findByEventIdAndSeatId(UUID eventId, UUID seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EventSeat> findWithLockByEventIdAndSeatId(
            UUID eventId,
            UUID seatId
    );

    List<EventSeat> findAllByEventId(UUID eventId);

    @Query("""
    SELECT es
    FROM EventSeat es
    JOIN FETCH es.seat
    WHERE es.event.id = :eventId
      AND es.event.status <> com.dhanush.ticketing.event.EventStatus.CANCELLED
      AND es.seat.status = com.dhanush.ticketing.venue.SeatStatus.ACTIVE
    """)
    List<EventSeat> findPublicSeatsByEventId(@Param("eventId") UUID eventId);
}