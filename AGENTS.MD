Update this file to keep track of project progress, which tasks are done, which ones are in progress, which ones remains to do.
Note everything here that you may need to remember sooner or later if you need to update the project.

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
