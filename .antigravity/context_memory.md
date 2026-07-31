# Mémoire de Contexte AeonFlux - Android App

## État d'Avancement du Projet
- **Phase Actuelle :** Kick-Off & Initialisation de la structure du projet.
- **Dernières Actions :**
  - Pivot architectural : réécriture du projet Android de Kotlin à Java.
  - Remplacement de Jetpack Compose par le système de vues XML classique et View Binding pour compatibilité Java native.
  - Définition du package Android global : `com.aeonflux.app`.
  - Configuration de la gouvernance en Java Android.

## Prochaines Étapes
1. Mettre à jour les fichiers Gradle de build pour enlever Kotlin et ajouter le support de Java/Annotation Processors.
2. Créer l'arborescence des packages Java et générer le code squelette pour `core-database`, `core-network`, et `core-media` en Java.
3. Intégrer les layouts XML de base.
