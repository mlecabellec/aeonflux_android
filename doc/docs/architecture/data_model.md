# AeonFlux Data Model

This document outlines the SQLite (via Room) database schema for the AeonFlux Android Client. The data model is designed to be offline-first, flexible for future extensions, and secure for storing secrets.

## Database Schema Diagram

Below is the Entity-Relationship diagram showing the tables, primary keys, foreign keys, and relationships.

```mermaid
erDiagram
    sources {
        string id PK
        string url
        string title
        string description
        string icon_url
        string source_type
        int refresh_interval_minutes
        long last_refreshed_at
        int is_contributed_to_gae
    }

    articles {
        string id PK
        string source_id FK
        string guid
        string title
        string content_raw
        string content_cleaned
        string author
        long published_at
        string url
        int is_read
        int is_bookmarked
        string media_url
        long media_duration_ms
        long playback_position_ms
        int is_downloaded
        string local_media_path
        string cached_html_path
        string ai_summary
        string transcript_text
    }

    keywords {
        string id PK
        string keyword
    }

    labels {
        string id PK
        string name
        string color
    }

    article_keywords {
        string article_id PK, FK
        string keyword_id PK, FK
    }

    article_labels {
        string article_id PK, FK
        string label_id PK, FK
    }

    properties {
        long id PK
        string entity_type
        string entity_id
        string property_key
        string property_value
        string data_type
    }

    source_secrets {
        string source_id PK, FK
        string secret_key PK
        string encrypted_value
        string iv
    }

    settings {
        string key PK
        string value
    }

    sources ||--o{ articles : "owns"
    articles ||--o{ article_keywords : "links"
    keywords ||--o{ article_keywords : "links"
    articles ||--o{ article_labels : "links"
    labels ||--o{ article_labels : "links"
    sources ||--o{ source_secrets : "secures"
```

### PlantUML Schema Diagram

```plantuml
@startuml
skinparam linetype ortho
skinparam monochrome true
skinparam packageStyle rectangle
skinparam shadowing false

entity "sources" as sources {
  * id : TEXT <<PK>>
  --
  * url : TEXT
  * title : TEXT
  description : TEXT
  icon_url : TEXT
  * source_type : TEXT
  refresh_interval_minutes : INTEGER
  last_refreshed_at : INTEGER
  is_contributed_to_gae : INTEGER
}

entity "articles" as articles {
  * id : TEXT <<PK>>
  --
  * source_id : TEXT <<FK>>
  * guid : TEXT
  * title : TEXT
  content_raw : TEXT
  content_cleaned : TEXT
  author : TEXT
  published_at : INTEGER
  * url : TEXT
  is_read : INTEGER
  is_bookmarked : INTEGER
  media_url : TEXT
  media_duration_ms : INTEGER
  playback_position_ms : INTEGER
  is_downloaded : INTEGER
  local_media_path : TEXT
  cached_html_path : TEXT
  ai_summary : TEXT
  transcript_text : TEXT
}

entity "keywords" as keywords {
  * id : TEXT <<PK>>
  --
  * keyword : TEXT <<UNIQUE>>
}

entity "labels" as labels {
  * id : TEXT <<PK>>
  --
  * name : TEXT <<UNIQUE>>
  color : TEXT
}

entity "article_keywords" as article_keywords {
  * article_id : TEXT <<PK, FK>>
  * keyword_id : TEXT <<PK, FK>>
}

entity "article_labels" as article_labels {
  * article_id : TEXT <<PK, FK>>
  * label_id : TEXT <<PK, FK>>
}

entity "properties" as properties {
  * id : INTEGER <<PK, AUTOINCREMENT>>
  --
  * entity_type : TEXT
  * entity_id : TEXT
  * property_key : TEXT
  property_value : TEXT
  * data_type : TEXT
}

entity "source_secrets" as source_secrets {
  * source_id : TEXT <<PK, FK>>
  * secret_key : TEXT <<PK>>
  --
  * encrypted_value : TEXT
  * iv : TEXT
}

entity "settings" as settings {
  * key : TEXT <<PK>>
  --
  value : TEXT
}

sources ||--o{ articles : "source_id"
articles ||--o{ article_keywords : "article_id"
keywords ||--o{ article_keywords : "keyword_id"
articles ||--o{ article_labels : "article_id"
labels ||--o{ article_labels : "label_id"
sources ||--o{ source_secrets : "source_id"

@enduml
```

## Table Definitions

### 1. `sources`
Stores information about RSS feeds, podcasts, Bluesky accounts, or other future source types.
* **`id`** (TEXT, PK): Unique identifier.
* **`url`** (TEXT): Ingestion endpoint/feed URL.
* **`title`** (TEXT): Source title.
* **`description`** (TEXT): Brief summary.
* **`icon_url`** (TEXT): Optional avatar/icon URL.
* **`source_type`** (TEXT): Enum representing source type (e.g. `'RSS'`, `'PODCAST'`, `'BLUESKY'`).
* **`refresh_interval_minutes`** (INTEGER): Ingestion refresh interval.
* **`last_refreshed_at`** (INTEGER): Epoch timestamp of the last fetch.
* **`is_contributed_to_gae`** (INTEGER): Boolean flag (0 or 1) indicating if source metadata is synchronized with backend.

### 2. `articles`
Stores items or articles ingested from sources.
* **`id`** (TEXT, PK): Unique identifier.
* **`source_id`** (TEXT, FK): Link to `sources(id)` with `ON DELETE CASCADE`.
* **`guid`** (TEXT): Original GUID from the feed.
* **`title`** (TEXT): Headline.
* **`content_raw`** (TEXT): Raw article contents.
* **`content_cleaned`** (TEXT): Cleaned/sanitized readability copy.
* **`author`** (TEXT): Author name.
* **`published_at`** (INTEGER): Publish timestamp.
* **`url`** (TEXT): Target link/website.
* **`is_read`** (INTEGER): Read status.
* **`is_bookmarked`** (INTEGER): Favorite status.
* **`media_url`** (TEXT): Direct audio stream URL (for Podcasts).
* **`media_duration_ms`** (INTEGER): Duration of audio.
* **`playback_position_ms`** (INTEGER): Exoplayer resume position.
* **`is_downloaded`** (INTEGER): Local cache status.
* **`local_media_path`** (TEXT): Path to downloaded audio files.
* **`cached_html_path`** (TEXT): Path to cached offline HTML file.
* **`ai_summary`** (TEXT): AI-generated digest.
* **`transcript_text`** (TEXT): Transcribed podcast audio text.

### 3. `keywords`
Keywords extracted from articles for tags, indexing, or search filters.
* **`id`** (TEXT, PK): Unique hash or UUID.
* **`keyword`** (TEXT, UNIQUE): Literal keyword text.

### 4. `labels`
Flexible tagging system managed manually via UI or automatically via AI rules.
* **`id`** (TEXT, PK): Unique ID.
* **`name`** (TEXT, UNIQUE): Name of the label (e.g., "AI Summary", "Important").
* **`color`** (TEXT): Hex color code.

### 5. `properties` (Entity-Attribute-Value Model)
Allows extending the fields of any primary object (`sources`, `articles`, `keywords`, `labels`) dynamically.
* **`id`** (INTEGER, PK, AUTOINCREMENT): Primary key.
* **`entity_type`** (TEXT): Type of target object (`'SOURCE'`, `'ARTICLE'`, `'KEYWORD'`, `'LABEL'`).
* **`entity_id`** (TEXT): The ID of the target object.
* **`property_key`** (TEXT): Name of the dynamic property.
* **`property_value`** (TEXT): Serialized string value.
* **`data_type`** (TEXT): Conversion type (`'STRING'`, `'INTEGER'`, `'BOOLEAN'`, `'DOUBLE'`).
* *Indices*: A unique constraint exists on `(entity_type, entity_id, property_key)` to prevent duplicate property definitions.

### 6. `source_secrets`
Stores sensitive, ciphered secrets (tokens, credentials, API keys) related to sources.
* **`source_id`** (TEXT, PK, FK): Link to `sources(id)` with `ON DELETE CASCADE`.
* **`secret_key`** (TEXT, PK): Secret label/identifier.
* **`encrypted_value`** (TEXT): Base64-encoded encrypted cipher.
* **`iv`** (TEXT): Base64-encoded initialization vector.

### 7. `settings`
App-wide key-value preferences.
* **`key`** (TEXT, PK): Configuration key.
* **`value`** (TEXT): Configuration value.

---

## Data Model Extensions and Extensibility
The `properties` table implements the Entity-Attribute-Value (EAV) design pattern. This allows adding custom fields to any source, article, keyword, or label without schema migrations. The `DatabaseService` wraps EAV queries into simple, typed getter/setter methods.

## Security & Secrets Management
Source credentials are encrypted locally using **AES/GCM/NoPadding** via the Android Keystore-backed key `AeonFluxMasterKey`. The initialization vector (IV) generated for each encryption is stored alongside the encrypted value in the `source_secrets` table.

Below is the byte/bit layout of an encrypted secret payload rendered via `mkdocs-kit` `bytefield` engine:

```bytefield
[
  {"name": "Initialization Vector (IV - 12 Bytes)", "bits": 12},
  {"name": "AES-256-GCM Encrypted Secret Payload", "bits": 32},
  {"name": "GCM Authentication Tag (16 Bytes)", "bits": 16}
]
```

