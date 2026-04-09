-- Tarif de location (centimes). NULL = non renseigne cote catalogue (libelle "a confirmer").

ALTER TABLE resources
    ADD COLUMN IF NOT EXISTS rental_price_cents DOUBLE PRECISION;
