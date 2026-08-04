# Technical and Functional Specifications Document & Kick-Off Engine
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
1. [Introduction & Project Vision](#1-introduction--project-vision)
2. [Tech Stack & Environment Recommendations](#2-tech-stack--environment-recommendations)
3. [Detailed Functional Specifications (Requirements Matrix)](#3-detailed-functional-specifications-requirements-matrix)
4. [System Architecture & API Specifications](#4-system-architecture--api-specifications)
5. [Project Structure Specifications & Antigravity CLI Governance](#5-project-structure-specifications--antigravity-cli-governance)
6. [Quality Standards, Test Strategy & Formal Quality Assurance](#6-quality-standards-test-strategy--formal-quality-assurance)
7. [Roadmap & Kick-Off Instructions for Antigravity CLI](#7-roadmap--kick-off-instructions-for-antigravity-cli)

---

## 1. Introduction & Project Vision

### 1.1 General Objective
The project aims to design and implement a sovereign and unified software ecosystem allowing the aggregation, consumption, AI-enriched processing, and collaborative discovery of heterogeneous information feeds. This ecosystem combines:
1. **A native Android client application** prioritizing UX smoothness, offline consumption, precise background refresh control, and the execution of accessibility/artificial intelligence tools (text-to-speech, STT, text summarization via Google Cloud APIs / Gemini).
2. **A highly scalable cloud backend on Google App Engine (GAE)** serving as an anonymized and collaborative catalog of RSS feeds, podcasts, and Bluesky sources, guaranteeing real-time indexing, deduplication, and search.

### 1.2 Architectural Guiding Principles
- **Zero Lock-in & Privacy:** Contribution to the centralized catalog is anonymized. The link between a local user account and specific subscriptions hosted in App Engine is strictly decoupled at the cloud datastore level.
- **Offline-First & Resilience-First:** The mobile application works without degradation in offline mode. All data (articles, podcast metadata, Bluesky posts, sanitized HTML web caches) are orchestrated via a *Single Source of Truth* (SSOT) pattern powered by a local Room database and synchronized in the background.
- **Modularity & Formal Quality:** Codebase written according to Clean Architecture and SOLID standards, with systematic test coverage (Unit, Integration, UI, Dynamic Analysis).

---

## 2. Tech Stack & Environment Recommendations

### 2.1 Android Ecosystem (Native Client)
- **Primary Language:** Kotlin 2.0+ (K2 Compiler enabled, Explicit API mode for core modules).
- **User Interface (UI):** Jetpack Compose (Material 3 UI, Dynamic Color, Adaptive Layouts for tablets and smartphones).
- **UI Layer Architecture:** MVVM / Unidirectional Data Flow (UDF) with `StateFlow` and `SharedFlow`.
- **Dependency Injection:** Hilt / Dependency Injection (Dagger Hilt 2.50+).
- **Local Persistence:** Room Database 2.6+ (with FTS5 support for local full-text search and optional SQLCipher encryption).
- **Network & Parsing:**
  - Ktor Client 2.x or Retrofit 2.11 + OkHttp 4.12.
  - RSS/Atom Parsing: High-performance custom XML Pull Parser + FeedKit parser.
  - Bluesky Protocol: AT Protocol SDK Kotlin (Bluesky API Client via AT Proto Lexicons REST/WebSocket).
- **Media & Audio (Podcasts):** Media3 / ExoPlayer 1.3+ (Background service `MediaLibraryService`, native media notification, audio focus management, resume playback, variable speed control `0.5x` to `3.0x`, silence skipping).
- **WebView & Offline Caching:** AndroidX WebKit + Custom `WebViewClient` intercepting requests to serve them from the pre-loaded local cache (ServiceWorker + OkHttp Cache Interceptor / local HTML storage sanitized by JSoup).
- **Background Task Scheduling:** WorkManager 2.9+ (Periodic Work Request, network and battery constraints).
- **AI Services & Google APIs:**
  - **Text-to-Speech (TTS):** Google Cloud Text-to-Speech API (Neural2 / Journey voices) & Fallback Android Native TTS Engine.
  - **Speech-to-Text (STT):** Google Cloud Speech-to-Text V2 API for audio podcast transcription.
  - **Generative AI / Summarization:** Google GenAI SDK / Vertex AI REST API (Gemini 1.5 Flash/Pro) for summary generation, keyword extraction, and Web enrichment.

### 2.2 Google App Engine Backend Ecosystem (Cloud Platform)
- **Runtime Environment:** Google App Engine Standard Environment (Python 3.11 / 3.12 runtime).
- **Web Backend Framework:** FastAPI (Async native, Pydantic v2 validation, auto-generated OpenAPI documentation).
- **Process Manager / ASGI Server:** Uvicorn / Gunicorn.
- **Data Storage (Datastore):**
  - **Primary Storage:** Google Cloud Firestore in Native or Datastore Mode (High availability, automatic scaling to zero).
  - **Search & Indexing:** Firestore Vector Search / Algolia / Cloud Search or native Datastore Inverted Index indexing for multi-criteria full-text search.
  - **Cache:** Memorystore for Redis (caching of search endpoints and URL deduplication).
- **Asynchrony & Batching:** Google Cloud Tasks + Cloud Pub/Sub for asynchronous processing of contributed feeds (HTTP validity check, MP3 enclosure parsing, OG image extraction).
- **Security & Authentication:** Anonymized client API Keys with Hashing / Google Identity Platform rotating token or App Check to ensure that the GAE API is only queried by genuine mobile application instances (Anti-Scraping / Anti-Spam).

### 2.3 Development Tooling & DevOps
- **Recommended IDEs:** Android Studio Koala / Ladybug (2024.x+) & PyCharm Professional / VS Code for the GAE backend.
- **Versioning & CI/CD:** Git (GitFlow / GitHub Actions for automated builds, ktlint, detekt, ruff, mypy, pytest, and Android UI Tests via Gradle Managed Devices).
- **AI Orchestration:** Antigravity CLI with custom rules configurations (`.antigravity/rules`).

---

## 3. Detailed Functional Specifications (Requirements Matrix)

The requirements are structured according to the following codification:
- `FEAT-xxxx`: High-level feature
- `REQ-APP-xxxx`: Functional requirement for Android Client
- `REQ-GAE-xxxx`: Functional requirement for Google App Engine Backend
- `REQ-QUAL-xxxx`: Quality / Non-functional requirement

### 3.1 Feed & Subscription Management (RSS, Podcasts, Bluesky)

#### [FEAT-001] Unified Multi-Source Aggregation
- **REQ-APP-00101:** The Android application must provide a unified add screen allowing subscription to a feed by entering either a direct RSS/Atom link, a Podcast RSS feed URL, a Bluesky handle/feed (`@handle` or `did:plc:...`), or by performing a search in the GAE catalog.
- **REQ-APP-00102:** The application must validate the structure of the entered URL and perform auto-discovery (automatic detection of `<link rel="alternate" type="application/rss+xml">` tags for websites).
- **REQ-APP-00103:** For each subscription, the user must be able to assign one or more labels (customizable tags/categories e.g., "Tech", "News", "Audio Podcasts", "Dev").
- **REQ-APP-00104:** The user must be able to modify the display name, custom icon, and description of each subscription.
- **REQ-APP-00105:** The user must be able to delete a subscription. The deletion must offer the choice to: delete only the feed (keep read history) or completely purge the associated history.

#### [FEAT-002] Granular Refresh Configuration
- **REQ-APP-00201:** The application must allow defining a global refresh policy (e.g., every 30 min, 1h, 6h, 24h, or manual only).
- **REQ-APP-00202:** The application must allow overriding the global policy at the individual feed level (e.g., refreshing a Bluesky feed every 15 minutes and a long Podcast every 24 hours).
- **REQ-APP-00203:** The application must integrate energy and data-saving rules: option to limit heavy downloads (podcast audio files, HTML pre-cache) to Wi-Fi and charging state only.

#### [FEAT-003] GAE Catalog Querying & Anonymous Synchronization
- **REQ-APP-00301:** The application must anonymously submit the URL of any new subscription added by the user to the Google App Engine backend API.
- **REQ-APP-00302:** Feed submission to the GAE catalog must contain NO personal identifier (no Android ID, no email, no personal token). A rotating anonymous device token or general application API key will be used.
- **REQ-APP-00303:** The application must integrate a search engine in the UI querying the GAE catalog in real-time (autocompletion, filtering by type: RSS, Podcast, Bluesky, sorting by anonymous popularity).

#### [FEAT-004] Native Bluesky Feed Management (AT Protocol)
- **REQ-APP-00401:** The application must allow subscribing to public Bluesky "Feeds" as well as specific user post feeds without requiring a Bluesky account (reading via the public Bluesky XRPC API `/xrpc/app.bsky.feed.getAuthorFeed` or `/xrpc/app.bsky.feed.getFeed`).
- **REQ-APP-00402:** If the user provides their Bluesky credentials (optional), the application must handle OAuth/App Password authentication to retrieve private feeds and allow basic interactions (liking, reposting).

---

### 3.2 Reading Experience & UI (RSS, Podcasts, Bluesky, WebView)

#### [FEAT-005] Reading & Aggregation of RSS Articles
- **REQ-APP-00501:** The RSS UI must present a unified or label-filtered list of received articles, sorted chronologically or reverse-chronologically.
- **REQ-APP-00502:** Articles must be markable as "Read / Unread", "Favorite / Read Later".
- **REQ-APP-00503:** The detailed view of an article must support "Reader view" (cleaning the Web layout for an optimal reading comfort with customization of font, text size, line spacing, and dark/light theme).

#### [FEAT-006] Podcast Reader & Engine (Media3 / ExoPlayer)
- **REQ-APP-00601:** The Podcast UI must offer a dedicated audio interface with a persistent mini-player at the bottom of the screen and a full-screen player (album art, progress slider, +10s/-30s skip controls).
- **REQ-APP-00602:** The application must manage a dynamic playback queue (Playlist) with drag-and-drop reordering and automatic episode chaining (*Autoplay*).
- **REQ-APP-00603:** Management of playback resumption (*Resume*) down to the millisecond, memorized in the local Room database.
- **REQ-APP-00604:** Offline downloading of podcast MP3/AAC files with storage manager (automatic deletion of finished episodes after X days).

#### [FEAT-007] Advanced WebView & Automatic Pre-caching
- **REQ-APP-00701:** For each RSS article or Bluesky post containing an external Web URL, the UI must allow opening via an integrated custom WebView.
- **REQ-APP-00702:** **Automatic caching:** Upon receiving an article in the background, the WorkManager service must download the associated HTML page and its critical resources (images, CSS) to the local application storage if the option is enabled.
- **REQ-APP-00703:** The WebView must intercept network requests (`shouldInterceptRequest`) to preferentially load archived local resources in offline mode.

---

### 3.3 AI Services & Google Cloud APIs Integration

#### [FEAT-008] Podcast Audio Transcription (Speech-to-Text)
- **REQ-APP-00801:** The user must be able to request transcription of a podcast episode.
- **REQ-APP-00802:** For short audio files, the transcription can be sent to the Google Cloud Speech-to-Text V2 API. The application displays the synchronized text under the episode (like interactive subtitles).
- **REQ-APP-00803:** Text search within the transcribed audio stream: clicking on a sentence will immediately transfer audio playback to the corresponding timestamp.

#### [FEAT-009] Text-to-Speech of Articles
- **REQ-APP-00901:** The user must be able to listen to the text of an RSS article or a Bluesky post via the TTS engine.
- **REQ-APP-00902:** Dual integration: local Android TTS (free, offline) and Google Cloud TTS API (high-quality Neural2/Journey voices) if a user API key or backend quota is configured.
- **REQ-APP-00903:** The flow of articles read by TTS must be insertable into the same global queue as the audio podcasts.

#### [FEAT-010] Smart Summarization & Content Enrichment (Gemini APIs)
- **REQ-APP-01001:** The application must provide a "Summarize" button generating a synthetic summary (bullet points of key ideas) of any RSS article, long Bluesky post, or podcast transcription.
- **REQ-APP-01002:** **Web Enrichment:** The application must be able to extract the main link of an article, perform a light background scraping request, and use the Gemini API to cross-reference the article content with the linked page content to generate a contextualized summary note.

---

### 3.4 Google App Engine (GAE) Backend Specifications

#### [FEAT-011] Catalog Ingestion, Validation, and Deduplication
- **REQ-GAE-01101:** The GAE API must expose a POST `/api/v1/catalog/submit` endpoint receiving feed URL proposals from Android clients.
- **REQ-GAE-01102:** The backend must execute an asynchronous task (via Cloud Tasks) to validate the reachability of the URL, verify the conformity of its structure (XML RSS/Atom, Bluesky feed, or valid audio Podcast feed), and extract metadata (title, image, description, canonical category).
- **REQ-GAE-01103:** The backend must proceed with strict deduplication of URLs (scheme normalization, removal of UTM parameters and tracking tokens).

#### [FEAT-012] Catalog Search Engine and Statistics
- **REQ-GAE-01201:** The GAE API must expose a GET `/api/v1/catalog/search` endpoint allowing text search with pagination, filtering by feed type (RSS, Podcast, Bluesky), and sorting (popularity/number of anonymous subscriptions, relevance).
- **REQ-GAE-01202:** The backend must maintain an incremental counter of anonymous subscriptions per feed to determine popular community feeds.

---

## 4. System Architecture & API Specifications

### 4.1 Client Data Model (Android Room Database Schema)

```sql
-- Subscriptions table (Feeds)
CREATE TABLE feeds (
    id TEXT PRIMARY KEY NOT NULL, -- UUID or Hash URL
    url TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT,
    icon_url TEXT,
    feed_type TEXT NOT NULL, -- 'RSS', 'PODCAST', 'BLUESKY'
    refresh_interval_minutes INTEGER NOT NULL DEFAULT 60,
    last_refreshed_at INTEGER,
    custom_tags TEXT, -- JSON Array or relation Many-To-Many
    is_contributed_to_gae INTEGER NOT NULL DEFAULT 0
);

-- Articles / episodes / posts table
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
    -- Specific to Podcasts
    media_url TEXT,
    media_duration_ms INTEGER,
    playback_position_ms INTEGER DEFAULT 0,
    is_downloaded INTEGER NOT NULL DEFAULT 0,
    local_media_path TEXT,
    -- Specific to Web Cache & AI
    cached_html_path TEXT,
    ai_summary TEXT,
    transcript_text TEXT,
    FOREIGN KEY(feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);

CREATE INDEX idx_items_feed_id ON feed_items(feed_id);
CREATE INDEX idx_items_published ON feed_items(published_at DESC);
```

### 4.2 Rest API Contracts (App Engine Backend)

#### Endpoint 1: Anonymous Feed Submission
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

#### Endpoint 2: Search in the Common Catalog
- **HTTP Method:** `GET /api/v1/catalog/search`
- **Query Parameters:**
  - `q` (string, optional): Search keyword (title, description).
  - `type` (string, optional): `RSS`, `PODCAST`, `BLUESKY` or `ALL`.
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
      "description": "The daily news of cutting-edge technologies.",
      "feed_type": "RSS",
      "icon_url": "https://example.com/icon.png",
      "subscriber_count": 1420,
      "verified": true
    }
  ]
}
```

---

## 5. Project Structure Specifications & Antigravity CLI Governance

To orchestrate development via the autonomous agent **Antigravity CLI**, the mobile and backend projects must follow a rigorous directory structure including the `.antigravity/` folder containing engineering rules, the knowledge base, and context memory.

### 5.1 Global Structure of the Multi-Module Repository

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

### 5.2 Antigravity CLI Governance Rules (`.antigravity/rules/`)

#### File: `.antigravity/rules/01-kotlin-android-rules.md`
```markdown
# Android Development Guidelines for Antigravity Agent

1. **Architecture:** Mandatory implementation of the Clean Architecture MVVM pattern with Jetpack Compose. No business logic in Composables.
2. **Explicit API Mode:** Any component exposable in the `core` package must specify its visibility and explicit return types.
3. **Coroutines & Flow:** Never use `GlobalScope`. Always inject a `CoroutineDispatcher` (Dispatchers.IO, Dispatchers.Default) via Dependency Injection.
4. **State Management:** Use sealed classes/interfaces (`sealed interface UIState<out T>`) to represent UI states (Loading, Success, Error).
5. **Zero Hardcoded Strings:** All UI strings must reside in `strings.xml`.
```

#### File: `.antigravity/rules/02-python-gae-rules.md`
```markdown
# Google App Engine Python Backend Guidelines for Antigravity Agent

1. **Async FastAPI:** Prefer asynchronous functions `async def` for all I/O-bound endpoints.
2. **Strict Typing:** Enable `mypy --strict` verification. No function should omit type annotations for arguments and return values.
3. **Pydantic V2:** Use Pydantic for JSON request and response validation.
4. **Error Handling:** Capture exceptions and return RFC 7807 standardized responses (Problem Details).
```

---

## 6. Quality Standards, Testing Strategy & Formal Quality Assurance

### 6.1 Test Coverage Strategy (Android Client)
- **Coverage Goal:** Minimum **85% line code coverage** on `core` and `feature` modules (ViewModels, Repositories, Parsers).
- **Unit Tests (JUnit 5 + MockK + Turbine):**
  - Validation of XML RSS/Atom parsing on edge cases (malformed files, UTF-8/ISO-8859-1 encodings).
  - Validation of ViewModels' state management using Turbine to test `StateFlow` streams.
- **Database Tests (Room In-Memory Test Runner):**
  - Validation of DAO queries, schema migrations, and FTS5 search.
- **UI & Integration Tests (Jetpack Compose Testing + Espresso):**
  - UI interaction tests for podcast queue and smooth scrolling of RSS articles.
- **Static Analysis:**
  - Mandatory execution of `detekt` (cyclomatic complexity < 10) and `ktlint` before any commit.

### 6.2 Test Coverage Strategy (GAE Backend)
- **Coverage Goal:** Minimum **90% code coverage** via `pytest-cov`.
- **API Integration Tests (Pytest + HTTPX AsyncClient):**
  - Validation of each REST route with valid data, malformed data, and malicious query injection.
- **Static Analysis & Quality:**
  - `ruff` for ultra-fast linting and code formatting.
  - `mypy` with strict configuration (`disallow_untyped_defs = true`).

---

## 7. Roadmap & Kick-Off Instructions for Antigravity CLI

To initialize the project via the Antigravity CLI agent, execute the following instructions:

### Step 1: Directory Tree & Governance Files Initialization
```bash
# Antigravity Command Sequence
antigravity init-project --template=clean-architecture-android-gae
```

### Step 2: Generation of Core Modules (Android)
```bash
antigravity Agent: "Generate core-database (Room), core-network (RSS/Bluesky), and core-media (ExoPlayer) modules according to the rules defined in .antigravity/rules/01-kotlin-android-rules.md."
```

### Step 3: Google App Engine Backend Launch
```bash
antigravity Agent: "Generate the FastAPI backend in gae-backend/ with the submission and search endpoints of the common catalog according to .antigravity/rules/02-python-gae-rules.md."
```
