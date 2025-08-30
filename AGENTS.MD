                                                                                Update this file to keep track of project progress, which tasks are done, which ones are in progress, which ones remains to do.
Note everything here that you may need to remember sooner or later if you need to update the project.

Please don't use name like Scaffold. Use name inspired by the screen usage.

We are using latest Android Studio and emulator for build and app human testing.

See SPECS.MD

---

# Operational Executive Summary – UI/UX Scope (from SPECS.MD)

This summarizes the required screens, core interactions, and implementation milestones for the Néologotron MVP in alignment with SPECS.MD and ARCHITECTURE.MD.

## Core Screens

1) Théâtre du Mot (Main)
- Center: generated word; below: definition; optional decomposition [prefix + racine + suffixe]
- Primary actions: Generate/New, Favorite, Share, Definition mode toggle (Technique/Poétique)
- Gestures: Swipe left/right for recent history; optional shake-to-generate (settings toggle)

2) Historique
- Scrollable list of recent generations (word, short definition, timestamp)
- Tap opens details (full definition, decomposition, actions)
- Actions: re-generate similar, favorite, copy/share; optional swipe-to-remove from history

3) Favoris
- Saved words with quick actions (copy/share/remove)
- Search/filter by tags or definition mode (optional in MVP if time allows)

4) Thématique (Semi-guidé)
- Tag picker (chips) to bias generator by semantic categories
- Apply/Reset actions; accessible from Main (toolbar or FAB)

5) Atelier (Manuel)
- Three selectors: Préfixe, Racine, Suffixe (searchable lists)
- Live preview (word + definition + decomposition) with coherence hints
- Compose button to commit to history/favorites

6) Paramètres / Personnalisation
- Theme selection (Minimaliste, Rétro 80's, Cyberpunk)
- Generator options: enable/disable coherence filters, default definition mode, shake-to-generate

7) Onboarding (3 écrans)
- Concept walkthrough: 1) Combiner 2) Créer 3) Partager
- Start button; skip allowed; runs only on first launch

## Key Interactions & States
- Share: Android share sheet + copy-to-clipboard (card image generation is post‑MVP)
- Empty states: no history, no favorites, no tags selected (provide clear calls to action)
- Loading/skeletons: while reading local DB or computing previews in Atelier
- Error handling: fallback UI + retry per ARCHITECTURE.MD guidelines
- Accessibility: WCAG-minded focus order, content descriptions, contrast per theme

## Navigation Model (Compose)
- Single-activity app with Navigation Compose
- Bottom bar: Main, Historique, Favoris; overflow/menu for Thématique, Atelier, Paramètres
- Onboarding as a first‑run route that gates main graph until completed

## Data & Domain
- Room/SQLite: tables for morphèmes (préfixes, racines, suffixes) with origin, meaning, compatibility rules, tags
- Local tables for historique and favoris (word, definition, decomposition, mode, tags, timestamp)
- Generator: Random, Semi-guidé (tags), Manuel; includes coherence filters (élision, consonne de liaison, plausibility score)

## Milestones (MVP)
- M0: Import `specs_wip/*.csv` → Room seed; define entities/DAOs
- M1: Generator core + coherence filters + definition composer
- M2: Théâtre du Mot UI + actions + gestures
- M3: Historique + details; Favoris
- M4: Thématique tag picker; Atelier manual builder
- M5: Paramètres + theming (3 themes)
- M6: Share & copy; clipboard integration
- M7: Onboarding; polish, A11y, performance

## Risks & Mitigations
- Data quality of morphèmes: curate CSVs; validation tooling; unit tests on rules
- Coherence tuning: feature flags, adjustable thresholds, beta feedback loop
- Gesture conflicts: provide settings toggles; respect platform accessibility
- Scope creep on share visuals: defer image card generation to post‑MVP

## Acceptance (per screen, MVP)
- Main: Generates instantly (<150ms perceived), toggles modes, swipe history works
- History/Favorites: Persist across sessions; open details; remove/add operations
- Thématique/Atelier: Filters/selection affect output predictably; live preview in Atelier
- Settings/Themes: Theme applies app‑wide; options persist
- Onboarding: Only first launch; accessible and skippable

## Non‑Goals (MVP)
- TTS, community features, AI integrations, image-card generation (visuals), exports

## Next Execution Steps
- Define Room schema + seed pipeline from CSVs
- Implement generator service + unit tests on filters
- Scaffold Navigation + bottom bar + routes
- Build Main screen in Compose; wire actions and gestures
- Implement History/Favorites storage and UIs

---

# Project Progress – Status Log

## Done (Validated)
- Navigation: Bottom bar + `NavHost` with routes (`main`, `history`, `favorites`, `settings`, `thematic`, `workshop`, `detail/{word}`)
- Root container rename: `WordTheatreHost` (avoid generic "Scaffold" naming)
- Primary screens: Main, History, Favorites, Settings (Compose stubs with strings)
- Additional screens: Thématique (tag chips), Atelier (manual builder), Word Detail (title, definition, etymology)
- Inter-screen flows: Buttons from Main → Thématique/Atelier; History/Favorites → Detail via tap
- Detail UX: Top app bar with back navigation; argument handling for selected word
- Resources: Strings for titles, actions, placeholders; icons via Material Icons Extended
- Build system: Compose Navigation dependency; Kotlin/AGP aligned
- KSP migration: Replaced KAPT with KSP for Hilt (faster builds, removed warnings)
- Build health: `assembleDebug` passes; studio human tests pass; no warnings
- Data layer: Room v3 with entities (prefixes, roots, suffixes, history, db_meta), DAOs, `AppDatabase`, Hilt DI
- Seeding: CSVs moved to assets and seeded on first run via `SeedManager`; `db_meta.createdAtMillis` recorded
- Repositories: `LexemeRepository` (tags/lists/search), `HistoryRepository` (save/recent), `AdminRepository` (reset+reseed)
- Thematic/Atelier wiring: Thematic chips are populated from DB tags; Atelier shows sample lists from DB
- Generator: Minimal `GeneratorService` (random composition, basic elision/liaison, simple definition composer) + saves to history
- Main screen: "Générer" button now generates a word/definition and persists to history
- Debug screen: Accessible from Settings; shows DB build time; provides Reset DB (clear + reseed)
- Fixes: SQL order-by changed to SQLite-compatible CASE; CSV parser corrected; DB version bumped with destructive migration for MVP

## In Progress / Open Items
- Bottom bar behavior: Optionally hide on Detail for a focused view (decision pending)
- Thematic: Wire real tag data + selection state; pass filters to generator
  
- History screen: Replace placeholders with recent entries from Room
- Favorites: Storage model + UI wiring (toggle in Detail/Main)

## Next Up (Planned)
- Themes & settings: DataStore-backed preferences (theme, default definition mode, coherence filters, shake-to-generate); implement 3 visual themes; wire `NeologotronTheme` to prefs
- Definition mode: UI toggle (Technique/Poétique) on Main/Detail; persist default; extend composer templates for more suffix types and better phrasing
- Thematic biasing: flow layout for all tags, persist selection; improve multi-tag weighting (intersection priority + bias factor, robust fallback)
- History UX: swipe-to-remove with snackbar undo; auto-refresh on `ON_RESUME` like Favorites
- Gestures: shake-to-generate with sensitivity/debounce; gated by settings toggle
- Loading/error/a11y: skeletons for History/Favorites; repository error snackbars with retry; content descriptions and contrast checks across themes
- Testing/data quality: unit tests for `GeneratorService` and CSV parsing edge cases; basic navigation tests; curate/expand CSVs with simple validation
- CI: optional separate job for instrumented tests (keep unit/linters as-is for speed)

## Notes & Decisions
- Image-card generation deferred post‑MVP (per specs)
- KSP adopted; no kapt-specific options remain
- Accessibility to be verified after core flows land
- Room uses `fallbackToDestructiveMigration` during MVP; safe since data is seeded and user-generated data minimal for now

- Bottom bar remains visible across all screens (including Detail) to improve accessibility and direct navigation.
- On Detail, the bottom bar highlights the parent tab (History/Favorites/Main) for UX consistency.
- Bottom bar tap behavior prefers popping back to an existing destination instance before navigating to avoid stacking duplicates.
- Add an About screen referencing original Logotron idea (Raymond Queneau, Jean Lescure, Oulipo); accessible from Settings.

— Definition default mode will be persisted in settings and surfaced on Main/Detail. Share/copy is considered complete for MVP. Image-card generation remains post‑MVP as previously decided.

---

## Newly Completed (This Session)
- Favorites end-to-end: Room entity/DAO/repository; Favorites screen lists stored favorites; Detail screen favorite toggle (add/remove) with persisted state; DB version bumped to 4.
- Main screen: Added heart toggle next to generated word; favorite state updates after each generation.
- Favorites screen: Auto-refreshes on `ON_RESUME` to reflect changes made elsewhere.
- Navigation UX: From Detail, tapping a bottom bar tab pops back to that tab's root; on Detail, the tab of origin is highlighted via a `from` nav argument.
  - Extended: If Detail originates from non-bottom destinations (Thématique/Atelier/Debug), the Main tab is highlighted by default for consistency.
 - Snackbars: Added confirmation snackbars on Théâtre and Détail when adding/removing favorites.
- About: New `AboutScreen` with credits and version; Settings now links to About.

### Workshop (Atelier) – Draft selectors + live preview
- ViewModel: Added selected prefix/root/suffix state and live preview computation (`previewWord`, `previewDefinition`, `previewDecomposition`).
- Search: ViewModel exposes `searchPrefixes/Roots/Suffixes` delegating to repository search or list‑by‑tag.
- UI: Implemented selection dialogs (searchable lists) for Préfixe/Racine/Suffixe and wired to ViewModel selections.
- Preview: Workshop screen now shows current selection summary and a live preview (word, definition, decomposition) based on selections.
- Commit: Implemented "Forger le mot" — composes from selections, saves to history with mode="manual", and navigates to Detail.
- Preview in Detail (Atelier): Opens Detail for the current selection WITHOUT saving to history, by passing transient definition + decomposition via nav args. Detail falls back to these when no history/favorite exists.
- Guardrails: "Forger le mot" is disabled until all three parts are selected; tapping "Aperçu dans Détail" without full selection shows a snackbar prompt.

---

## Newly Completed (This Session)
- Favorites end-to-end: Room entity/DAO/repository; Favorites screen now lists stored favorites; Detail screen has add/remove favorite toggle reflecting state; database version bumped to 4.
- About screen: Corrected attribution — the « Logotron » is credited to Jean‑Pierre Petit (France, 1977); removed misattribution to Queneau/Lescure and clarified inspiration context.
- Share & copy: Main and Detail screens support sharing via `ShareCompat` and copying with `ClipboardManager`; tests cover intents and clipboard.

---

## Assessment Snapshot – Implementation Status and Gaps

### Current State (Summary)
- Navigation: Single-activity with bottom bar and Detail highlighting; pop-to-root behavior implemented.
- Data: Room schema for morphèmes, history, favorites, meta; CSV seeding on first run; Admin reset + reseed flow.
- Generator: Word composition with elision/liaison/duplicate-boundary handling and plausibility score; unit tests for rules.
- Screens: Main, History, Favorites, Thematic, Workshop (manual with live preview + commit), Detail, Settings (stub), About, Debug, Onboarding (3 screens).
- Share/Copy: Implemented with instrumentation tests.

### Gaps vs AGENTS.MD Milestones
- M1 filters/composer: Coherence rules exist but need refinement; Definition mode not exposed in UI nor persisted.
- M2 gestures: Shake-to-generate not implemented.
- M4 thematic: Tag UI shows subset only; selection not persisted; weighting for multi-tag not tuned.
- M5 themes/settings: Only a basic theme stub; no theme variants or persisted preferences.
- Cross-cut polish: No swipe/undo in History, limited loading/error states, a11y/contrast/content descriptions need pass.

### Milestone Status (Quick View)
- M0: Complete (schema + seed).
- M1: Partial (needs rule refinements + tests; definition mode integration).
- M2: Partial (main UI done; gestures pending).
- M3: Mostly complete (add swipe/undo polish).
- M4: Baseline complete; improve tag UX/weighting and persist selection.
- M5: Not started (themes + settings + persistence).
- M6: Complete (share/copy).
- M7: Onboarding implemented; polish/a11y/performance ongoing.

### Prioritized Next Steps (Accepted)
1) Themes & Settings (DataStore + 3 themes + toggles)
2) Definition Mode UI + persistence + composer improvements
3) Thematic biasing UX + weighting + persistence
4) History swipe/undo and refresh behavior
5) Shake-to-generate (sensors + debounce)
6) Loading/error states and a11y pass
7) Tests for generator/service/seeding and simple navigation
8) CSV curation and validation; optional CI enhancement for instrumented tests
