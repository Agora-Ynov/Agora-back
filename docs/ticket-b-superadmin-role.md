# Ticket B — Rôle SUPERADMIN (alignement spec + JWT)

## Contexte

La spec (`agora_api_endpoints_version_final.md`, section **12. SUPERADMIN**) impose l’accès **`SUPERADMIN`** pour les routes `/api/superadmin/*` (ex. `GET|POST|DELETE /api/superadmin/admin-support`).

Aujourd’hui le back utilise **`ROLE_SECRETARY_ADMIN`**, attribué uniquement si l’email du `User` correspond à `agora.auth.admin-email` (défaut `admin@agora.local`).

Ce ticket aligne **noms de rôles**, **JWT** et **`@PreAuthorize`** sans casser les écrans admin existants qui s’appuient déjà sur le compte “admin email”.

---

## État actuel (références code)

| Élément | Fichier | Comportement |
|--------|---------|----------------|
| Construction du JWT (rôles) | `src/main/java/com/agora/service/auth/JwtService.java` | Si `user.getEmail()` = `adminEmail` → `List.of("ROLE_SECRETARY_ADMIN")`, sinon `List.of()` |
| Superadmin HTTP | `src/main/java/com/agora/controller/admin/SuperadminController.java` | `@PreAuthorize("hasRole('SECRETARY_ADMIN')")` |
| Autres endpoints admin | `src/main/java/com/agora/controller/admin/*.java` | Idem `hasRole('SECRETARY_ADMIN')` |
| Config admin email | `src/main/resources/application.yml` (clé `agora.auth.admin-email`) | Identifie qui reçoit le rôle admin dans le token |

Spring Security : `hasRole('SECRETARY_ADMIN')` vérifie l’autorité **`ROLE_SECRETARY_ADMIN`**.

---

## Objectif produit

1. Exposer dans le JWT un rôle explicite **`ROLE_SUPERADMIN`** pour le(s) compte(s) concernés par la spec (au minimum : même périmètre que l’admin email actuel, sauf décision métier contraire).
2. Restreindre **`SuperadminController`** (routes sous `/api/superadmin`) à **`hasRole('SUPERADMIN')`** (ou `hasAnyRole` pendant une phase de transition — voir ci‑dessous).
3. **Ne pas** retirer `ROLE_SECRETARY_ADMIN` du JWT tant que les contrôleurs `/api/admin/*` ne sont pas migrés vers une politique unifiée — **recommandation** : pour **un seul commit cohérent**, ajouter **les deux** rôles sur le même utilisateur “admin email” :
   - `ROLE_SUPERADMIN` → accès `/api/superadmin/**`
   - `ROLE_SECRETARY_ADMIN` → accès `/api/admin/**` (inchangé jusqu’à ticket ultérieur)

   Ainsi aucun écran front admin ne perd l’accès.

---

## Périmètre technique (à implémenter)

### B.1 — `JwtService`

**Fichier :** `JwtService.java`, méthode `generateAccessToken(User user)`.

- Lorsque l’email correspond à `adminEmail`, construire la liste des rôles avec **au minimum** :
  - `ROLE_SECRETARY_ADMIN` (comportement actuel),
  - **`ROLE_SUPERADMIN`** (nouveau).
- Ordre des strings indifférent ; éviter les doublons.

Pseudo-attendu :

```java
List<String> roles = isAdminEmail(user)
    ? List.of("ROLE_SECRETARY_ADMIN", "ROLE_SUPERADMIN")
    : List.of();
```

(Si plus tard un autre mécanisme BDD détermine le superadmin, remplacer `isAdminEmail` par la règle métier.)

### B.2 — `SuperadminController`

**Fichier :** `SuperadminController.java`

- Remplacer `@PreAuthorize("hasRole('SECRETARY_ADMIN')")` par  
  `@PreAuthorize("hasRole('SUPERADMIN')")`  
  sur `GET /api/superadmin/admin-support`.

**Phase de transition (optionnel, si peur de régression)** :  
`@PreAuthorize("hasAnyRole('SUPERADMIN','SECRETARY_ADMIN')")` — à retirer une fois les tokens en prod tous régénérés avec `ROLE_SUPERADMIN`. Préférer la version stricte si équipe petite et pas de vieux tokens longue durée.

### B.3 — Tests

- **Tests unitaires / config sécu** qui fabriquent un JWT avec des rôles : mettre à jour les attentes si un test vérifie la liste exacte des rôles (ex. ajouter `ROLE_SUPERADMIN` là où on mock le token admin).
- Rechercher dans `src/test` : `ROLE_SECRETARY_ADMIN`, `SuperadminController`, `generateAccessToken`.
- Faire tourner : `./mvnw test`.

### B.4 — Front / OpenAPI (si déjà généré à partir des annotations)

- Si le client Angular dépend du schéma de sécurité : régénérer OpenAPI après changement **uniquement si** la doc Swagger change ; sinon pas obligatoire pour ce ticket.
- Vérifier qu’un utilisateur non admin ne peut pas appeler `GET /api/superadmin/admin-support` (403).

---

## Hors périmètre (tickets séparés)

- Remplir le corps de `GET /api/superadmin/admin-support` (liste réelle des `ADMIN_SUPPORT`) — **Ticket A / données**.
- Implémenter `POST` et `DELETE` admin-support selon spec.
- Stockage BDD des rôles utilisateur (`ADMIN_SUPPORT`, etc.) — migration Flyway dédiée.

---

## Definition of Done

- [ ] JWT admin email contient **`ROLE_SUPERADMIN`** (+ conservation de `ROLE_SECRETARY_ADMIN` si `/api/admin` reste sur ce rôle).
- [ ] `SuperadminController` protégé par **`hasRole('SUPERADMIN')`** (ou transition documentée).
- [ ] `./mvnw test` vert.
- [ ] Description PR : alignement section 12 spec ; mention que les vieux tokens sans `ROLE_SUPERADMIN` ne passent plus sur `/api/superadmin` (forcer re-login si besoin).

---

## Message de commit (convention projet)

```
fix(auth): expose ROLE_SUPERADMIN for superadmin routes
```

ou, si scope large :

```
refactor(auth): align JWT superadmin with API spec section 12
```

Ajuster le `type` (`feat` / `fix` / `refactor`) selon ce que la PR fait réellement.
