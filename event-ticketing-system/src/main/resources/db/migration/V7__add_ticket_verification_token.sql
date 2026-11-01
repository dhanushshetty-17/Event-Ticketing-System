ALTER TABLE tickets
    ADD COLUMN verification_token VARCHAR(255);

UPDATE tickets
SET verification_token = ticket_code
WHERE verification_token IS NULL;

ALTER TABLE tickets
    ALTER COLUMN verification_token SET NOT NULL;

ALTER TABLE tickets
    ADD CONSTRAINT uk_ticket_verification_token
        UNIQUE (verification_token);
