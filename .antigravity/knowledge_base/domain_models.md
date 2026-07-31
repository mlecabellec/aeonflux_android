# Base de Connaissance : Modèles de Domaine AeonFlux

## Entités Principales

### Feed (Flux)
Représente un canal d'information de type RSS, Podcast ou Bluesky.
- `id` : Identifiant unique généré par hash de l'URL ou UUID.
- `url` : URL source du flux.
- `title` : Titre du flux.
- `description` : Courte description.
- `iconUrl` : URL de l'icône/avatar.
- `feedType` : `RSS`, `PODCAST`, ou `BLUESKY`.
- `refreshIntervalMinutes` : Fréquence de synchronisation.
- `lastRefreshedAt` : Horodatage de dernière synchronisation.
- `customTags` : Liste de mots-clés/catégories définis par l'utilisateur.
- `isContributedToGae` : Booléen indiquant si ce flux a été soumis anonymement au catalogue central.

### FeedItem (Élément de Flux)
Représente un article, un épisode de podcast ou un post Bluesky.
- `id` : Identifiant unique (GUID ou hash).
- `feedId` : Clé étrangère pointant vers `Feed`.
- `guid` : Identifiant original du flux source.
- `title` : Titre ou résumé court.
- `contentRaw` : Contenu HTML/brut d'origine.
- `contentCleaned` : Contenu HTML épuré et sécurisé pour affichage hors-ligne.
- `author` : Auteur du contenu.
- `publishedAt` : Date de publication d'origine.
- `url` : Lien web externe de l'élément.
- `isRead` : Statut de lecture.
- `isBookmarked` : Marqué comme favori.
- `mediaUrl` : URL du fichier audio si Podcast.
- `mediaDurationMs` : Durée du média audio.
- `playbackPositionMs` : Position actuelle de lecture audio.
- `isDownloaded` : Audio stocké localement.
- `localMediaPath` : Chemin local du fichier audio téléchargé.
- `cachedHtmlPath` : Chemin vers l'archive HTML locale pré-chargée.
- `aiSummary` : Résumé automatique généré par Gemini.
- `transcriptText` : Transcription textuelle générée par Speech-to-Text.
