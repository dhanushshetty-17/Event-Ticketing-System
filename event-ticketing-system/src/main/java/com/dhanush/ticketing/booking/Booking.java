package com.dhanush.ticketing.booking;

import com.dhanush.ticketing.event.Event;
import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_seat_id", nullable = false)
    private EventSeat eventSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public Booking() {
    }

    public Booking(
            User user,
            Event event,
            EventSeat eventSeat,
            BookingStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt) {

        this.user = user;
        this.event = event;
        this.eventSeat = eventSeat;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public EventSeat getEventSeat() {
        return eventSeat;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setEventSeat(EventSeat eventSeat) {
        this.eventSeat = eventSeat;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}