-- ============================================================
-- V4 - Seed de resources (données de démonstration)
-- ============================================================

INSERT INTO resources (
    id,
    name,
    description,
    resource_type,
    capacity,
    active,
    accessibility_tags,
    deposit_amount_cents,
    image_url,
    created_at,
    updated_at,
    created_by,
    updated_by
)
VALUES
(
    gen_random_uuid(),
    'Salle des fêtes — Grande salle',
    'Grande salle pour événements jusqu''à 250 personnes',
    'IMMOBILIER',
    250,
    TRUE,
    '["PMR_ACCESS","PARKING","SOUND_SYSTEM"]',
    15000,
    'https://picsum.photos/seed/grande-salle/800/500',
    NOW(),
    NOW(),
    'seed',
    'seed'
),
(
    gen_random_uuid(),
    'Salle polyvalente — Petite salle',
    'Salle polyvalente pour réunions et activités associatives',
    'IMMOBILIER',
    80,
    TRUE,
    '["PMR_ACCESS"]',
    8000,
    'https://picsum.photos/seed/salle-polyvalente/800/500',
    NOW(),
    NOW(),
    'seed',
    'seed'
),
(
    gen_random_uuid(),
    'Vidéoprojecteur Epson EB-X51',
    'Vidéoprojecteur portable avec câble HDMI',
    'MOBILIER',
    NULL,
    TRUE,
    '[]',
    5000,
    'https://picsum.photos/seed/videoprojecteur/800/500',
    NOW(),
    NOW(),
    'seed',
    'seed'
),
(
    gen_random_uuid(),
    'Chaises pliantes (lot de 50)',
    'Lot de 50 chaises pliantes — retrait en mairie',
    'MOBILIER',
    NULL,
    TRUE,
    '[]',
    3000,
    'https://picsum.photos/seed/chaises-pliantes/800/500',
    NOW(),
    NOW(),
    'seed',
    'seed'
);
