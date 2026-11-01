ALTER TABLE tickets
    ADD COLUMN scanned_at TIMESTAMPTZ,
    ADD COLUMN scanned_by VARCHAR(255);

ALTER TABLE tickets
    DROP CONSTRAINT chk_ticket_status;

ALTER TABLE tickets
    ADD CONSTRAINT chk_ticket_status
        CHECK (status IN ('CONFIRMED', 'ISSUED', 'CANCELLED', 'USED'));
