# Directives Développement Android pour Antigravity Agent

1. **Architecture :** Implémenter obligatoirement le pattern Clean Architecture MVVM avec Jetpack Compose. Aucun code métier dans les Composables.
2. **Explicit API Mode :** Tout composant exposable dans le package `core` doit spécifier sa visibilité et ses types de retour explicites.
3. **Coroutines & Flow :** Ne jamais utiliser `GlobalScope`. Toujours injecter des `CoroutineDispatcher` (Dispatchers.IO, Dispatchers.Default) via Dependency Injection.
4. **State Management :** Utiliser des classes scellées (`sealed interface UIState<out T>`) pour représenter les états (Loading, Success, Error).
5. **Zero Hardcoded Strings :** Toutes les chaînes de caractères IHM doivent résider dans `strings.xml`.
6. **Code Quality :** Configurer et appliquer KtLint et Detekt sur tous les fichiers Kotlin pour forcer un style uniforme et éviter les complexités cyclomatiques élevées (>10).
