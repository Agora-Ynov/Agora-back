# AGORA - Backend de reservation de ressources

Backend Spring Boot pour la gestion des reservations de ressources municipales (projet scolaire Ynov).

## Prerequis

| Outil | Version minimale |
|---|---|
| Java | 21 |
| Docker Desktop | recent |

## Lancement local

### 1) Démarrer la stack Docker (PostgreSQL + backend)

```bash
docker compose up -d
```

Reconstruire l’image backend après changement de code :

```bash
docker compose up -d --build
```

Rebuild complet sans cache (image JAR recalculée de zéro — plus long) :

```bash
docker compose build --no-cache
docker compose up -d
```

### Commandes Docker utiles

Les ports exposés dépendent de ton fichier `.env` (ex. `BACKEND_PORT`, `DB_PORT`).

| Action | Commande |
|--------|----------|
| État des conteneurs | `docker compose ps` |
| Logs backend (flux continu) | `docker compose logs backend -f` |
| Dernières lignes backend | `docker compose logs backend --tail 100` |
| Arrêter sans supprimer les volumes | `docker compose down` |
| Arrêter et supprimer les images du projet | `docker compose down --rmi all` |
| **Réinitialiser la base** (supprime le volume Postgres — perte des données) | `docker compose down -v` puis `docker compose up -d` |

Shell PostgreSQL dans le conteneur : voir la section **Accès à la base de données (PostgreSQL)** plus bas.

### Alternative: lancer Spring Boot en local (en gardant Postgres via Docker)

- Démarre uniquement la DB :

```powershell
docker compose up -d postgres
```

- Ou en 1 commande via les scripts :
  - macOS/Linux : `./scripts/dev-run-local.sh`
  - Windows (PowerShell) : `.\scripts\dev-run-local.ps1`

- Charge les variables `.env` puis démarre Spring Boot.
  - Sur macOS/Linux (bash/zsh) :

```bash
set -a && source .env && set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

  - Sur Windows (PowerShell) :

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*#') { return }
  if ($_ -match '^\s*$') { return }
  $kv = $_.Split('=',2)
  [System.Environment]::SetEnvironmentVariable($kv[0], $kv[1])
}
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## Verification

- Health (Actuator): `http://127.0.0.1:${BACKEND_PORT:-8081}/actuator/health` (ajuster le port selon `.env`)
- Swagger UI: `http://127.0.0.1:${BACKEND_PORT:-8081}/swagger-ui/index.html` (chemin exact : voir `springdoc` si besoin)

## Variables d'environnement

Voir `.env.example`.

## Resolution de problemes

- Port déjà pris (8080/5432): change `BACKEND_PORT` / `DB_PORT` dans `.env` (ou copie `.env.example` → `.env`).
- Windows: si tu as une erreur de bind sur `127.0.0.1:8080` ("access permissions"), mets `BIND_ADDR=0.0.0.0` dans `.env` (ou change `BACKEND_PORT`).
- Flyway / migrations incohérentes après des changements d'historique: reset du volume DB dev (voir aussi la table *Commandes Docker utiles* plus haut) :

```bash
docker compose down -v
docker compose up -d
```

- JAVA_HOME manquant: definis `JAVA_HOME` ou garde Java accessible via `PATH`.

## Tests et qualité

### Tests unitaires et d’intégration

```bash
./mvnw test
```

Les tests `@Tag("security-web")` et les tests d’intégration sont inclus dans le même module ; la configuration Surefire écrit les logs détaillés dans `target/surefire-reports/`.

### Couverture de code (JaCoCo)

Après les tests, le rapport HTML est généré lors de la phase `verify` :

```bash
./mvnw verify
open target/site/jacoco/index.html   # macOS
```

Un résumé CSV est disponible : `target/site/jacoco/jacoco.csv`.

**Contrôle automatique** : `jacoco:check` impose un ratio minimal de lignes sur le bundle (propriété `jacoco.bundle.line.minimum`, par défaut ~0,52) et des **seuils élevés** sur les packages `com.agora.service.impl.*` déjà bien couverts (≥85–95 % selon le package). Toute régression fait échouer `verify`.

Les classes sous `com.agora.config.seed` sont exclues du rapport (données de démo).

### Tests de mutation (PIT)

PIT est activé via le profil Maven `pitest` (périmètre restreint : services liste d’attente / superadmin admin-support, formatage remise, désérialiseur d’heure).

```bash
./mvnw -P pitest test-compile org.pitest:pitest-maven:mutationCoverage
open target/pit-reports/index.html
```

Seuils configurés dans le profil : couverture des lignes mutées et score de mutation (voir `pom.xml`). En cas d’échec, le rapport HTML indique les mutants survivants.

### Objectif de couverture globale

La couverture **globale** du dépôt (~50 % lignes hors exclusions) augmente avec les tests admin/API ; l’objectif documenté pour la soutenance est **≥90 % sur les services métier critiques** (`service.impl`, règles JaCoCo par package ci-dessus).

## Architecture

```
com.agora
 |- controller   -> REST endpoints (DTO, validation HTTP)
 |- service      -> Logique metier + transactions
 |- repository   -> Acces aux donnees (Spring Data JPA)
 |- entity       -> Entites JPA
 |- dto          -> Data Transfer Objects
 |- mapper       -> MapStruct mappers
 |- config       -> Configuration Spring (Security, OpenAPI)
 |- exception    -> Gestion globale des erreurs
```
## Monitoring (Zabbix)

Le backend intègre un agent Zabbix pour le monitoring.

### Configuration
Modifier les variables d'environnement dans le fichier `compose.yaml` (ou `.env`) :
- `ZBX_SERVER_HOST`: IP du serveur Zabbix.
- `ZBX_HOSTNAME`: `agora-app` (par défaut).

### Accès
- **Port** : `10050` doit être accessible depuis le serveur Zabbix.
- **Endpoints Actuator** :
  - Health : `http://localhost:${BACKEND_PORT:-8081}/actuator/health`
  - OpenAPI : `http://localhost:${BACKEND_PORT:-8081}/v3/api-docs`

## Accès à la base de données (PostgreSQL)

### Via Docker (CLI)
Pour ouvrir un shell `psql` directement dans le conteneur :
```powershell
docker exec -it agora-db psql -U agora -d agora
```

Quelques commandes utiles :
- `\dt` : Lister les tables.
- `select count(*) from users;` : Compter les utilisateurs.
- `\q` : Quitter psql.

### Via un client externe (DBeaver, DataGrip, pgAdmin)
- **Host** : `127.0.0.1`
- **Port** : `${DB_PORT}` (par défaut `5432`)
- **Database** : `${DB_NAME}` (par défaut `agora`)
- **User** : `${DB_USER}` (par défaut `agora`)
- **Password** : `${DB_PASS}` (par défaut `agora`)

Toutes ces valeurs sont définies dans votre fichier `.env` à la racine du projet.