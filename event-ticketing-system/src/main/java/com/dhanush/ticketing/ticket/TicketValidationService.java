package com.dhanush.ticketing.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class TicketValidationService {

    private final TicketRepository ticketRepository;

    public TicketValidationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketValidationResponse validate(UUID ticketId, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket was already used at " + ticket.getScannedAt());
        }

        if (ticket.getStatus() != TicketStatus.ISSUED
                && ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket is not valid for entrance");
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setScannedAt(Instant.now());
        ticket.setScannedBy(authentication.getName());
        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketValidationResponse(
                savedTicket.getId(),
                savedTicket.getStatus(),
                savedTicket.getScannedAt(),
                savedTicket.getScannedBy(),
                true);
    }
}
