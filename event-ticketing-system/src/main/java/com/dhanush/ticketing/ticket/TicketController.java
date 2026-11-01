package com.dhanush.ticketing.ticket;

import com.dhanush.ticketing.booking.Booking;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketValidationService ticketValidationService;
    private final String publicBaseUrl;

    public TicketController(
            TicketRepository ticketRepository,
            TicketValidationService ticketValidationService,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.ticketRepository = ticketRepository;
        this.ticketValidationService = ticketValidationService;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/$", "");
    }

    @PostMapping("/{ticketId}/validate")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public TicketValidationResponse validateTicket(
            @PathVariable UUID ticketId,
            Authentication authentication) {
        return ticketValidationService.validate(ticketId, authentication);
    }

    @GetMapping("/verify/{token}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public TicketResponse verifyTicket(@PathVariable String token) {
        Ticket ticket = ticketRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket verification token not found"));
        return toResponse(ticket);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getTicket(
            @PathVariable UUID ticketId,
            Authentication authentication) {

        Ticket ticket = ticketRepository.findWithDetailsById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found"));

        ensureAuthorized(ticket.getBooking(), authentication);
        return toResponse(ticket);
    }

    @GetMapping("/booking/{bookingId}")
    public TicketResponse getTicketByBooking(
            @PathVariable UUID bookingId,
            Authentication authentication) {

        Ticket ticket = ticketRepository.findWithDetailsByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found for booking"));

        ensureAuthorized(ticket.getBooking(), authentication);
        return toResponse(ticket);
    }

    private void ensureAuthorized(
            Booking booking,
            Authentication authentication) {

        boolean privileged = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ORGANIZER")
                                || authority.getAuthority().equals("ROLE_ADMIN"));

        if (privileged) {
            return;
        }

        UUID userId = UUID.fromString(authentication.getName());
        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's ticket");
        }
    }

    private TicketResponse toResponse(Ticket ticket) {
        Booking booking = ticket.getBooking();
        String verificationUrl = publicBaseUrl + "/api/tickets/verify/"
                + (ticket.getVerificationToken() != null
                ? ticket.getVerificationToken()
                : ticket.getTicketCode());
        String qrPayload = "{\"ticketId\":\"" + ticket.getId()
                + "\",\"ticketCode\":\"" + ticket.getTicketCode()
                + "\",\"verificationUrl\":\"" + verificationUrl + "\"}";

        return new TicketResponse(
                ticket.getId(),
                booking.getId(),
                booking.getEvent().getName(),
                booking.getEvent().getVenue().getName(),
                booking.getEvent().getVenue().getAddress(),
                null,
                null,
                booking.getEvent().getStartsAt(),
                booking.getEventSeat().getSeat().getSeatLabel(),
                ticket.getStatus(),
                ticket.getTicketCode(),
                qrPayload,
                ticket.getScannedAt(),
                ticket.getScannedBy()
        );
    }

}
