-- Drapeau métier : compte avec pouvoir ADMIN_SUPPORT (géré par superadmin API).

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS admin_support BOOLEAN NOT NULL DEFAULT FALSE;
