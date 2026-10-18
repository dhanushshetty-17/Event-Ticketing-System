package com.dhanush.ticketing.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    boolean existsByBookingId(UUID bookingId);
}