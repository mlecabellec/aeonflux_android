# Directives Architecture Clean Code

1. **Separation of Concerns :**
   - **Presentation Layer :** UI, ViewModels, States, Composables. UI observe le ViewModel et ne modifie aucun état directement.
   - **Domain Layer :** Use Cases, Domain Models, Repository Interfaces. Pas de dépendance vers Room, Retrofit ou toute bibliothèque tierce Android.
   - **Data Layer :** Repository Implementations, Room Entities/DAOs, Retrofit API Interfaces, Data Sources. Responsable du caching (Single Source of Truth) et du mapping entre modèles de données/réseau et modèles de domaine.
2. **SOLID Principles :**
   - Single Responsibility : Une classe = une seule raison de changer.
   - Open/Closed : Classes ouvertes à l'extension mais fermées à la modification (ex. utilisation d'interfaces/abstractions).
   - Liskov Substitution, Interface Segregation, Dependency Inversion.
3. **Data Flows :**
   - Le flux d'information monte via Flow/StateFlow depuis le Repository/Database jusqu'au ViewModel et l'UI.
   - Les commandes descendent de l'UI vers le ViewModel puis les Use Cases et Repositories.
