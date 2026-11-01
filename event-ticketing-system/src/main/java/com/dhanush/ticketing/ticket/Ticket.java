package com.dhanush.ticketing.ticket;

import com.dhanush.ticketing.booking.Booking;
import jakarta.persistence.*;

import java.util.UUID;
import java.time.Instant;

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

    @Column(name = "scanned_at")
    private Instant scannedAt;

    @Column(name = "scanned_by")
    private String scannedBy;

    @Column(name = "verification_token", nullable = false, unique = true)
    private String verificationToken;

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

    @PrePersist
    private void initializeVerificationToken() {
        if (verificationToken == null && ticketCode != null) {
            verificationToken = ticketCode;
        }
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

    public Instant getScannedAt() {
        return scannedAt;
    }

    public String getScannedBy() {
        return scannedBy;
    }

    public void setScannedAt(Instant scannedAt) {
        this.scannedAt = scannedAt;
    }

    public void setScannedBy(String scannedBy) {
        this.scannedBy = scannedBy;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}