# Directives Développement Android Java pour Antigravity Agent

1. **Language & Tools :** Le projet Android est entièrement écrit en Java 17+. Ne pas utiliser de code Kotlin.
2. **Architecture :** Implémenter le pattern MVVM. La couche UI utilise les Android Views (layouts XML) et le View Binding. Aucun code métier dans les Activities ou Fragments.
3. **Dependency Injection :** Utiliser Dagger Hilt pour Java (annotations `@AndroidEntryPoint`, `@Inject`, `@Module`, `@InstallIn`, `@Provides`).
4. **Asynchronisme :** Utiliser les Executors Java ou RxJava/Livedata pour gérer la réactivité. Ne jamais bloquer le thread principal (UI thread).
5. **Zero Hardcoded Strings :** Toutes les chaînes de caractères IHM doivent résider dans `strings.xml`.
6. **Code Quality :** Configurer et appliquer Checkstyle et PMD sur les fichiers Java.
