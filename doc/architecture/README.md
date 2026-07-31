# AeonFlux Android Architecture Index

This directory details the client architecture.

## Architecture Guidelines

* **Clean Architecture & MVVM:** Separating responsibilities into UI/Presentation, Use Cases, and Data sources.
* **Reactive Database Pattern:** Room database acts as the single source of truth (SSOT). All UI feeds observe database tables via Java `LiveData`.
* **ExoPlayer Service:** Native audio playback runs in a persistent background service (`PlaybackService`) implementing Media3 `MediaLibraryService`.

## Documents

* [clean_code_rules.md](../governance/README.md) - Design patterns and governance rules.
