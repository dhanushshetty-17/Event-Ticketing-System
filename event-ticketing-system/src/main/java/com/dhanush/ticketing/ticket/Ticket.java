package com.dhanush.ticketing.ticket;

import com.dhanush.ticketing.booking.Booking;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_booking",
                        columnNames = "booking_id"
                ),
                @UniqueConstraint(
                        name = "uk_ticket_code",
                        columnNames = "ticket_code"
                )
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "ticket_code", nullable = false, unique = true)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    public Ticket() {
    }

    public Ticket(
            Booking booking,
            String ticketCode,
            TicketStatus status) {

        this.booking = booking;
        this.ticketCode = ticketCode;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}