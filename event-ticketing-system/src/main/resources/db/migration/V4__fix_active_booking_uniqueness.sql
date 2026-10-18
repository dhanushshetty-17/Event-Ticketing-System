DROP INDEX IF EXISTS uk_active_booking_per_event_seat;

CREATE UNIQUE INDEX uk_active_booking_per_event_seat
    ON bookings (event_id, event_seat_id)
    WHERE status IN ('PENDING', 'CONFIRMED');