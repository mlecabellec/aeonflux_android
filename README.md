# AeonFlux Android Client

Unified offline-first content aggregator client written in Java for Android Native. It acts as an RSS reader, podcast player, and Bluesky client with integrated local data persistence and AI capabilities.

## Features & Specifications

Based on the [AeonFlux specifications](doc/specifications/aeon_flux.md):

* **Multi-Source Aggregation (RSS, Podcasts, Bluesky):**
  * Subscribe using URL (RSS/Atom), podcast XML feeds, or Bluesky actor handles/did identities.
  * Local tags/categories mapping.
  * Custom tags edit, icons, and displays.
* **Offline-First Resilience:**
  * Uses Room database as the Single Source of Truth (SSOT).
  * Auto-caching of article pages using JSoup and WebKit interceptors (`shouldInterceptRequest`) to load archived versions when offline.
  * Battery and network-aware synchronization using background WorkManager.
* **Native Audio & Podcast Player:**
  * Background playback service (`PlaybackService`) implementing Media3 `MediaLibraryService`.
  * Resume playback position, queue management, skip silences, and variable speed support.
* **AI & Cloud Integrations:**
  * Text-to-Speech (TTS) voice generation using Google Cloud TTS and fallback Android native engine.
  * Generative AI summarizing with Gemini.

## Tech Stack & Architecture

* **Language:** Java 17+
* **UI Paradigm:** View system with XML layouts and View Binding.
* **Architecture:** Model-View-ViewModel (MVVM) with LiveData.
* **Dependency Injection:** Dagger Hilt.
* **Local Database:** Room Database with Full-Text Search (FTS5).
* **Network Client:** Retrofit / OkHttp.
* **Media Player:** AndroidX Media3 / ExoPlayer.

## Project Structure

```
aeonflux_android/
├── .antigravity/            # Antigravity agent configuration and governance rules
├── app/
│   ├── build.gradle         # App-level build file
│   └── src/
│       ├── main/
│       │   ├── java/com/aeonflux/app/
│       │   │   ├── core/
│       │   │   │   ├── database/    # Room database, Entities (FeedEntity, FeedItemEntity), and DAOs
│       │   │   │   ├── media/       # Media3 ExoPlayer PlaybackService
│       │   │   │   └── network/     # Network connections & Parsers
│       │   │   └── ui/              # Main Activities, ViewModels, and Layout bindings
│       │   ├── res/
│       │   │   ├── layout/          # Layout XMLs (activity_main.xml)
│       │   │   └── values/          # Strings, colors, styles, and themes
│       │   └── AndroidManifest.xml
│       └── test/                # Unit & Integration tests
├── doc/                     # Structured documentation
├── build.gradle             # Project-level build configuration
├── gradle.properties        # Gradle properties
├── LICENSE                  # Apache 2.0 License
└── settings.gradle          # Settings gradle descriptor
```

## Getting Started

1. Open this directory in **Android Studio**.
2. Sync the project with Gradle files.
3. Build and run the `app` module on a device or emulator.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
