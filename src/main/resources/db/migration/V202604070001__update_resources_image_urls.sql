-- ============================================================
-- V202604070001 - Mise à jour des image_url des ressources seed
-- Remplace les URLs fictives (mairie-exemple.fr) par des images réelles
-- ============================================================

UPDATE resources SET image_url = 'https://www.team-business-centers.com/wp-content/uploads/2021/07/Location-salle-de-r%C3%A9union-design-Paris-8-570x500.jpg'
WHERE name = 'Salle des fêtes — Grande salle';

UPDATE resources SET image_url = 'https://picsum.photos/seed/salle-polyvalente/800/500'
WHERE name = 'Salle polyvalente — Petite salle';

UPDATE resources SET image_url = 'https://www.team-business-centers.com/wp-content/uploads/2021/07/Location-salle-de-r%C3%A9union-design-Paris-8-570x500.jpg'
WHERE name = 'Vidéoprojecteur Epson EB-X51';

UPDATE resources SET image_url = 'https://www.team-business-centers.com/wp-content/uploads/2021/07/Location-salle-de-r%C3%A9union-design-Paris-8-570x500.jpg'
WHERE name = 'Chaises pliantes (lot de 50)';
