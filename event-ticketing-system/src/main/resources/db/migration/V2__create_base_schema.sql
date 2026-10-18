CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,

                       CONSTRAINT chk_user_role
                           CHECK (role IN ('USER', 'ORGANIZER'))
);


CREATE TABLE venues (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(255) NOT NULL,
                        address VARCHAR(500) NOT NULL
);


CREATE TABLE seats (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       venue_id UUID NOT NULL,
                       seat_label VARCHAR(50) NOT NULL,
                       row_name VARCHAR(50) NOT NULL,
                       seat_number INTEGER NOT NULL,
                       status VARCHAR(20) NOT NULL,

                       CONSTRAINT fk_seat_venue
                           FOREIGN KEY (venue_id)
                               REFERENCES venues(id),

                       CONSTRAINT uk_seat_venue_label
                           UNIQUE (venue_id, seat_label),

                       CONSTRAINT chk_seat_status
                           CHECK (status IN ('ACTIVE', 'INACTIVE'))
);


CREATE TABLE events (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        name VARCHAR(255) NOT NULL,
                        description TEXT,

                        venue_id UUID NOT NULL,
                        organizer_id UUID NOT NULL,

                        starts_at TIMESTAMPTZ NOT NULL,
                        ends_at TIMESTAMPTZ NOT NULL,

                        status VARCHAR(20) NOT NULL,

                        CONSTRAINT fk_event_venue
                            FOREIGN KEY (venue_id)
                                REFERENCES venues(id),

                        CONSTRAINT fk_event_organizer
                            FOREIGN KEY (organizer_id)
                                REFERENCES users(id),

                        CONSTRAINT chk_event_status
                            CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),

                        CONSTRAINT chk_event_time
                            CHECK (ends_at > starts_at)
);


CREATE TABLE event_seats (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             event_id UUID NOT NULL,
                             seat_id UUID NOT NULL,

                             status VARCHAR(20) NOT NULL,

                             reserved_by UUID,
                             reserved_at TIMESTAMPTZ,
                             reservation_expires_at TIMESTAMPTZ,

                             CONSTRAINT fk_event_seat_event
                                 FOREIGN KEY (event_id)
                                     REFERENCES events(id),

                             CONSTRAINT fk_event_seat_seat
                                 FOREIGN KEY (seat_id)
                                     REFERENCES seats(id),

                             CONSTRAINT uk_event_seat
                                 UNIQUE (event_id, seat_id),

                             CONSTRAINT chk_event_seat_status
                                 CHECK (status IN ('AVAILABLE', 'RESERVED', 'BOOKED'))
);


CREATE INDEX idx_event_seats_event_id
    ON event_seats(event_id);

CREATE INDEX idx_event_seats_seat_id
    ON event_seats(seat_id);