package com.dhanush.ticketing.event;

import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.venue.Venue;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    public Event() {
    }

    public Event(
            String name,
            String description,
            Venue venue,
            User organizer,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            EventStatus status) {

        this.name = name;
        this.description = description;
        this.venue = venue;
        this.organizer = organizer;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Venue getVenue() {
        return venue;
    }

    public User getOrganizer() {
        return organizer;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}