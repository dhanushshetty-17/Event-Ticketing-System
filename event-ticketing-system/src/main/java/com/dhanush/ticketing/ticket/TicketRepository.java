package com.dhanush.ticketing.ticket;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findById(UUID ticketId);

    boolean existsByBookingId(UUID bookingId);

    @EntityGraph(attributePaths = {
            "booking",
            "booking.user",
            "booking.event",
            "booking.event.venue",
            "booking.eventSeat",
            "booking.eventSeat.seat"
    })
    Optional<Ticket> findByVerificationToken(String verificationToken);

    @EntityGraph(attributePaths = {
            "booking",
            "booking.user",
            "booking.event",
            "booking.event.venue",
            "booking.eventSeat",
            "booking.eventSeat.seat"
    })
    Optional<Ticket> findWithDetailsById(UUID ticketId);

    @EntityGraph(attributePaths = {
            "booking",
            "booking.user",
            "booking.event",
            "booking.event.venue",
            "booking.eventSeat",
            "booking.eventSeat.seat"
    })
    Optional<Ticket> findWithDetailsByBookingId(UUID bookingId);
}