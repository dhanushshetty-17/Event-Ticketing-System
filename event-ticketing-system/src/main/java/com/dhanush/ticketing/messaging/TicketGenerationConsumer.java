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

@Profile("!outbox-test")
@Component
public class TicketGenerationConsumer {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    public TicketGenerationConsumer(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository) {

        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
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

        ticketRepository.save(ticket);
    }
}