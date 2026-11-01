ALTER TABLE event_seats
    DROP CONSTRAINT chk_event_seat_status;

ALTER TABLE event_seats
    ADD CONSTRAINT chk_event_seat_status
        CHECK (status IN ('AVAILABLE', 'RESERVED', 'BOOKED', 'DISABLED'));
