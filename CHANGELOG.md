# Journal des changements — AGORA (backend)

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/).

## [Non publié] — 2026-04

### Ajouté

- **Espace administrateur** : gestion des utilisateurs (liste paginée, fiche, comptes tutorés, suspension / réactivation, impersonation, promotion support), des groupes (création / mise à jour / suppression, membres), des réservations (consultation, changement de statut, suivi des paiements / historique de caution), du tableau de bord (statistiques), des exports, du journal d’audit filtrable, et des **périodes de blackout** (indisponibilités calendaires).
- **Files d’attente** : API de liste d’attente sur les créneaux / ressources (entités, statuts, migrations associées).
- **Documents de réservation** : dépôt et consultation des pièces (contrats, justificatifs), avec tests d’intégration upload.
- **Activation de compte** : jetons d’activation (stockage dédié, service dédié) pour parcours sécurisé après inscription.
- **Super-administration** : services et endpoints de niveau super-admin (orchestration des opérations sensibles), en complément du rôle support.
- **Ressources** : champ **tarif de location** (`rental_price_cents`), exposé dans les DTO et persistant via migration Flyway.
- **Réservations** : prise en charge élargie (statuts admin, historique de dépôt, récurrence côté modèle / DTOs où applicable).
- **Auth / session** : enrichissement de `/me` (résumés de groupes, libellés de remise pour l’affichage catalogue), ajustements JWT et filtre d’authentification.
- **Migrations Flyway** (sans modification des migrations déjà appliquées) : remises liées aux réservations de groupe, tables documents et blackout, indicateur support admin, waitlist, tokens d’activation, colonnes de caution réservation côté admin, année de naissance utilisateur, **prix de location ressource**.

### Modifié

- **Sérialisation réservation** : désérialisation JSON plus tolérante pour les heures (`LocalTime`) afin d’accepter les chaînes envoyées par le front sans erreur Jackson.
- **Configuration** : sécurité (règles d’accès admin / super-admin), OpenAPI, journalisation d’audit, paramètres applicatifs de test.
- **Documentation** : README, inventaire des endpoints, schéma API exportable (`OpenApiExportTest`).

### Tests

- Nouveaux tests Web / intégration pour l’audit admin, les documents de réservation, l’export OpenAPI et les services métier associés ; suites existantes mises à jour (auth, réservations, ressources, JWT).

---

*Les versions suivantes reprendront un numéro sémantique (`MAJOR.MINOR.PATCH`) lors des releases taguées.*
