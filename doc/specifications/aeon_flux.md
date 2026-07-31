# Document de Spécifications Techniques et Fonctionnelles & Kick-Off Engine
## Unified Feed Manager (RSS, Podcasts, Bluesky)
### Multi-Platform System Architecture: Android Native App & Google App Engine (GAE) Backend

---

## Document Control & Metadata
* **Project Codename:** Project Aether Feed / Unified Content Hub
* **Document Version:** 1.0.0-DRAFT-COMPLETE
* **Target Execution CLI:** Antigravity AI Autonomous Coding System
* **Primary Target Stack:** Android Native (Kotlin, Jetpack Compose, Material 3, Room, WorkManager) + Google App Engine Standard (Python 3.11+, FastAPI, Cloud Datastore / Firestore, Cloud Tasks)
* **Author / Architect:** System Architecture & Enterprise Engineering Group
* **Date:** July 2026

---

## Table of Contents
1. [Introduction & Vision Project](#1-introduction--vision-project)
2. [Stack Technique & Recommandations d'Environnement](#2-stack-technique--recommandations-denvironnement)
3. [Spécifications Fonctionnelles Détaillées (Matrice d'Exigences)](#3-spécifications-fonctionnelles-détaillées-matrice-dexigences)
4. [Architecture Système & Spécifications des API](#4-architecture-système--spécifications-des-api)
5. [Spécifications de Structure de Projets & Gouvernance Antigravity CLI](#5-spécifications-de-structure-de-projets--gouvernance-antigravity-cli)
6. [Normes Qualité, Stratégie de Test & Assurance Qualité Formelle](#6-normes-qualité-stratégie-de-test--assurance-qualité-formelle)
7. [Feuille de Route & Instructions de Kick-Off pour Antigravity CLI](#7-feuille-de-route--instructions-de-kick-off-pour-antigravity-cli)

---

## 1. Introduction & Vision Project

### 1.1 Objectif Général
Le projet vise à concevoir et implémenter un écosystème logiciel souverain et unifié permettant l'agrégation, la consommation, le traitement IA enrichi et la découverte participative de flux d'information hétérogènes. Cet écosystème combine :
1. **Une application client native Android** priorisant la fluidité UX, la consommation hors-ligne, le contrôle précis du rafraîchissement d'arrière-plan et l'exécution d'outils d'accessibilité/intelligence artificielle (synthèse vocale, STT, résumé textuel via Google Cloud APIs / Gemini).
2. **Un backend cloud haute scalabilité sur Google App Engine (GAE)** servant de catalogue anonymisé et contributif de flux RSS, podcasts et sources Bluesky, garantissant l'indexation, la déduplication et la recherche temps réel.

### 1.2 Principes Directeurs Architecturels
- **Zero Lock-in & Confidentialité :** La contribution au catalogue centralisé est anonymisée. Le lien entre un compte utilisateur local et les abonnements spécifiques hébergés dans l'App Engine est rigoureusement dissocié au niveau des datastores cloud.
- **Offline-First & Resilience-First :** L'application mobile fonctionne sans dégradation en mode déconnecté. Toutes les données (articles, métadonnées de podcast, posts Bluesky, caches web HTML sanitizés) sont orchestrées via un pattern *Single Source of Truth* (SSOT) alimenté par une base de données locale Room et synchronisé en tâche de fond.
- **Modularité & Qualité Formelle :** Code base rédigée selon les standards Clean Architecture, SOLID, avec couverture de test systématique (Unit, Integration, UI, Dynamic Analysis).

---

## 2. Stack Technique & Recommandations d'Environnement

### 2.1 Ecosystem Android (Client Native)
- **Langage principal :** Kotlin 2.0+ (K2 Compiler enabled, Explicit API mode pour modules core).
- **Interface Homme-Machine (IHM) :** Jetpack Compose (Material 3 UI, Dynamic Color, Adaptive Layouts pour tablettes et smartphones).
- **Architecture de couche UI :** MVVM / Unidirectional Data Flow (UDF) avec `StateFlow` et `SharedFlow`.
- **Injection de Dépendances :** Hilt / Dependency Injection (Dagger Hilt 2.50+).
- **Persistance locale :** Room Database 2.6+ (avec support FTS5 pour recherche textuelle locale plein texte et chiffrement SQLCipher optionnel).
- **Réseau & Parsing :**
  - Ktor Client 2.x ou Retrofit 2.11 + OkHttp 4.12.
  - Parsing RSS/Atom : XML Pull Parser haute performance custom + parser FeedKit.
  - Protocole Bluesky : AT Protocol SDK Kotlin (Bluesky API Client via AT Proto Lexicons REST/WebSocket).
- **Média & Audio (Podcasts) :** Media3 / ExoPlayer 1.3+ (Service d'arrière-plan `MediaLibraryService`, notification multimédia native, gestion du focus audio, reprise de lecture, contrôle de vitesse variable `0.5x` à `3.0x`, saut de silences).
- **Webview & Offline Caching :** AndroidX WebKit + Custom `WebViewClient` interceptant les requêtes pour servir depuis le cache local pré-chargé (ServiceWorker + OkHttp Cache Interceptor / Storage local HTML sanitizé par JSoup).
- **Planification des tâches d'arrière-plan :** WorkManager 2.9+ (Periodic Work Request, contraintes réseau et batterie).
- **Services IA & Google APIs :**
  - **Text-to-Speech (TTS) :** Google Cloud Text-to-Speech API (voix Neural2 / Journey) & Fallback Android Native TTS Engine.
  - **Speech-to-Text (STT) :** Google Cloud Speech-to-Text V2 API pour la transcription de podcasts audio.
  - **Generative AI / Summarization :** SDK Google GenAI / Vertex AI REST API (Gemini 1.5 Flash/Pro) pour la génération de résumés, extraction de mots-clés et enrichissement Web.

### 2.2 Ecosystem Google App Engine Backend (Cloud Platform)
- **Environnement d'exécution :** Google App Engine Standard Environment (Python 3.11 / 3.12 runtime).
- **Framework Web Backend :** FastAPI (Async native, validation Pydantic v2, documentation OpenAPI auto-générée).
- **Gestionnaire de process / Serveur ASGI :** Uvicorn / Gunicorn.
- **Stockage de Données (Datastore) :**
  - **Primary Storage :** Google Cloud Firestore en mode Native ou Datastore Mode (Haute disponibilité, scaling automatique à zéro).
  - **Search & Indexing :** Firestore Vector Search / Algolia / Cloud Search ou indexation Inverted Index native Datastore pour recherche plein texte multi-critères.
  - **Cache :** Memorystore pour Redis (Caching des endpoints de recherche et déduplication des URLs).
- **Asynchronisme & Batching :** Google Cloud Tasks + Cloud Pub/Sub pour le traitement asynchrone des flux contribués (vérification de validité HTTP, parsing des enclosures MP3, extraction d'images OG).
- **Sécurité & Authentification :** API Keys client anonymisées avec Hashing / Token de rotation Google Identity Platform ou App Check pour garantir que l'API GAE n'est interrogée que par des instances d'applications mobiles authentiques (Anti-Scraping / Anti-Spam).

### 2.3 Outillage de Développement & DevOps
- **IDE Recommandés :** Android Studio Koala / Ladybug (2024.x+) & PyCharm Professional / VS Code pour le backend GAE.
- **Versionning & CI/CD :** Git (GitFlow / GitHub Actions pour build automatisé, ktlint, detekt, ruff, mypy, pytest, et Android UI Tests via Gradle Managed Devices).
- **Orchestration IA :** Antigravity CLI avec configurations de règles personnalisées (`.antigravity/ rules`).

---

## 3. Spécifications Fonctionnelles Détaillées (Matrice d'Exigences)

Les exigences sont structurées selon la codification :
- `FEAT-xxxx` : Fonctionnalité de haut niveau
- `REQ-APP-xxxx` : Exigence fonctionnelle Android Client
- `REQ-GAE-xxxx` : Exigence fonctionnelle Backend Google App Engine
- `REQ-QUAL-xxxx` : Exigence de qualité / Non-fonctionnelle

### 3.1 Gestion des Abonnements & Flux (RSS, Podcasts, Bluesky)

#### [FEAT-001] Agrégation Multi-Source Unifiée
- **REQ-APP-00101 :** L'application Android doit fournir un écran d'ajout unifié permettant de souscrire à un flux en saisissant soit un lien direct RSS/Atom, une URL d'un feed RSS de Podcast, un identifiant/feed Bluesky (`@handle` ou `did:plc:...`), ou en effectuant une recherche dans le catalogue GAE.
- **REQ-APP-00102 :** L'application doit valider la structure de l'URL saisie et exécuter un auto-discovery (détection automatique des balises `<link rel="alternate" type="application/rss+xml">` pour les sites Web).
- **REQ-APP-00103 :** Pour chaque abonnement, l'utilisateur doit pouvoir attribuer une ou plusieurs étiquettes (tags/catégories personnalisables ex: "Tech", "Actualités", "Podcasts Audio", "Dev").
- **REQ-APP-00104 :** L'utilisateur doit pouvoir modifier le nom d'affichage, l'icône personnalisée et la description de chaque abonnement.
- **REQ-APP-00105 :** L'utilisateur doit pouvoir supprimer un abonnement. La suppression doit offrir le choix entre : supprimer uniquement le flux (conserver l'historique lu) ou dépuré totalement l'historique associé.

#### [FEAT-002] Paramétrage Granulaire du Rafraîchissement
- **REQ-APP-00201 :** L'application doit permettre de définir une politique de rafraîchissement globale (ex: toutes les 30 min, 1h, 6h, 24h, ou manuel uniquement).
- **REQ-APP-00202 :** L'application doit permettre de surcharger la politique globale au niveau de chaque flux individuel (ex: rafraîchir un flux Bluesky toutes les 15 minutes et un Podcast long toutes les 24h).
- **REQ-APP-00203 :** L'application doit intégrer des règles d'économie d'énergie et de données : option pour limiter les téléchargements lourds (fichiers audio de podcasts, pré-cache HTML) au Wi-Fi et en charge électrique uniquement.

#### [FEAT-003] Interrogation du Catalogue GAE & Synchronisation Anonyme
- **REQ-APP-00301 :** L'application doit soumettre anonymement l'URL de tout nouvel abonnement ajouté par l'utilisateur à l'API du backend Google App Engine.
- **REQ-APP-00302 :** La soumission d'un flux au catalogue GAE ne doit contenir AUCUN identifiant personnel (Pas d'Android ID, pas d'email, pas de jeton nominatif). Un jeton de device anonyme tournant ou une clé API d'application générale sera utilisé.
- **REQ-APP-00303 :** L'application doit intégrer un moteur de recherche dans l'IHM interrogeant le catalogue GAE en temps réel (autocomplétion, filtrage par type : RSS, Podcast, Bluesky, tri par popularité anonyme).

#### [FEAT-004] Gestion Native des Flux Bluesky (AT Protocol)
- **REQ-APP-00401 :** L'application doit permettre de s'abonner aux "Feeds" publics Bluesky ainsi qu'aux fils de posts d'utilisateurs spécifiques sans imposer la possession d'un compte Bluesky (lecture via l'API publique XRPC de Bluesky `/xrpc/app.bsky.feed.getAuthorFeed` ou `/xrpc/app.bsky.feed.getFeed`).
- **REQ-APP-00402 :** Si l'utilisateur renseigne ses identifiants Bluesky (optionnel), l'application doit gérer l'authentification OAuth/App Password pour récupérer les flux privés et permettre les interactions de base (liker, repost).

---

### 3.2 Expérience de Lecture & IHM (RSS, Podcasts, Bluesky, Webview)

#### [FEAT-005] Lecture & Agrégation des Articles RSS
- **REQ-APP-00501 :** L'IHM RSS doit présenter une liste unifiée ou filtrée par étiquette des articles reçus, triés par date chronologique ou anti-chronologique.
- **REQ-APP-00502 :** Les articles doivent pouvoir être marqués comme "Lu / Non lu", "Favori / À lire plus tard".
- **REQ-APP-00503 :** L'affichage détaillé d'un article doit supporter le mode "Reader view" (nettoyage du layout Web pour un confort de lecture optimal avec personnalisation de la police, taille de texte, interligne et thème sombre/clair).

#### [FEAT-006] Liseur & Moteur de Podcasts (Media3 / ExoPlayer)
- **REQ-APP-00601 :** L'IHM Podcast doit proposer une interface dédiée audio avec mini-lecteur persistant en bas d'écran et écran plein lecteur (Pochette d'album, slider de progression, contrôles de saut +10s/-30s).
- **REQ-APP-00602 :** L'application doit gérer une file d'attente de lecture dynamique (Playlist) avec réordonnancement par drag-and-drop et enchaînement automatique des épisodes (*Autoplay*).
- **REQ-APP-00603 :** Gestion de la reprise de lecture (*Resume*) au milliseconde près, mémorisée dans la base Room locale.
- **REQ-APP-00604 :** Téléchargement hors-ligne des fichiers MP3/AAC de podcasts avec gestionnaire de stockage (suppression automatique des épisodes terminés après X jours).

#### [FEAT-007] WebView Avancée & Pré-cache Automatique
- **REQ-APP-00701 :** Pour chaque article RSS ou post Bluesky contenant une URL Web externe, l'IHM doit permettre l'ouverture via une WebView personnalisée intégrée.
- **REQ-APP-00702 :** **Mise en cache automatique :** Dès réception d'un article en tâche d'arrière-plan, le service WorkManager doit télécharger la page HTML associée et ses ressources critiques (images, CSS) dans le stockage d'application local si l'option est activée.
- **REQ-APP-00703 :** La WebView doit intercepter les requêtes réseau (`shouldInterceptRequest`) pour charger préférentiellement les ressources locales archivées en mode hors-ligne.

---

### 3.3 Services IA & Intégration Google Cloud APIs

#### [FEAT-008] Transcription Audio des Podcasts (Speech-to-Text)
- **REQ-APP-00801 :** L'utilisateur doit pouvoir demander la transcription d'un épisode de podcast.
- **REQ-APP-00802 :** Pour les fichiers audio courts, la transcription peut être envoyée à l'API Google Cloud Speech-to-Text V2. L'application affiche le texte synchronisé sous l'épisode (façon sous-titres interactifs).
- **REQ-APP-00803 :** Recherche textuelle au sein du flux audio transcrit : cliquer sur une phrase transféra immédiatement la lecture audio au timestamp correspondant.

#### [FEAT-009] Synthèse Vocale des Articles (Text-to-Speech)
- **REQ-APP-00901 :** L'utilisateur doit pouvoir écouter le texte d'un article RSS ou d'un post Bluesky via le moteur TTS.
- **REQ-APP-00902 :** Intégration duale : TTS local Android (gratuit, hors-ligne) et Google Cloud TTS API (voix haute qualité Neural2/Journey) si une clé API utilisateur ou un quota backend est configuré.
- **REQ-APP-00903 :** Les flux d'articles lus par TTS doivent pouvoir s'insérer dans la même file d'attente globale que les podcasts audio.

#### [FEAT-010] Résumés Intelligents & Enrichissement de Contenu (Gemini APIs)
- **REQ-APP-01001 :** L'application doit fournir un bouton "Résumer" générant un condensé synthétique (Bullet points des idées clés) de tout article RSS, long post Bluesky ou transcription de podcast.
- **REQ-APP-01002 :** **Enrichissement Web :** L'application doit pouvoir extraire le lien principal d'un article, effectuer une requête de scraping léger en tâche de fond, et utiliser l'API Gemini pour croiser le contenu de l'article avec le contenu de la page liée afin de générer une note de synthèse contextualisée.

---

### 3.4 Spécifications du Backend Google App Engine (GAE)

#### [FEAT-011] Ingestion, Validation et Déduplication du Catalogue
- **REQ-GAE-01101 :** L'API GAE doit exposer un endpoint POST `/api/v1/catalog/submit` recevant les propositions d'URL de flux de la part des clients Android.
- **REQ-GAE-01102 :** Le backend doit exécuter une tâche asynchrone (via Cloud Tasks) pour valider la joignabilité de l'URL, vérifier la conformité de sa structure (XML RSS/Atom, feed Bluesky ou flux de Podcast audio valide) et extraire les métadonnées (titre, image, description, catégorie canonique).
- **REQ-GAE-01103 :** Le backend doit procéder à une déduplication stricte des URLs (normalisation des schémas, suppression des paramètres UTM et jetons de tracking).

#### [FEAT-012] Moteur de Recherche et Statistiques du Catalogue
- **REQ-GAE-01201 :** L'API GAE doit exposer un endpoint GET `/api/v1/catalog/search` permettant la recherche textuelle avec pagination, filtrage par type de flux (RSS, Podcast, Bluesky) et tri (popularité/nombre de souscriptions anonymes, pertinence).
- **REQ-GAE-01202 :** Le backend doit tenir à jour un compteur incrémental d'abonnements anonymes par flux pour déterminer les flux populaires de la communauté.

---

## 4. Architecture Système & Spécifications des API

### 4.1 Modèle de Données Client (Android Room Database Schema)

```sql
-- Table des abonnements (Feeds)
CREATE TABLE feeds (
    id TEXT PRIMARY KEY NOT NULL, -- UUID ou Hash URL
    url TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT,
    icon_url TEXT,
    feed_type TEXT NOT NULL, -- 'RSS', 'PODCAST', 'BLUESKY'
    refresh_interval_minutes INTEGER NOT NULL DEFAULT 60,
    last_refreshed_at INTEGER,
    custom_tags TEXT, -- JSON Array ou relation Many-To-Many
    is_contributed_to_gae INTEGER NOT NULL DEFAULT 0
);

-- Table des articles / épisodes / posts
CREATE TABLE feed_items (
    id TEXT PRIMARY KEY NOT NULL,
    feed_id TEXT NOT NULL,
    guid TEXT NOT NULL,
    title TEXT NOT NULL,
    content_raw TEXT,
    content_cleaned TEXT,
    author TEXT,
    published_at INTEGER NOT NULL,
    url TEXT NOT NULL,
    is_read INTEGER NOT NULL DEFAULT 0,
    is_bookmarked INTEGER NOT NULL DEFAULT 0,
    -- Spécifique Podcasts
    media_url TEXT,
    media_duration_ms INTEGER,
    playback_position_ms INTEGER DEFAULT 0,
    is_downloaded INTEGER NOT NULL DEFAULT 0,
    local_media_path TEXT,
    -- Spécifique Cache Web & IA
    cached_html_path TEXT,
    ai_summary TEXT,
    transcript_text TEXT,
    FOREIGN KEY(feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);

CREATE INDEX idx_items_feed_id ON feed_items(feed_id);
CREATE INDEX idx_items_published ON feed_items(published_at DESC);
```

### 4.2 Rest API Contracts (App Engine Backend)

#### Endpoint 1: Soumission Anonyme d'un Flux
- **HTTP Method:** `POST /api/v1/catalog/submit`
- **Request Headers:**
  - `Content-Type: application/json`
  - `X-App-Check-Token: <Firebase_App_Check_Token>`
- **Request Body:**
```json
{
  "feed_url": "https://example.com/podcast.xml",
  "declared_type": "PODCAST" // Enum: RSS, PODCAST, BLUESKY
}
```
- **Response Status:** `202 Accepted`
- **Response Body:**
```json
{
  "status": "QUEUED",
  "message": "Feed URL queued for validation and catalog ingestion.",
  "task_id": "task_abc123890"
}
```

#### Endpoint 2: Recherche dans le Catalogue Commun
- **HTTP Method:** `GET /api/v1/catalog/search`
- **Query Parameters:**
  - `q` (string, optionnel) : Mot-clé de recherche (titre, description).
  - `type` (string, optionnel) : `RSS`, `PODCAST`, `BLUESKY` ou `ALL`.
  - `page` (int, default: 1)
  - `limit` (int, default: 20, max: 100)
- **Response Status:** `200 OK`
- **Response Body:**
```json
{
  "total": 42,
  "page": 1,
  "limit": 20,
  "results": [
    {
      "catalog_id": "gae_cat_99823",
      "url": "https://example.com/tech-feed.xml",
      "title": "Tech Daily News",
      "description": "L'actualité quotidienne des technologies de pointe.",
      "feed_type": "RSS",
      "icon_url": "https://example.com/icon.png",
      "subscriber_count": 1420,
      "verified": true
    }
  ]
}
```

---

## 5. Spécifications de Structure de Projets & Gouvernance Antigravity CLI

Pour orchestrer le développement via l'agent autonome **Antigravity CLI**, les projets mobile et backend doivent suivre une arborescence rigoureuse incluant le répertoire `.antigravity/` contenant les règles d'ingénierie, la base de connaissance et la mémoire de contexte.

### 5.1 Arborescence Globale du Repository Multi-Module

```
/ unified-feed-system /
├── .antigravity /
│   ├── rules /
│   │   ├── 01-kotlin-android-rules.md
│   │   ├── 02-python-gae-rules.md
│   │   ├── 03-architecture-clean-code.md
│   │   └── 04-testing-enforcement.md
│   ├── knowledge_base /
│   │   ├── domain_models.md
│   │   ├── at_protocol_spec.md
│   │   └── gae_datastore_schema.md
│   └── context_memory.md
├── android-app /
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── app /
│   │   ├── build.gradle.kts
│   │   └── src /
│   │       ├── main /
│   │       │   ├── java / com / feed / app /
│   │       │   │   ├── core /
│   │       │   │   │   ├── database / (Room DB, Entities, Daos)
│   │       │   │   │   ├── network / (Retrofit/Ktor, Bluesky, RSS)
│   │       │   │   │   ├── media / (ExoPlayer Service)
│   │       │   │   │   └── ai / (Gemini, STT, TTS Clients)
│   │       │   │   ├── feature /
│   │       │   │   │   ├── feed_management /
│   │       │   │   │   ├── rss_reader /
│   │       │   │   │   ├── podcast_player /
│   │       │   │   │   ├── bluesky_timeline /
│   │       │   │   │   └── webview_cache /
│   │       │   │   └── ui / (Theme, Navigation, Components)
│   │       │   └── AndroidManifest.xml
│   │       └── test / (Unit tests, Mocks)
│   │       └── androidTest / (Compose UI tests, Room tests)
└── gae-backend /
    ├── app.yaml
    ├── requirements.txt
    ├── main.py
    ├── app /
    │   ├── api / (FastAPI Routers)
    │   ├── core / (Config, Security, App Check)
    │   ├── models / (Pydantic & Datastore entities)
    │   ├── services / (Ingestion, Catalog search, Worker tasks)
    │   └── tasks / (Cloud Tasks handlers)
    └── tests / (Pytest, End-to-End API tests)
```

### 5.2 Règles de Gouvernance Antigravity CLI (`.antigravity/rules/`)

#### Fichier : `.antigravity/rules/01-kotlin-android-rules.md`
```markdown
# Directives Développement Android pour Antigravity Agent

1. **Architecture :** Implémenter obligatoirement le pattern Clean Architecture MVVM avec Jetpack Compose. Aucun code métier dans les Composables.
2. **Explicit API Mode :** Tout composant exposable dans le package `core` doit spécifier sa visibilité et ses types de retour explicites.
3. **Coroutines & Flow :** Ne jamais utiliser `GlobalScope`. Toujours injecter des `CoroutineDispatcher` (Dispatchers.IO, Dispatchers.Default) via Dependency Injection.
4. **State Management :** Utiliser des classes scellées (`sealed interface UIState<out T>`) pour représenter les états (Loading, Success, Error).
5. **Zero Hardcoded Strings :** Toutes les chaînes de caractères IHM doivent résider dans `strings.xml`.
```

#### Fichier : `.antigravity/rules/02-python-gae-rules.md`
```markdown
# Directives Backend Google App Engine Python pour Antigravity Agent

1. **Async FastApi :** Préférer des fonctions asynchrones `async def` pour tous les endpoints I/O Bound.
2. **Typage Strict :** Activer la vérification `mypy --strict`. Aucune fonction ne doit omettre les annotations de type pour les arguments et le retour.
3. **Pydantic V2 :** Utiliser Pydantic pour la validation des requêtes et réponses JSON.
4. **Gestion des Erreurs :** Capturer les exceptions et retourner des réponses standardisées RFC 7807 (Problem Details).
```

---

## 6. Normes Qualité, Stratégie de Test & Assurance Qualité Formelle

### 6.1 Stratégie de Couverture de Tests (Android Client)
- **Objectif de Couverture :** Minimum **85% de couverture de code par lignes** sur les modules `core` et `feature` (ViewModels, Repositories, Parsers).
- **Tests Unitaires (JUnit 5 + MockK + Turbine) :**
  - Validation du parsing XML RSS/Atom sur des cas limites (fichiers malformés, encodages UTF-8/ISO-8859-1).
  - Validation du state management des ViewModels à l'aide de Turbine pour tester les flux `StateFlow`.
- **Tests de Base de Données (Room In-Memory Test Runner) :**
  - Validation des requêtes DAO, des migrations de schémas et de la recherche FTS5.
- **Tests UI & Integration (Jetpack Compose Testing + Espresso) :**
  - Tests d'interaction IHM pour la file d'attente de podcast et le défilement fluide des articles RSS.
- **Analyse Statique :**
  - Execution obligatoire de `detekt` (complexité cyclomatique < 10) et `ktlint` avant tout commit.

### 6.2 Stratégie de Couverture de Tests (GAE Backend)
- **Objectif de Couverture :** Minimum **90% de couverture de code** via `pytest-cov`.
- **Tests d'Intégration API (Pytest + HTTPX AsyncClient) :**
  - Validation de chaque route REST avec des données valides, malformées, et injection de requêtes malveillantes.
- **Analyse Statique & Qualité :**
  - `ruff` pour le linter ultra-rapide et le formateur de code.
  - `mypy` avec configuration stricte (`disallow_untyped_defs = true`).

---

## 7. Feuille de Route & Instructions de Kick-Off pour Antigravity CLI

Pour initialiser le projet via l'agent Antigravity CLI, exécuter les instructions suivantes :

### Étape 1 : Initialisation de l'Arborescence & Fichiers de Gouvernance
```bash
# Antigravity Command Sequence
antigravity init-project --template=clean-architecture-android-gae
```

### Étape 2 : Lancement de la Génération des Core Modules (Android)
```bash
antigravity Agent: "Génère les modules core-database (Room), core-network (RSS/Bluesky), et core-media (ExoPlayer) selon les règles définies dans .antigravity/rules/01-kotlin-android-rules.md."
```

### Étape 3 : Lancement du Backend Google App Engine
```bash
antigravity Agent: "Génère le backend FastAPI dans gae-backend/ avec les endpoints de soumission et de recherche du catalogue commun selon .antigravity/rules/02-python-gae-rules.md."
```
