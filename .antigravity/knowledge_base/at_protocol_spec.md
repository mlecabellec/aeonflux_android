# Spécification d'Intégration du Protocole AT (Bluesky)

Le protocole AT (Authenticated Transfer Protocol) est un protocole de réseau social fédéré. AeonFlux interagit avec l'API publique de Bluesky (sans compte requis par défaut) via des requêtes HTTP REST (XRPC).

## Requêtes Publiques (Lecture Seule)

### Récupération du flux d'un utilisateur (Author Feed)
- **Endpoint :** `GET https://bsky.social/xrpc/app.bsky.feed.getAuthorFeed`
- **Query Params :**
  - `actor` : DID (ex: `did:plc:z72ip7...`) ou Handle (ex: `paul.bsky.social`).
  - `limit` : Nombre maximal de posts à récupérer.
  - `cursor` : Curseur de pagination.

### Récupération d'un Custom Feed public
- **Endpoint :** `GET https://bsky.social/xrpc/app.bsky.feed.getFeed`
- **Query Params :**
  - `feed` : URI AT du feed (ex: `at://did:plc:z72ip7.../app.bsky.feed.generator/whats-hot`).

## Structures de Données AT Proto Reçues

Les posts Bluesky retournés sont au format JSON contenant le lexique `app.bsky.feed.defs#postView`.
- **URI :** Identifiant AT unique du post (ex: `at://did:plc:.../app.bsky.feed.post/3k...`).
- **CID :** Hash de contenu cryptographique.
- **Author :** Informations de profil (`handle`, `displayName`, `avatar`).
- **Record :** Contenu textuel, date de création (`createdAt`), tags, et liens.
- **Embed :** Médias embarqués (images, aperçus de liens web externe).
