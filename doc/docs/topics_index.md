# Navigation by Developer Topics

This index organizes the AeonFlux Android Client documentation by developer topics and operational areas. Use this page as a starting point depending on your discipline.

---

## 📱 Topic A: Reactive MVVM & DB Binding
Focuses on UI components, Room database as Single Source of Truth (SSOT), LiveData reactivity, and Dagger Hilt dependency injection.
- [Architecture Overview](architecture/README.md): Details on Clean Architecture layers, reactive database pattern, and packages setup.
- [App Database Handler](file:///home/m/git/aeonflux_android/app/src/main/java/com/aeonflux/app/core/database/AppDatabase.java): Database class definitions.
- [Feed Data Access Object (DAO)](file:///home/m/git/aeonflux_android/app/src/main/java/com/aeonflux/app/core/database/daos/FeedDao.java): Room entity queries.

---

## 🎧 Topic B: Background Media Playback
Focuses on persistent background service executions, audio sample handling, and Android Media3 ExoPlayer configurations.
- [ExoPlayer Service](architecture/README.md#architecture-guidelines): Background playback service using `MediaLibraryService`.
- [PlaybackService implementation](file:///home/m/git/aeonflux_android/app/src/main/java/com/aeonflux/app/core/media/PlaybackService.java): ExoPlayer wrapper.

---

## 🧪 Topic C: Project Rules & Guidelines
Focuses on general code quality, AI agent guidelines, and Java coding standards.
- [Rules Index](architecture/rules_index.md): Entry point for project rules.
- [CS-0010 Quality Standards](architecture/CS-0010.md): Traceability and comment formats.
- [CS-0020 AI Agent Rules](architecture/CS-0020.md): Rules for agent contributions and testing.
- [CS-0030 Java Standards](architecture/CS-0030.md): Parameter defense, null-safety, and OOP patterns.

---

## 🛠️ Topic D: Project Operations & Task Management
Focuses on repository build commands, project roadmap milestones, and task logs.
- [Project Tasks Overview](project/milestones/README.md): Milestones roadmap.
- [Active Documentation Task](project/tasks/TSK-20260804-001.md): First documentation set up task.
- [MainActivity UI & Feeds Tree Task](project/tasks/TSK-20260805-001.md): MainActivity dual drawer, gestures, ItemViewActivity, and Settings task.
- [OPML Import/Export & Background RSS Fetching Task](project/tasks/TSK-20260806-001.md): OPML parsing/exporting, UI drawer commands, and WorkManager RSS fetch task.
- [ItemViewActivity Embedded & Private WebViews Feature](specifications/FR-20260809-001.md): Action buttons, WebViewActivity, PrivateWebViewActivity with JS toggle & privacy hardening.
- [Dynamic Media Content Detection & Playback Engine Feature](specifications/FR-20260809-002.md): UrlContentDetector, AudioPlaybackActivity with Karaoke view, VideoPlaybackActivity, and speed/jump controls.
- [Vosk STT Engine & About Application Activity Feature](specifications/FR-20260809-003.md): VoskTranscriptEngine, AboutActivity with about.html asset, and STT toggle controls.
- [Audio VAD Silence Detector & Background Management Feature](specifications/FR-20260809-004.md): AudioSilenceDetector VAD sentence segmenter, centered auto-scroll, 50-item buffer cap, and background auto-pause/resume lifecycle.





