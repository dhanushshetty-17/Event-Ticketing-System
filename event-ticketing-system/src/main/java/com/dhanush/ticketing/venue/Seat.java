package com.dhanush.ticketing.venue;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_venue_label",
                        columnNames = {"venue_id", "seat_label"}
                )
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;

    @Column(nullable = false)
    private String rowName;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    public Seat() {
    }

    public Seat(
            Venue venue,
            String seatLabel,
            String rowName,
            Integer seatNumber,
            SeatStatus status) {

        this.venue = venue;
        this.seatLabel = seatLabel;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Venue getVenue() {
        return venue;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public String getRowName() {
        return rowName;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}