package com.dhanush.ticketing.messaging;

import com.dhanush.ticketing.booking.Booking;
import com.dhanush.ticketing.booking.BookingRepository;
import com.dhanush.ticketing.booking.BookingStatus;
import com.dhanush.ticketing.ticket.Ticket;
import com.dhanush.ticketing.ticket.TicketRepository;
import com.dhanush.ticketing.ticket.TicketStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

@Profile("!outbox-test")
@Component
public class TicketGenerationConsumer {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final String jwtSecret;

    public TicketGenerationConsumer(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            @Value("${security.jwt.secret}") String jwtSecret) {

        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.jwtSecret = jwtSecret;
    }

    @RabbitListener(queues = "ticket.generation.queue")
    @Transactional
    public void generateTicket(BookingConfirmedEvent event) {

        UUID bookingId = event.bookingId();

        if (ticketRepository.existsByBookingId(bookingId)) {
            return;
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Booking not found: " + bookingId
                        ));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            return;
        }

        Ticket ticket = new Ticket();

        ticket.setBooking(booking);
        ticket.setTicketCode(UUID.randomUUID().toString());
        ticket.setStatus(TicketStatus.ISSUED);

        Ticket savedTicket = ticketRepository.save(ticket);
        savedTicket.setVerificationToken(createVerificationToken(savedTicket));
        ticketRepository.save(savedTicket);
    }

    private String createVerificationToken(Ticket ticket) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(
                    (ticket.getId() + ":" + ticket.getTicketCode()).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create ticket verification token", exception);
        }
    }
}