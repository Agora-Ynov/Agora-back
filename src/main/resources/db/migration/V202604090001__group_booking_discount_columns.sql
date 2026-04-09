-- Règles de réservation et tarification par groupe (spec agora_api_endpoints_version_final)

ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS can_book_immobilier BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS can_book_mobilier BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS discount_type VARCHAR(40) NOT NULL DEFAULT 'NONE';

ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS discount_value INT NOT NULL DEFAULT 0;

ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS discount_applies_to VARCHAR(40) NOT NULL DEFAULT 'ALL';

-- Valeurs métier alignées sur la seed (noms exacts)
UPDATE groups
SET can_book_immobilier = FALSE,
    can_book_mobilier   = FALSE,
    discount_type       = 'NONE',
    discount_value      = 0,
    discount_applies_to = 'ALL'
WHERE name IN ('Public', 'Défaut');

UPDATE groups
SET can_book_immobilier = TRUE,
    can_book_mobilier   = FALSE,
    discount_type       = 'PERCENTAGE',
    discount_value      = 50,
    discount_applies_to = 'ALL'
WHERE name = 'Habitants commune';

UPDATE groups
SET can_book_immobilier = TRUE,
    can_book_mobilier   = TRUE,
    discount_type       = 'FULL_EXEMPT',
    discount_value      = 0,
    discount_applies_to = 'ALL'
WHERE name IN ('Conseillers municipaux', 'Association sportive locale');

UPDATE groups
SET can_book_immobilier = TRUE,
    can_book_mobilier   = TRUE,
    discount_type       = 'PERCENTAGE',
    discount_value      = 30,
    discount_applies_to = 'ALL'
WHERE name = 'Personnel mairie';
