ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS admin_comment TEXT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS deposit_status VARCHAR(50) NOT NULL DEFAULT 'DEPOSIT_PENDING';

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS payment_mode VARCHAR(50);

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS payment_comment TEXT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS check_number VARCHAR(100);

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS deposit_updated_at TIMESTAMPTZ;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS deposit_updated_by_name VARCHAR(255);

CREATE TABLE deposit_payment_history (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id  UUID         NOT NULL REFERENCES reservations (id),
    status          VARCHAR(50)  NOT NULL,
    amount_cents    INTEGER      NOT NULL,
    payment_mode    VARCHAR(50),
    check_number    VARCHAR(100),
    comment         TEXT,
    updated_by_name VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deposit_history_reservation ON deposit_payment_history (reservation_id, updated_at DESC);
