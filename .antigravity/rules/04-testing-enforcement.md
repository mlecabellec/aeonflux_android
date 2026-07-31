# Directives Test & Qualité Formelle

1. **Objectif de Couverture minimum :**
   - 85% de couverture globale par lignes de code pour les modules critiques (ViewModels, Use Cases, Repositories, Parsers).
2. **Tests Unitaires (JUnit 5 / MockK / Turbine) :**
   - Obligatoires pour chaque ViewModel et Repository.
   - Les flows asynchrones `StateFlow` doivent être validés avec `Turbine`.
   - Les extracteurs de flux RSS/Podcast doivent être testés face à des XML valides et corrompus.
3. **Tests de Base de Données (Room In-Memory) :**
   - Chaque DAO de Room doit posséder une classe de test JUnit validant l'insertion, la mise à jour, la suppression et la recherche plein texte (FTS5).
4. **Tests UI (Compose Test Rule / Espresso) :**
   - Valider les comportements IHM complexes (comme la file d'attente audio, le défilement et le pull-to-refresh).
