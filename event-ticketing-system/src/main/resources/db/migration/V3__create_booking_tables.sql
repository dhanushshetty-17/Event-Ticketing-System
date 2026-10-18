CREATE TABLE bookings (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          user_id UUID NOT NULL,
                          event_id UUID NOT NULL,
                          event_seat_id UUID NOT NULL,

                          status VARCHAR(20) NOT NULL,

                          created_at TIMESTAMPTZ NOT NULL,
                          expires_at TIMESTAMPTZ,

                          CONSTRAINT fk_booking_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id),

                          CONSTRAINT fk_booking_event
                              FOREIGN KEY (event_id)
                                  REFERENCES events(id),

                          CONSTRAINT fk_booking_event_seat
                              FOREIGN KEY (event_seat_id)
                                  REFERENCES event_seats(id),

                          CONSTRAINT chk_booking_status
                              CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX idx_bookings_user_id
    ON bookings(user_id);

CREATE INDEX idx_bookings_event_id
    ON bookings(event_id);

CREATE INDEX idx_bookings_event_seat_id
    ON bookings(event_seat_id);

CREATE UNIQUE INDEX uk_active_booking_per_event_seat
    ON bookings(event_id, event_seat_id)
    WHERE status IN ('PENDING', 'CONFIRMED');


CREATE TABLE tickets (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         booking_id UUID NOT NULL,
                         ticket_code VARCHAR(255) NOT NULL,
                         status VARCHAR(20) NOT NULL,

                         CONSTRAINT uk_ticket_booking
                             UNIQUE (booking_id),

                         CONSTRAINT uk_ticket_code
                             UNIQUE (ticket_code),

                         CONSTRAINT fk_ticket_booking
                             FOREIGN KEY (booking_id)
                                 REFERENCES bookings(id),

                         CONSTRAINT chk_ticket_status
                             CHECK (status IN ('ISSUED', 'CANCELLED'))
);