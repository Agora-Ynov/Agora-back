# Ticket C — API `/api/superadmin/admin-support` (contrat spec complet)

> **Implémenté dans le dépôt (2026-04)** : migration `V202604100001__user_admin_support_flag.sql`, `SuperadminAdminSupportService`, `GET|POST|DELETE` sur `SuperadminController`, `@PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN')")` pour compat ticket B en cours.

## Référence normative

Tout doit être **aligné** sur :

- `agora_api_endpoints_version_final.md` — **section 12. SUPERADMIN**  
- Prérequis sécurité : **rôle `SUPERADMIN`** dans le JWT et `@PreAuthorize` cohérents — voir `docs/ticket-b-superadmin-role.md`.

## Contrat à implémenter (récap)

| Méthode | Route | Auth | Comportement |
|--------|--------|------|--------------|
| `GET` | `/api/superadmin/admin-support` | SUPERADMIN | Liste des comptes **ADMIN_SUPPORT** actifs |
| `POST` | `/api/superadmin/admin-support` | SUPERADMIN | Body `{ "userId": "<uuid>" }` — promouvoir un **USER actif** en ADMIN_SUPPORT — **201** / **409** si déjà |
| `DELETE` | `/api/superadmin/admin-support/{userId}` | SUPERADMIN | Révoquer le rôle ADMIN_SUPPORT — **204** |

**Réponse GET 200** : tableau JSON d’objets comme dans la spec (champs : `id`, `email`, `firstName`, `lastName`, `status`).

**DTO existant** (à réutiliser ou étendre si la spec ajoute un champ) :

- `com.agora.dto.response.admin.AdminSupportUserDto`

## État actuel du back

- **`GET`** seulement, stub : `com.agora.controller.admin.SuperadminController#listAdminSupport` → `List.of()`.
- **`POST`** / **`DELETE`** : absents.
- **`@PreAuthorize`** : encore `SECRETARY_ADMIN` — à remplacer par la politique SUPERADMIN une fois le ticket B livré.
- **Modèle `User`** : pas de notion persistée **ADMIN_SUPPORT** (seulement `accountType` / `accountStatus`).

## Modèle de données (à trancher en équipe)

Deux approches possibles (documenter le choix dans la PR) :

1. **Colonne sur `users`**  
   Ex. `support_admin BOOLEAN NOT NULL DEFAULT FALSE` ou enum `app_role` / `staff_role` avec valeurs `NONE`, `ADMIN_SUPPORT` (VARCHAR comme les autres enums du projet).

2. **Table de liaison**  
   Ex. `user_staff_roles(user_id, role)` si plusieurs rôles staff futurs.

**Règles Flyway**

- Nouveau fichier **uniquement** dans `src/main/resources/db/migration/` (ne pas modifier une migration déjà mergée).
- Nom du type : `V2026MMDDnnn__admin_support_role.sql` (incrémenter après `git pull`).
- `ddl-auto: validate` : mettre à jour l’entité JPA dans `com.agora.entity.user` (ou package dédié si table séparée).

## Couches (architecture projet)

1. **Repository** : requêtes pour lister les users avec rôle ADMIN_SUPPORT + statut actif, find par id, etc.
2. **Service** `SuperadminAdminSupportService` (ou sous-package `service.admin`) :
   - `GET` : liste triée / filtres si spec précise.
   - `POST` : charger user par `userId`, vérifier **ACTIVE** (et pas déjà ADMIN_SUPPORT), puis attribuer rôle — `409` si conflit.
   - `DELETE` : retirer rôle — `404` si user inconnu, comportement si pas ADMIN_SUPPORT (idempotent 204 vs 404 : **coller à la spec**).
3. **Controller** : `SuperadminController` enrichi ou contrôleur dédié `SuperadminAdminSupportController` sous `/api/superadmin` (garder une seule classe si plus simple).

**DTO request** pour POST : créer `CreateAdminSupportRequestDto` ou nom équivalent dans `com.agora.dto.request.admin` avec `UUID userId` (ou `String` si spec montre `u001` — **aligner sur le doc**).

## Codes HTTP et erreurs

- Respecter la spec pour **201** (corps éventuel « profil mis à jour » — copier l’exemple du `.md` si présent).
- **409** : message métier clair (code `ErrorCode` existant ou nouveau dans `com.agora.exception.ErrorCode`).
- **403** : utilisateur authentifié mais pas SUPERADMIN (Spring Security).

## Tests

- `@WebMvcTest` sur les trois endpoints avec mock service + JWT / `@WithMockUser(roles = "SUPERADMIN")` selon config tests du projet.
- Tests service : promotion, double promotion → 409, révocation.

## Definition of Done

- [ ] Les **trois** endpoints exposés et conformes au fichier `agora_api_endpoints_version_final.md`.
- [ ] Migration Flyway + entité cohérente ; `./mvnw test` vert.
- [ ] Sécurité **SUPERADMIN** (pas seulement `SECRETARY_ADMIN`) sur ces routes, sauf phase documentée avec Ticket B.
- [ ] Aucune régression sur `/api/admin/*` pour un simple secrétaire sans rôle superadmin.

## Après merge (coordination front)

- Régénérer OpenAPI (`Agora-front` : `npm run openapi:fetch` + `openapi:generate`) et brancher l’UI — voir `Agora-front/docs/ticket-d-front-roles-and-superadmin-ui.md`.
