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
        - Sources externalisées: `res/raw/retro80s_agsl.aglsl`, `res/raw/cyberpunk_agsl.aglsl` pour maintenance.
        - Override optionnel (modding): si présent, charger `assets/shaders/*.aglsl` en priorité (mêmes noms) — aucune dépendance réseau.
        - Blend contrôlé : output shader mélangé avec `MaterialTheme.colorScheme.background` via uniforme `uOpacity` (lisibilité garantie).
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
        - Les shaders peuvent être remplacés par des fichiers `assets/shaders/*.aglsl` en build de dev sans modification du code.

3.1. Accessibilité & Qualité

    Accessibilité (WCAG) : Toutes les actions possèdent une description (contentDescription); ordre de focus logique; contraste suffisant dans les trois thèmes.

    États de chargement/erreur : Squelettes de chargement pour listes; erreurs récupérables avec action « Réessayer ».

    Performance : Génération quasi‑instantanée (<150ms perçus) même avec CSVs volumineux; seuil/débounce du shake ajustés; indexation/caching des tags pour réduire les coûts par génération.

    Tests :
        - Unitaires pour règles de génération et composeDefinition (modes TECH/POETIC, mapping POS et synonymes FR/EN)
        - Intégration pour GeneratorService avec DAO en mémoire (élision, duplication consonne ON/OFF via filtres, sélection de template par mode)
        - Instrumentés pour partage/copier, navigation (barre inférieure, pop‑to‑root, mise en évidence de Détail), et one‑time hint shake
        - Parsing CSV (guillemets, guillemets échappés "" dans champs cités, virgules dans guillemets, lignes vides)

3.3. Chaîne ETL (Wiktionary → CSV → App) – Implémentation V1

    Objectif : Fournir une base morphologique riche (préfixes, racines, suffixes) à partir de Wiktionary via Kaikki/wiktextract, avec un flux reproductible et interactif.

    Outils et scripts :
        - `etl/cli.py` : Assistant interactif (wizard) qui télécharge les jeux de données, extrait le Translingual (mul), transforme, fusionne et exporte les CSV dans `app/src/main/assets/seed/`.
            - Options : fusion FR+MUL par défaut (FR prioritaire), `--fr-only` pour ignorer MUL.
            - Affichages : téléchargement avec taille/vitesse/ETA, spinner pendant les transformations, progression de filtrage MUL (l/s), progression sur la copie vers assets.
        - `etl/wiktextract_to_neologotron.py` : Transforme un JSONL wiktextract/Kaikki en CSVs (préfixes/racines/suffixes). Filtrage « classical » basé sur l’étymologie (texte et lignage). Option `--mul-fallback-classical` pour accepter certains affixes Translingual.

    Sources de données :
        - FR : `https://kaikki.org/dictionary/downloads/fr/fr-extract.jsonl.gz`
        - Toutes langues (brut) : `https://kaikki.org/dictionary/raw-wiktextract-data.jsonl.gz` (filtrage `lang_code=="mul"` pour Translingual).

    Intégration App :
        - Les CSV placés sous `app/src/main/assets/seed/` sont chargés au premier démarrage et via l’écran Debug → « Réinitialiser la base ».
        - Les colonnes supplémentaires sont ignorées par l’app (compatibilité ascendante) : seules les 9 premières sont consommées.

    Curation interactive :
        - `python3 etl/cli.py review --run <ts>` : file « incertaine » (gloss court/manquant, origine/tags manquants, forme suspecte), actions accepter/éditer/rejeter.
        - Filtres : `--origin-lang grc,la,mul`, `--domains science,medicine,tech`, `--show-all`.
        - `apply-review` applique les décisions et met à jour les assets.

    Conformité & Crédits :
        - Contenu Wiktionary sous licence CC BY‑SA (4.0 pour la réutilisation). Attribution visible dans l’écran « À propos » (CC BY‑SA, citation wiktextract, liens Kaikki).
        - Traçabilité : `etl/runs/<horodatage>/run.json` conserve les URLs des dumps, l’option `--fr-only`, etc.

Annexes – Spécifications détaillées de la chaîne ETL

Annexe A. Schémas CSV et sémantique des colonnes

- Colonnes consommées par l’app (actuelles)
  - Les 9 premières colonnes de chaque CSV seulement. Les colonnes étendues peuvent être présentes et seront ignorées par l’app (compatibilité ascendante).

- Préfixes (`neologotron_prefixes.csv`)
  - id: identifiant stable, unique (string)
  - form: forme affichée, ex. « bio- » (string)
  - alt_forms: variantes, séparées par « , » (nullable string)
  - gloss: sens court (FR), concaténation de glosses (string)
  - origin: langue d’origine affichée (ex. « grec », « latin ») (nullable string)
  - connector: voyelle/consonne de liaison suggérée (ex. « o ») (nullable string)
  - phon_rules: règles phono (texte libre, ex. assimilation) (nullable string)
  - tags: catégories/lexiques (FR, « tech, médecine, science… ») (nullable string)
  - weight: poids facultatif (double, nullable)
  - Étendu (facultatif): ety_lang, ety_desc, ety_lineage, proto_form, ipa, attest_from, cognates, sources, examples

- Racines (`neologotron_racines.csv`)
  - id: identifiant stable, unique (string)
  - form: forme affichée (string)
  - alt_forms: variantes (nullable string)
  - gloss: sens court (string)
  - origin: langue d’origine affichée (nullable string)
  - domain: domaine(s) (FR) (nullable string)
  - connector_pref: liaison typique (ex. « o ») (nullable string)
  - examples: dérivés/exemples (séparés par « ; ») (nullable string)
  - weight: poids facultatif (double, nullable)
  - Étendu: root_lang, proto_root, ety_desc, ety_lineage, semantic_field, sources

- Suffixes (`neologotron_suffixes.csv`)
  - id: identifiant stable, unique (string)
  - form: forme affichée (ex. « -logie ») (string)
  - alt_forms: variantes (nullable string)
  - gloss: sens court (string)
  - origin: langue d’origine affichée (nullable string)
  - pos_out: partie du discours résultante (nullable string)
  - def_template: modèle de définition (texte/template) (nullable string)
  - tags: catégories/lexiques (FR) (nullable string)
  - weight: poids facultatif (double, nullable)
  - Étendu: ety_lang, ety_desc, ety_lineage, proto_form, ipa, attest_from, cognates, sources, examples

Annexe B. Identité, génération d’ID et fusion FR+MUL

- Clé d’identité logique (pour la fusion):
  - type (préfixe/racine/suffixe) + `form` (exact, y compris tirets de tête/queue).
  - Les `alt_forms` ne participent pas à l’identité.

- Génération d’ID (dans l’ETL):
  - Préfixe technique (`pre_`, `root_`, `suf_`) + `form` normalisée en minuscules alpha-numériques avec `_` pour les séparateurs; accents supprimés.
  - Déduplication en suffixant un compteur si collision (ex. `_2`, `_3`).
  - Les IDs produits peuvent différer entre exécutions si des collisions/insertions diffèrent; d’où la fusion par `form` en phase merge, pas par `id`.

- Priorité et règles de fusion (FR vs MUL):
  - Priorité aux lignes FR (fr-extract) sur conflit de `form`; les lignes MUL (Translingual) ne s’ajoutent que si `form` inexistant.
  - Champs issus de FR conservent leur gloss/origin/tags; les champs étendus ne sont pas fusionnés à ce stade (V1 simple).
  - Raison: FR fournit glosses FR et adéquation à l’app; MUL comble des manques de couverture (affixes/classiques, combinings).

Annexe C. Règles de transformation et filtres

- Détection du type (POS):
  - S’appuie sur `pos`/`pos_title` (wiktextract/Kaikki, angl./localisé). Correspondances accent‑insensibles: « prefix(e) », « suffix(e) », « affix(e) », « combining form », « élément de composition/formant », « confix », etc.

- Normalisation de `form`:
  - Conserver les tirets de tête/queue pour marquer préfixes/suffixes (ex. « bio- », « -logie »).
  - `alt_forms` : déduites de `forms[].form` différentes de `word`.

- Gloss et tags/domaines:
  - `gloss` = concaténation unique des `senses[].glosses` (séparateur « ; »).
  - `tags/domain` = union de `senses[].topics` + `senses[].tags`, mappés vers quelques libellés FR (« biology »→« science », « medicine »→« médecine », « technology »→« tech », etc.).

- Étymologie et origine:
  - `ety_lang`: code normalisé (`fr`, `mul`, `grc`, `la`, `el`, `en`…), depuis `lang_code`/`lang`.
  - `origin`: libellé FR affiché (ex. « grec », « latin ») dérivé de `ety_lang` ou inféré via lignage.
  - `ety_desc`: `etymology_text`/`etymology_texts` nettoyés (liens/templ. retirés).
  - `ety_lineage`: chaîne compacte type « fr < la < grc » quand `etymology_templates` disponibles (rare dans fr-extract). V1: fallback sur `ety_desc` pour la détection « classical ».

- Filtre « classical » (par défaut):
  - Conserve les entrées dont `origin`/`ety_lineage`/`ety_desc` font mention de grec/latin (mots‑clés: « grec », « latin », « grc », « la »).
  - Option `--origin-filter none` pour garder tout.
  - Option `--mul-fallback-classical`: pour `ety_lang==mul`, accepte les affixes « ressemblant à des affixes » (forme début/fin par tiret) en absence d’indices explicites.

- Racines à partir du Translingual:
  - `--roots-from-translingual`: duplique certaines entrées MUL (préfixes/suffixes) en racines quand l’étymologie suggère une origine classique, ou via fallback ci‑dessus.
  - Les « combining forms »/« affix » en `mul/grc/la` deviennent des racines directement.

- Connecteur (« o » etc.):
  - Heuristique simple: si la `form` contient « o- » ou « -o- », suggérer `connector`/`connector_pref = "o"`. Éditable en revue.

- Provenance (`sources`):
  - Chaîne compacte `wiktionary:<site>:<pageid>:<word>#<pos>` quand disponible (ex. `wiktionary:fr:23456:-logie#suffix`).

Annexe D. Validation, QA et garde‑fous

- Validations d’export:
  - En‑têtes exacts, colonnes obligatoires non vides (`id`, `form`, `gloss`).
  - Valeurs numériques parsables (`weight`).
  - Aucune ligne vide; CSVs encodés UTF‑8; guillemets correctement échappés.

- Rapports et bornes:
  - Compteurs par type (préfixes/racines/suffixes) et avant/après filtre « classical ».
  - Statistiques fusion FR+MUL (ajouts MUL, totaux finaux).

- Revue interactive (qualité):
  - File « incertaine »: gloss court (<6), absence d’origine, tags/domain manquants, forme suspecte (majuscules).
  - Filtres: `--origin-lang`, `--domains`, `--show-all`. Actions: accepter/éditer/rejeter; application via `apply-review`.

    - Reproductibilité:
        - `etl/runs/<horodatage>` conserve matières premières, CSVs intermédiaires, fusion, export, décisions de revue (`review/decisions.jsonl`) et `run.json`.
        - Les IDs pouvant varier à la marge, la fusion/curation s’appuie sur `form` comme identité logique.

Annexe E. Variantes d’affixes & politique de canonicalisation

Objectif
- Réduire les doublons et améliorer la qualité des combinaisons en regroupant les variantes d’un même « morphème idée » (allomorphes, romanisations, liaisons), tout en excluant par défaut les abréviations techniques peu naturelles en français.

Familles de variantes (exemples non exhaustifs)
- Assimilation/Allomorphes (préfixes latins)
  - in- → im- (devant b/p/m), il- (devant l), ir- (devant r)
  - ad- → ac-/af-/ag-/al-/an-/ap-/ar-/as-/at-
  - con- → com-/col-/cor- ; syn- → sym- (devant b/p/m)
  - e-/ex- selon la base
- Romanisation/diacritiques et liaison grecque
  - cephalo-/céphalo-, cardio-/cardi-, leuco-/leuko-, hémato-/hemato-, etc.
  - Élément de liaison « o » (grec) présent/absent selon les mots
- Paires sémantiques (suffixes proches)
  - -logie (étude) ↔ -logue/-logiste (spécialiste)
  - -phile ↔ -philie ; -phobe ↔ -phobie ; -isme ↔ -iste ; -ité (abstrait) ; -tion/-sion ; -eur/-euse ; -teur/-trice ; -if/-ive ; -al/-ale/-aux ; -able/-ible ; -eux/-euse (lat. -osus)
- Abréviations techniques/notation scientifique (à exclure par défaut)
  - D-/L-/R-/S-/(RS-)/DL- (stéréochimie)
  - n- (nano-), m- (milli-), u- (μ/micro), z- (zepto-), r- (ronto-), my- (myria-), etc.

Choix de conception (politique V1)
- Canonicaliser par « idée morphologique »
  - Choisir une forme canonique en français (FR) quand elle existe et est usuelle (diacritiques inclus) ; lister les autres variantes dans `alt_forms`.
  - Exemple: canon « céphalo- » (ou « cephalo- » si l’usage l’exige), `alt_forms: cephalo-`.
- Exclure par défaut les abréviations techniques
  - Filtrer les entrées portant des tags `abbreviation`, `alt-of`, `informal`, `proscribed` du set standard.
  - Conserver ces entrées dans un « mode expert » optionnel (futur) ou en `alt_forms` d’un canon pertinent.
- Suffixes « paires »
  - Conserver des gloses distinctes, et, si possible, fournir un `pos_out` indicatif pour orienter la définition (ex.: -logie → `noun`/`result_noun`, -logue → `agent_noun`).
- Pondération
  - Les variantes d’un même canon ne doivent pas sur‑pondérer la génération ; le poids s’applique au canon. Les `alt_forms` ne sont pas comptés comme entrées séparées.

Application dans l’ETL (plan et implémentation)
- Fichier d’équivalences (éditable): `etl/equiv_map.json`
  - Format: `{ "canon": { "alt_forms": ["..."], "notes": "..." }, ... }`
  - Entrées prévues: groupes in-/im-/il-/ir-, séries ad- assimilées, couples cephalo-/céphalo-, hémato-/hemato-, liaison « o », etc.
- Filtre d’exclusion par tags (défaut)
  - Wizard: activer par défaut un filtre `--exclude-tags abbreviation,alt-of,informal,proscribed` (BACKLOG), avec override en mode expert.
  - Revue: priorité aux entrées marquées par ces tags pour « rejeter » ou rétrograder vers `alt_forms`.
- Fusion FR+MUL
  - FR prioritaire ; si seule MUL existe, appliquer l’équivalence (si connue) pour imposer le canon FR ; l’autre va dans `alt_forms`.
- Génération (app)
  - L’assimilation phonétique est traitée au moment de la composition (ex.: in- → im- devant b/p/m) plutôt que dupliquer des entrées ; `alt_forms` reste informatif.

Heuristiques de revue (priorité)
- Signaler en premier:
  - `form` courts (1–2 chars) + tags `abbreviation`/`alt-of`
  - Formes en MAJUSCULES (D-, L-, R-, S-, RS-, DL-)
  - Romanisations sans diacritiques concurrentes à une forme FR
  - Paires sémantiques suspectes (vérifier gloses courtes/cohérentes)

Backlog (technique)
- Ajouter au wizard: `--exclude-tags` (valeur par défaut) + `--include-tags-expert` pour rétablir les abréviations.
- Implémenter `equiv_map.json` dans l’ETL: remonter les variantes en `alt_forms` du canon et dédupliquer par canon.
- Tests unitaires pour la canonicalisation et la non‑duplication des variantes ; tests de parsing sur cas diacritiques.

Annexe F. Préfixes très courts (1–3 caractères) – Politique

Contexte
- De nombreux préfixes 1–2 lettres proviennent d’abréviations SI (n-, m-, u-, z-, r-, q-) ou de notations (D-/L-/R-/S-/RS-/DL-). Ils sont peu naturels pour un usage ludique grand public.
- D’autres courts sont très productifs en français (a-, co-, re-, dé-/de-, ex-, in- (+im-/il-/ir-)).

Politique V1
- Deny-list (exclusion par défaut): `D-`, `L-`, `R-`, `S-`, `RS-`, `DL-`, `n-`, `m-`, `u-`, `z-`, `r-`, `q-`.
- Allow-list (préserver): `a-`, `co-`, `re-`, `dé-` (ou `de-`), `ex-`, `in-`, `im-`, `il-`, `ir-`, et canons courts utiles `bio-`, `geo-`, `pro-`.
- Implémentation: fichier `etl/short_prefix_policy.json` (BACKLOG) et application dans le wizard.
- Revue: prioriser l’audit des restes 1–2 lettres ; proposer exclusion ou rattachement en `alt_forms`.

Annexe G. Préfixes multiples (enchaînés)

Objectif
- Permettre ponctuellement des combinaisons avec 2 préfixes consécutifs (ex.: co‑re‑X), pour enrichir la variété.

Règles
- OFF par défaut; toggle en Paramètres.
- Sélection du second préfixe parmi la allow-list; distinct du premier; jamais une abréviation/notation.
- Composition: appliquer les mêmes règles d’élision et d’assimilation phonétique que pour la jonction préfixe/racine.
- Définition: reste basée sur racine + suffixe (glose courte) pour conserver la concision.

Annexe H. IA d’assistance (traduction/summarisation/normalisation)

Objectifs
- Obtenir des gloses FR courtes et homogènes ; proposer pos_out (suffixes) ; signaler doublons/variantes/canons.

Flux
- Sélection d’un échantillon: `prep-ai` (JSONL + prompt), prompts FR prescriptifs avec exemples.
- Import: `import-ai` applique les gloses/pos_out et écrit un plan d’actions `actions_todo.jsonl` (canonize, alt_form, rationale) et `edited_ids.txt`.
- Revue ciblée: `review --ids-file edited_ids.txt`; application finale via `apply-review` (et, à venir, application des actions du plan).

Intégration LLM locaux (BACKLOG)
- Exposer un sous-commande `ai-run` (endpoint, modèle, lot, temperature, max_tokens, timeout) compatible ollama (localhost:11434), LM Studio (localhost:1234), llamafile (serveur local).
- Cache des réponses (`runs/<ts>/ai/cache/`), reprise sur échec, limitation du débit, dry-run.
- Stratégie multi-prompts: gloss‑only, gloss+pos, canon/alt/excl., plus prompts de raffinement (anti-“ou”, anti-anglicismes, clamp 80c).

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
