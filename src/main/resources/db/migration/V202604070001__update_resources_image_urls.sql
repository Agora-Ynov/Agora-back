-- ============================================================
-- V202604070001 - Mise à jour des image_url des ressources seed
-- Remplace les URLs fictives (mairie-exemple.fr) par des images réelles
-- ============================================================

UPDATE resources SET image_url = 'https://picsum.photos/seed/grande-salle/800/500'
WHERE name = 'Salle des fêtes — Grande salle';

UPDATE resources SET image_url = 'https://picsum.photos/seed/salle-polyvalente/800/500'
WHERE name = 'Salle polyvalente — Petite salle';

UPDATE resources SET image_url = 'https://picsum.photos/seed/videoprojecteur/800/500'
WHERE name = 'Vidéoprojecteur Epson EB-X51';

UPDATE resources SET image_url = 'https://picsum.photos/seed/chaises-pliantes/800/500'
WHERE name = 'Chaises pliantes (lot de 50)';
