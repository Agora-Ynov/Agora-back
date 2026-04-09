CREATE TABLE waitlist (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users (id),
    resource_id  UUID         NOT NULL REFERENCES resources (id),
    slot_date    DATE         NOT NULL,
    slot_start   VARCHAR(10)  NOT NULL,
    slot_end     VARCHAR(10)  NOT NULL,
    position     INTEGER      NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    notified_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT waitlist_user_resource_slot_unique UNIQUE (user_id, resource_id, slot_date, slot_start, slot_end)
);

CREATE INDEX idx_waitlist_resource_slot ON waitlist (resource_id, slot_date, slot_start, slot_end);
