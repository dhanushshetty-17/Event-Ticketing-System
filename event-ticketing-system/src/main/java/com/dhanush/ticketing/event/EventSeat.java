package com.dhanush.ticketing.event;

import com.dhanush.ticketing.venue.Seat;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "event_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_seat",
                        columnNames = {"event_id", "seat_id"}
                )
        }
)
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSeatStatus status;

    @Column(name = "reserved_by")
    private UUID reservedBy;

    @Column(name = "reserved_at")
    private OffsetDateTime reservedAt;

    @Column(name = "reservation_expires_at")
    private OffsetDateTime reservationExpiresAt;

    public EventSeat() {
    }

    public EventSeat(Event event, Seat seat, EventSeatStatus status) {
        this.event = event;
        this.seat = seat;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Seat getSeat() {
        return seat;
    }

    public EventSeatStatus getStatus() {
        return status;
    }

    public UUID getReservedBy() {
        return reservedBy;
    }

    public OffsetDateTime getReservedAt() {
        return reservedAt;
    }

    public OffsetDateTime getReservationExpiresAt() {
        return reservationExpiresAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public void setStatus(EventSeatStatus status) {
        this.status = status;
    }

    public void setReservedBy(UUID reservedBy) {
        this.reservedBy = reservedBy;
    }

    public void setReservedAt(OffsetDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    public void setReservationExpiresAt(OffsetDateTime reservationExpiresAt) {
        this.reservationExpiresAt = reservationExpiresAt;
    }
}