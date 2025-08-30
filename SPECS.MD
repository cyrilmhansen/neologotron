Spécification Technique et Fonctionnelle – Application "Néologotron"

Version : 1.0
Date : 28 août 2025
1. Introduction et Vision du Produit

1.1. Objectif Principal
Développer une application mobile native Android, nommée "Néologotron", destinée à la génération créative de néologismes. L'application combine des préfixes, racines et suffixes issus principalement du grec et du latin pour créer des mots inédits. Chaque mot est accompagné d'une définition plausible, oscillant entre l'étymologique, le poétique et le surprenant.

1.2. Philosophie
Le Néologotron se veut plus qu'un simple générateur aléatoire. C'est un outil d'inspiration pour les écrivains, les poètes, les créatifs, les maîtres de jeu et les curieux de la langue. Il s'inspire du « Logotron », inventé par Jean‑Pierre Petit (France, 1977), et s'inscrit aussi dans l'esprit des expérimentations littéraires de l'Oulipo, pour le transposer dans une ère numérique et interactive. Il vise à moderniser le plaisir étymologique en offrant une expérience ludique, fluide et esthétiquement soignée.

1.3. Public Cible

    Créatifs : Écrivains, poètes, paroliers en quête d'inspiration ou de mots évocateurs.

    Passionnés de langues : Amateurs d'étymologie, de linguistique et de jeux de mots.

    Joueurs de jeux de rôle (JDR) et créateurs d'univers : Pour nommer des concepts, des lieux, des technologies dans des mondes imaginaires.

    Le grand public curieux : Utilisateurs à la recherche d'un divertissement intelligent et original.

2. Spécifications Fonctionnelles

2.1. Module 1 : Moteur de Génération de Néologismes

    2.1.1. Base de Données Morphologique :

        Une base de données locale (SQLite) embarquée contenant trois tables distinctes : préfixes, racines, et suffixes.

        Chaque entrée devra contenir :

            Le morphème (ex: bio-, -philo-, -logie).

            Son origine (grec, latin).

            Sa signification principale (ex: "vie", "amour de", "étude de").

            Des règles de compatibilité phonétique/morphologique (ex: "éviter si la racine commence par 'o'", "requiert une consonne de liaison").

            Des tags sémantiques (ex: nature, temps, société, technologie, abstrait).

    2.1.2. Algorithme de Génération :

        Mode Aléatoire : Combine un préfixe, une racine et un suffixe au hasard.

        Mode Semi-Guidé ("Thématique") : L'utilisateur choisit une ou plusieurs catégories (tags sémantiques) pour orienter la génération vers un champ lexical précis. L'algorithme pèse les morphèmes selon le nombre de tags correspondants (favorise les intersections multi‑tags), avec repli sur correspondance simple puis non filtré si aucun tag ne correspond.

        Comportement d'application immédiate : Les sélections de tags sont persistées (DataStore) et appliquées instantanément à la génération (pas de bouton « Appliquer »). Une action « Réinitialiser » permet d'effacer la sélection. Un bouton « Aperçu dans Détail » peut ouvrir un aperçu sans enregistrer dans l'historique.

        Mode Atelier ("Manuel") : L'utilisateur sélectionne manuellement chaque composant (préfixe, racine, suffixe) depuis des listes pour forger son propre mot.

    2.1.3. Filtres de Cohérence Linguistique :

        L'algorithme doit intégrer des règles pour améliorer la qualité des résultats et éviter les cacophonies. Ces filtres sont activables/désactivables via Paramètres.

        Gestion de l'élision : Suppression automatique de voyelles en double (ex: bio + o-logie → biologie et non bioologie).

        Consonnes de liaison : Ajout de consonnes euphoniques si nécessaire entre deux morphèmes.

        Filtre de plausibilité (optionnel) : Un système de notation simple pourrait écarter les combinaisons jugées les moins probables ou les plus difficiles à prononcer.

2.2. Module 2 : Moteur de Définition

    2.2.1. Génération de Définition :

        Le système concatène les significations des morphèmes pour produire une définition de base.

            Exemple : chrono ("temps") + phage ("manger") → "Qui/Celui qui mange/dévore le temps."

        Mode "Technique" : La définition est littérale et fidèle à l'étymologie.

        Mode "Poétique" : La définition est générée à partir de templates plus métaphoriques et évocateurs.

            Exemple : "L'art de consumer les instants" ou "La mélancolie de voir les secondes disparaître."

2.3. Module 3 : Interface et Expérience Utilisateur (UI/UX)

    2.3.1. Écran Principal ("Le Théâtre du Mot") :

        Affichage central et proéminent du néologisme généré.

        En dessous, une zone affichant la définition.

        Possibilité d'afficher la décomposition étymologique (ex: [chrono-] + [-phage]).

        Interaction principale :

            Un bouton central "Générer" ou "Nouveau Mot".

            Gestes intuitifs : Le swipe (gauche/droite) pour naviguer dans l'historique récent. Le shake (secouer le téléphone) pour générer un nouveau mot.

        Mode de définition (Technique/Poétique) :

            Contrôle à deux états via FilterChips Material3 avec état sélectionné explicite (choix d’un composant stable plutôt qu’un composant segmenté expérimental).

            Sélection persistée (DataStore) et appliquée immédiatement au moteur; contrôle identique sur l’Écran principal et l’Écran Détail.

    2.3.2. Fonctionnalités d'Interaction :

        Favoris : Une icône "cœur" ou "étoile" pour sauvegarder un mot apprécié dans une liste dédiée.

        Historique : Accès à la liste des derniers mots générés.

        Partage : Un bouton de partage permettant :

            De copier le mot et sa définition dans le presse-papiers.

            De partager directement sur les réseaux sociaux.

            [Suggestion] De générer une image stylisée (une "carte") avec le mot, sa définition et le thème visuel de l'app, prête à être partagée sur Instagram, etc. (post‑MVP)

        Historique : prise en charge du "swipe pour supprimer" avec option d'annulation (Undo) via snackbar.

    2.3.3. Personnalisation :

        Thèmes Visuels : L'utilisateur peut choisir entre plusieurs thèmes modifiant les polices, les couleurs et l'arrière-plan.

            Thème Minimaliste : Noir, blanc, typographie sobre.

            Thème Rétro 80's : Couleurs vives, polices pixellisées.

            Thème Cyberpunk : Tons néon (magenta, cyan), effets de "glitch".

        Paramètres du Générateur : Option pour activer/désactiver les filtres de cohérence, choisir le mode de définition par défaut (technique/poétique).

    2.3.4. Thématique – Détails UI :

        Disposition des tags : Affichage des chips dans une grille fluide (FlowRow) avec retour à la ligne automatique pour exposer l'ensemble des tags.

        Retour immédiat : Le tap sur un chip applique instantanément le filtre (pas de bouton « Appliquer »), avec persistance de la sélection. Un bouton « Réinitialiser » efface la sélection. « Aperçu dans Détail » reste disponible.

        Pondération multi‑tags : Les éléments correspondant à plusieurs tags sont favorisés; comportement de repli documenté lorsque aucun élément ne correspond aux tags sélectionnés.

3. Spécifications Techniques

    Plateforme : Android (API niveau 24+ recommandé pour un bon équilibre compatibilité/fonctionnalités).

    Langage : Kotlin (préféré pour le développement Android moderne, concis et sécurisé).

    Architecture : MVVM (Model-View-ViewModel) ou MVI (Model-View-Intent) pour une structure claire et testable.

    Base de données : SQLite avec l'ORM Room pour une gestion simplifiée; migrations versionnées (ex: persistance des métadonnées morphologiques dans Historique/Favoris pour recomposition dynamiques en Détail).

    UI : Jetpack Compose pour une interface utilisateur déclarative, moderne et performante.

    Dépendances : Aucune dépendance externe majeure n'est requise pour la V1, ce qui garantit une application légère et rapide.

    Stockage de préférences : DataStore Preferences pour thèmes, mode de définition, filtres de cohérence, options de geste (shake), et sélection thématique persistée.

3.2. Fonds animés par shader (AGSL) — Implémentation V1.6

    Portée : Enrichir les thèmes (Minimaliste, Rétro 80's, Cyberpunk) avec un fond animé optionnel.

    Technique :
        - API 33+ : AGSL via `RuntimeShader` et rendu dans un `Canvas` Jetpack Compose (composable `ThemedBackground`).
        - API < 33 : dégradé statique/soft fallback (sans dépendances externes).
        - Blend contrôlé : les shaders mélangent leur sortie avec `MaterialTheme.colorScheme.background` via un uniforme d’opacité (préserve la lisibilité).
        - Respect "Réduire les animations" : le temps (`iTime`) est figé si l’option système désactive les animations.

    Paramètres/Uniformes clés :
        - `iResolution`, `iTime`, `iIntensity`, `uOpacity`, couleurs d’accent du thème et `uBgColor`.
        - Rétro 80's : scanlines douces + grain + léger jitter horizontal.
        - Cyberpunk : blocs I/O pulsants avec poussière/éclat, grain fin et vignette légère.

    Paramètres utilisateur (DataStore + UI Paramètres) :
        - `animated_backgrounds_enabled` (bool)
        - `animated_backgrounds_intensity` (LOW | MEDIUM | HIGH)

    Intégration UI :
        - Chrome transparent (Surface/Scaffold/Top bars/Bottom bar) avec `contentColor = onBackground` pour garantir un contraste AA au‑dessus du fond.

    Critères d’acceptation :
        - Fond animé visible et fluide sur API 33+; pas de scintillement; lisibilité intacte.
        - Fond statique lisible quand l’option est OFF ou sur API < 33.
        - "Réduire les animations" fige l’animation; aucune régression de performance perceptible.

3.1. Accessibilité & Qualité

    Accessibilité (WCAG) : Toutes les actions possèdent une description (contentDescription); ordre de focus logique; contraste suffisant dans les trois thèmes.

    États de chargement/erreur : Squelettes de chargement pour listes; erreurs récupérables avec action « Réessayer ».

    Performance : Génération quasi‑instantanée (<150ms perçus) même avec CSVs volumineux; seuil/débounce du shake ajustés; indexation/caching des tags pour réduire les coûts par génération.

    Tests :
        - Unitaires pour règles de génération et composeDefinition (modes TECH/POETIC, mapping POS et synonymes FR/EN)
        - Intégration pour GeneratorService avec DAO en mémoire (élision, duplication consonne ON/OFF via filtres, sélection de template par mode)
        - Instrumentés pour partage/copier, navigation (barre inférieure, pop‑to‑root, mise en évidence de Détail), et one‑time hint shake
        - Parsing CSV (guillemets, guillemets échappés "" dans champs cités, virgules dans guillemets, lignes vides)

4. Évolutions Futures (Roadmap Potentielle)

    V1.5 : Améliorations Linguistiques

        Prononciation Audio : Intégration d'un moteur Text-to-Speech (TTS) pour "entendre" le mot généré.

        Support Multilingue : Enrichissement de la base de données avec des morphèmes d'autres origines (germanique, etc.) et adaptation de l'interface pour l'anglais, l'espagnol, etc.

    V2.0 : Fonctionnalités Communautaires

        Mode "Compétition" : Un thème est proposé (ex: "inventer un mot pour la solitude joyeuse"), les utilisateurs soumettent leurs créations via le mode "Atelier", et la communauté vote.

        Galerie Publique : Un espace pour voir les mots favoris de la communauté.

    V2.5 : Intégrations IA

        Générateur d'Illustrations : Connexion à une API de génération d'images (type Stable Diffusion, Midjourney) pour créer une représentation visuelle du néologisme.

        Définitions Améliorées : Utilisation d'un modèle de langage léger (API type Gemini, etc.) pour des définitions poétiques encore plus riches.

    V1.6 : Thèmes — Fonds animés par shader (LIVRÉ)

        Implémenté selon 3.2 (AGSL + paramètres). Deux shaders fournis (Rétro 80's, Cyberpunk) avec repli et réglages d’intensité. Chrome transparent et `contentColor` explicite assurent la lisibilité.

    V3.0 : Outils Experts

        Export : Exporter sa liste de favoris en glossaire PDF ou CSV.

        Widget : Un widget pour l'écran d'accueil affichant un "mot du jour".

✅ Remarques et Points de Vigilance

    La Qualité de la Base de Données est Cruciale : Le succès et la pertinence de l'application reposent à 90% sur la richesse et la qualité de la base de morphèmes. Il faudra un travail méticuleux de recherche et de saisie des données (significations, règles, tags). C'est le cœur du réacteur.

    L'Équilibre entre Hasard et Pertinence : L'algorithme devra être finement réglé. Trop de filtres et les résultats seront ennuyeux ; pas assez, et 99% des mots seront imprononçables ou absurdes. Prévoir une phase de bêta-test est indispensable pour ajuster ces règles.

    L'Expérience Initiale (Onboarding) : L'application est originale. Il faudra guider l'utilisateur lors de la première ouverture avec un mini-tutoriel de 3 écrans expliquant le concept : "1. Combinez des mots anciens. 2. Créez des idées nouvelles. 3. Partagez vos inventions."

    Performance : La génération doit être quasi-instantanée pour procurer un sentiment de fluidité et encourager l'exploration rapide. Le choix d'une base de données locale est excellent pour cela. Prévoir indexation des tags et profilage du seuil/débounce du shake.

    Attribution : S'assurer que les références publiques mentionnent correctement Jean‑Pierre Petit pour le « Logotron », tout en citant l'inspiration Oulipo séparément.

---

Mise à Jour Spécification — 30 août 2025

- Données & Persistance
  - Historique/Favoris stockent désormais en option des métadonnées morphologiques: `prefixForm`, `rootForm`, `suffixForm`, `rootGloss`, `rootConnectorPref`, `suffixPosOut`, `suffixDefTemplate`, `suffixTags`.
  - Détail recompute mot/définition à la volée depuis ces métadonnées lorsque mode de définition et filtres changent.

- Moteur de Définition
  - Normalisation POS étendue: prise en charge de `adj`, `adv`, `verbe/verb/v`, `nom`, `nom_agent`, `nom_action`, `nom_resultat` et composites (ex: `adj/nom`) avec priorisation agent > action > résultat > verbe > adjectif > nom > adverbe.
  - Templates par mode (`tech:` / `poetic:`) avec défauts sensés si absents; mapping `{ACTION}` contextuel (ex: verbe → « agir sur », nom_action → « action de »).
  - Tests unitaires ajoutés pour nouveaux cas (adv, nom_action, nom_resultat, composites).

- États de Chargement / Erreurs / A11y
  - Introduction de `UiState` (Loading/Data/Error) pour Historique, Favoris, Thématique (tags) et Atelier (chargement initial).
  - Squelettes de chargement pour listes et chips; snackbar « Réessayer » sur erreur; boutons désactivés pendant le chargement lorsque nécessaire.

- Performance
  - Caching mémoire dans `LexemeRepository` pour jeux complets et par tag (préfixes/racines/suffixes), invalidé lors d’un reseed.
  - Journalisation des latences de génération (DEBUG uniquement) avec alerte si total > 150ms; mesure séparée « compose » vs « total ».

- UI — Contrôle du mode de définition
  - Écran Principal et Détail utilisent des FilterChips pour afficher/sélectionner le mode (Technique/Poétique) avec état sélectionné explicite; persistance immédiate; recompute live en Détail si métadonnées présentes. Le composant segmenté Material3 a été écarté (expérimental/non garanti par le BOM courant).

- Tests complémentaires
  - Intégration pour `GeneratorService` avec DAO en mémoire (élision, duplication consonne ON/OFF via filtres, sélection de template par mode).
  - Parsing CSV: tests couvrant virgules dans guillemets, guillemets doubles échappés et lignes vides; helper `parseCsvLine` exposé (internal top‑level) pour testabilité.
