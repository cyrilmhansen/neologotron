Neologotron ETL Agent Plan (Wiktionary → High‑Quality Stems)

Goal
- Build and maintain a high‑quality, large stems/prefix/suffix database focused on French word generation, leveraging Wiktionary (French + English + Translingual) and selected open sources. Provide an interactive ETL that guides contributors from a clean checkout to an app with an up‑to‑date generator database.

Scope
- Languages: prioritize French with classical Greek/Latin stems; include Translingual; opportunistically use English entries where FR is sparse.
- Artifacts: three CSVs the app already consumes: prefixes, roots (racines), suffixes. Extended columns are allowed and safely ignored by the app.
- UX: a guided CLI/TUI to fetch/prepare data, preview diffs, curate ambiguous entries, and export to `app/src/main/assets/seed/`.

Key Files
- Transform script: `etl/wiktextract_to_neologotron.py` (JSONL → CSVs)
- Sample input: `etl/sample_wiktextract.jsonl`
- App seeds path: `app/src/main/assets/seed/`


Data Sources (Open, Sustainable)
- Primary: Wiktionary via wiktextract JSONL
  - Options:
    1) Prebuilt JSONL from Kaikki.org (daily wiktextract exports). Pros: simple, fast. Cons: occasionally lag behind; structure closely matches wiktextract.
    2) Local wiktextract run on recent frwiktionary/enwiktionary dumps. Pros: fully reproducible/offline. Cons: heavy initial run.
- Secondary: Curated lists (CC‑BY‑SA) of Greek/Latin roots (e.g., Wikipedia pages). Use for coverage/validation, keep provenance per row.
- Avoid: proprietary sources (Etymonline, CNRTL scraping) due to licensing/API constraints.

Kaikki FR extract findings (validated)
- File: `etl/fr-extract.jsonl.gz` (Kaikki.org French Wiktionary extract) — confirmed compatible with our importer.
- Observed fields: `word`, `lang_code` (e.g., "fr"), `lang` (localized, e.g., "Français"), `pos` (English label, e.g., "prefix", "suffix"), `pos_title` (localized, e.g., "Préfixe"), `senses[ ].glosses`, `forms`, `sounds[ ].ipa`, `etymology_texts`.
- Not observed: `etymology_templates` in FR extract (often missing). We now use `etymology_text(s)` as a fallback to detect Greek/Latin lineage.
- Contents span many languages (entries hosted on frwiki), so the importer must filter by `--lang fr` (done).

Licensing & Attribution
- Wiktionary content is CC BY‑SA 3.0. We must:
  - Keep attribution and provenance. We include a `sources` column pointing to page anchors.
  - Treat derived content under share‑alike terms. App may include a “Sources” screen and repo must maintain attribution.
  - Clearly mark other sources with their license per row (e.g., Wikipedia CC BY‑SA).

Attribution & Credits (What to show in About)
- Wiktionary: Text is available under the Creative Commons Attribution‑ShareAlike license (currently CC BY‑SA 4.0 for reusers; historically CC BY‑SA 3.0 on contribution). See
  - https://fr.wiktionary.org/wiki/Wiktionnaire:Licence
  - https://creativecommons.org/licenses/by-sa/4.0/
- Data extraction: Built from Wiktionary using wiktextract (by Tatu Ylonen) and Kaikki.org raw data.
- Preferred academic citation for wiktextract (from Kaikki):
  - Tatu Ylonen. “Wiktextract: Wiktionary as Machine‑Readable Structured Data.” LREC 2022, pp. 1317–1325. Marseille. https://github.com/tatuylonen/wiktextract
- Kaikki.org: Raw data downloads and website by Tatu Ylonen. Linking to relevant pages on https://kaikki.org is appreciated.

Recommended About screen content (ready‑to‑paste)
- “This app includes content from Wiktionary, which is available under the Creative Commons Attribution‑ShareAlike (CC BY‑SA) license. The data has been extracted using wiktextract and the Kaikki.org raw datasets. Our processing may modify, combine, and annotate the original content.”
- “Wiktionary (French): https://fr.wiktionary.org/ — License: CC BY‑SA 4.0 (see https://creativecommons.org/licenses/by-sa/4.0/)”
- “wiktextract project: https://github.com/tatuylonen/wiktextract — Please cite: T. Ylonen, LREC 2022.”
- “Kaikki.org raw data: https://kaikki.org/dictionary/rawdata.html — All languages dump: https://kaikki.org/dictionary/raw-wiktextract-data.jsonl.gz — French dump: https://kaikki.org/dictionary/downloads/fr/fr-extract.jsonl.gz”
- “Example build (as used in our runs): Extracted on 2025‑08‑18 from the enwiktionary dump dated 2025‑08‑02 using wiktextract (commits 0c45963, 3c020d2). See https://kaikki.org for details.”

Repository notices (recommended)
- Add a `THIRD-PARTY-NOTICES.md` at the repo root, summarizing:
  - Wiktionary CC BY‑SA license, links, and that our CSVs derived from Wiktionary are distributed under CC BY‑SA.
  - Kaikki.org and wiktextract acknowledgments + the LREC citation text.
  - Any other sources (e.g., Wikipedia CC BY‑SA) used for supplemental fields.
- Keep a machine‑readable `etl/runs/<date>/run.json` with dump dates, URLs, wiktextract versions/commits and our flags. Link to that in About if desired.

Per‑row provenance in UI (optional)
- The ETL writes a `sources` column per entry (e.g., `wiktionary:fr:<pageid>:<word>#<pos>`). Consider showing a small “Source: Wiktionary” link on detail screens that opens the relevant page.


Target Data Model (CSV columns)
- Prefixes (`neologotron_prefixes.csv`):
  - Required by app: id, form, alt_forms, gloss, origin, connector, phon_rules, tags, weight
  - Optional (kept by ETL): ety_lang, ety_desc, ety_lineage, proto_form, ipa, attest_from, cognates, sources, examples
- Roots (`neologotron_racines.csv`):
  - Required: id, form, alt_forms, gloss, origin, domain, connector_pref, examples, weight
  - Optional: root_lang, proto_root, ety_desc, ety_lineage, semantic_field, sources
- Suffixes (`neologotron_suffixes.csv`):
  - Required: id, form, alt_forms, gloss, origin, pos_out, def_template, tags, weight
  - Optional: ety_lang, ety_desc, ety_lineage, proto_form, ipa, attest_from, cognates, sources, examples

Notes
- The Android app reads only the first 9 columns; extra columns are ignored safely (see `app/src/main/java/com/neologotron/app/data/seed/SeedManager.kt`). This allows us to enrich now and adopt later.


Quality Strategy (Largest, Cleanest Coverage)
- Multi‑source merge: FR + Translingual as base; pull EN as fallback to fill gaps (sense gloss, alt forms, examples).
- Normalization:
  - Canonicalize forms: hyphen handling (`bio-`, `-logie`, `morpho-`), lowercase, accent stripping only for IDs (not for display).
  - Merge alt forms and spelling variants; keep unique forms.
- Affix/root detection:
  - Use `pos`/`pos_title` with accent‑insensitive checks for “prefix(e)”, “suffix(e)”, “affix(e)”, “combining form”, “élément de composition/formant”, “confix”.
  - Treat Translingual/Greek/Latin combining forms as roots; infer classical origin from lineage/templates.
- Filtering:
  - Default to “classical” (Greek/Latin) entries for first‑class dataset; allow `--origin-filter none` to keep all.
  - Classical detection uses origin code, lineage templates when available, and the etymology text as fallback (important for Kaikki FR which often lacks `etymology_templates`).
- Provenance & reproducibility:
  - Always preserve `sources` (pageid+pos anchor). Keep run metadata (dumps used, dates, flags) in a `run.json`.
- Weighting and domains:
  - Populate `tags`/`domain` from sense topics; map to concise FR labels (e.g., “biology”→“science”).
  - Optionally derive weights from coverage or future frequency lists.


End‑to‑End Workflow (Interactive CLI)
We will add a Python CLI (`etl/cli.py`) orchestrating the pipeline. Subcommands:
1) `init`: ensure Python venv, install deps (wiktextract optional).
2) `fetch`: download latest dumps or Kaikki JSONL for FR, EN, and Translingual.
3) `extract`: run wiktextract locally if using dumps; otherwise, verify JSONL structure.
4) `transform`: invoke `etl/wiktextract_to_neologotron.py` per language; merge outputs (FR base, add EN as fallback), dedupe.
5) `review`: open TUI for curation (see below). Save decisions to `review/decisions.jsonl`.
6) `export`: write final CSVs to `app/src/main/assets/seed/` and emit a short changelog.
7) `report`: summarize counts, coverage, diffs vs. previous export.

Directory layout for a run (idempotent)
- `etl/runs/2025-08-31/`
  - `raw/` (dumps or downloaded JSONL)
  - `jsonl/` (wiktextract outputs if run locally)
  - `csv/` (per‑lang transformed CSVs)
  - `merge/` (merged CSVs pre‑review)
  - `review/` (decisions, edits, rejections)
  - `export/` (final CSVs + `run.json`)


TUI Curation (Minimal, Fast)
- Implement with `textual` or `prompt_toolkit` (fallback: plain prompts for zero‑deps path).
- Views:
  - Queue: entries needing attention (low confidence origin; conflicts FR vs EN; very short gloss; missing lineage).
  - Detail: form, alt forms, gloss, tags, examples, lineage, source link; quick actions [accept/edit/reject/retag/merge].
  - Filters: search by regex, origin, domain, type (prefix/root/suffix).
- Decisions persist to `review/decisions.jsonl`; `export` applies them idempotently.


Implementation Plan (Phases)
Phase 1 — Baseline tooling
- Add `etl/cli.py` with `typer` or `argparse` subcommands (init/fetch/transform/export/report).
- Support both Kaikki JSONL and local wiktextract inputs.
- Use existing `wiktextract_to_neologotron.py`; add a small merge step to union FR + EN + MUL.
- Document end‑to‑end with the sample JSONL.

Phase 2 — Interactive review
- Implement `review` with a simple interactive CLI (first draft done), prioritizing “uncertain” entries (short/missing gloss, no origin, no tags, suspicious form).
- Allow in‑place edit via `field=value` pairs (gloss, origin, tags/domain, connector, pos_out, def_template, weight). Accept/reject/skip actions supported.
- Persist decisions to `etl/runs/<date>/review/decisions.jsonl`. `apply-review` applies edits/rejections idempotently, writes `export_reviewed/`, and updates app assets.
 - Filters (tunable): `--origin-lang grc,la,mul` (codes via `ety_lang`/`root_lang`), `--domains science,medicine,tech` (matches `tags`/`domain`), `--show-all` (include non‑uncertain entries), and `--csv` to pick type.

Phase 3 — Integration & DX polish
- Add a Gradle task `etlExport` to call `python etl/cli.py export` and copy CSVs.
- Add an “About → Sources” attribution view in app (Wiktionary + Wikipedia as needed).
- Optional: add a `make etl-full` target running the full pipeline with cached steps.

Stretch Goals
- Auto‑merge FR+EN with confidence scoring; prompt only high‑risk diffs.
- Integrate frequency lists (e.g., `wordfreq`) to weight popular roots.
- Optional per‑language support beyond French if the generator expands.
- Add unit tests for normalization/dedup heuristics.


How‑To (Quickstart)
Prereqs
- Python 3.10+
- Optional: `pip install wiktextract wikitextprocessor` (for local extraction)
- For TUI: `pip install textual` (optional; otherwise review falls back to prompts)

Sanity test with sample JSONL
- Transform a tiny sample into CSVs (writes under `etl/out_sample2/` by default in the repo):
  - `python3 etl/wiktextract_to_neologotron.py --input etl/sample_wiktextract.jsonl --out-dir etl/out_sample2 --lang fr --include-translingual --origin-filter classical --debug`

End‑to‑end (using Kaikki JSONL) — interactive CLI
- Single guided flow (prompts only for URLs, defaults provided):
  - `python3 etl/cli.py wizard`
  - Steps performed:
    - Download FR extract + All‑languages raw dump
    - Filter Translingual (mul) from the raw dump (unless `--fr-only`)
    - Transform FR and (optionally) MUL to CSVs via `wiktextract_to_neologotron.py`
    - Merge FR (preferred) + MUL into a single set (or use FR only if `--fr-only`)
    - Export CSVs to `app/src/main/assets/seed/`
    - Write run metadata to `etl/runs/<timestamp>/run.json`
  - Optional interactive curation flow:
    - Review: `python3 etl/cli.py review --run <timestamp> --csv all --limit 50`
    - Apply:  `python3 etl/cli.py apply-review --run <timestamp>`
  - FR‑only run (skip MUL):
    - `python3 etl/cli.py wizard --fr-only`

Post‑processing (gloss polishing)
- Shorten and localize glosses for better on‑device display (and to prefer FR):
  - `python3 etl/cli.py polish --run <timestamp> --max-chars 80 --map etl/gloss_map_fr.sample.json`
- Behavior:
  - Collapses multi‑clause glosses to the first clause; removes parentheticals; trims to `max-chars` with ellipsis.
  - Applies optional phrase‑level FR mapping (customizable JSON). Does not call external services.
- Future: an AI‑assisted mode can translate + summarize missing FR glosses; batch prompts per run and cache decisions, while keeping manual review in the loop.

AI Import (apply AI results)
- Prepare candidates + prompt:
  - `python3 etl/cli.py prep-ai --run <timestamp> --count 10 --min-len 90`
  - Produces `runs/<ts>/ai/candidates.jsonl` and `runs/<ts>/ai/prompt.txt`
- After getting AI JSONL (one object per line: `{ id, short_gloss_fr, keep?, pos_out? }`), apply edits:
  - `python3 etl/cli.py import-ai --run <timestamp> --ai-jsonl /path/to/ai.jsonl`
  - Writes updated CSVs to `runs/<ts>/ai_imported/` and an IDs file `runs/<ts>/ai/edited_ids.txt`
  - Review only edited entries:
    - `python3 etl/cli.py review --run <timestamp> --ids-file runs/<ts>/ai/edited_ids.txt`
  - Apply and export:
    - `python3 etl/cli.py apply-review --run <timestamp>`

AI Import – advanced actions (canon/alt/exclure)
- `import-ai` reconnaît aussi (facultatifs) dans le JSONL IA:
  - `duplicate_of` ou `alt_form_for`: propose une action `alt_form` vers `target_form`.
  - `prefer_canon` (booléen): propose une action `canonize` pour faire de cette forme le canon FR.
  - `rationale` (texte court): justification à afficher pendant la revue.
- L’outil écrit un plan d’actions pour revue: `runs/<ts>/ai/actions_todo.jsonl`.
- Flux recommandé: ouvrir `actions_todo.jsonl` pendant la revue et valider/rejeter les propositions; une étape future appliquera ces actions automatiquement (BACKLOG).

Politique préfixes courts (1–3 caractères)
- Deny-list (par défaut, exclu du set public): `D-`, `L-`, `R-`, `S-`, `RS-`, `DL-`, `n-`, `m-`, `u-`, `z-`, `r-`, `q-` (notations SI, stéréochimie…).
- Allow-list explicite (préfixes courts utiles): `a-`, `co-`, `re-`, `dé-` (ou `de-`), `ex-`, `in-` (+ assimilations `im-`, `il-`, `ir-`), et canons fréquents `bio-`, `geo-`, `pro-`.
- Implémentation prévue: fichier `etl/short_prefix_policy.json` et filtre appliqué dans le wizard (BACKLOG), plus revue prioritaire des 1–2 lettres restants.

Préfixes multiples (enchaînés) — plan
- Option expérimentale (OFF par défaut) pour composer `pref2 + pref1 + racine + suffixe` avec assimilation phonétique.
- Sélection: `pref2` tiré de la allow-list, distinct de `pref1` et non abréviatif.
- Définition: reste basée sur racine + suffixe (glosse courte). Toggle en Paramètres.

LLM locaux (ollama / LM Studio / llamafile)
- Objectif: permettre les étapes IA sans service externe, avec prompts multiples et lots maîtrisés.
- Intégration (BACKLOG):
  - Ajout d’un sous-commande `ai-run` configurable (endpoint HTTP, modèle, taille de lot, température, max tokens, timeout).
  - Formats supportés: ollama (http://localhost:11434), LM Studio (http://localhost:1234), llamafile (binaire local avec serveur intégré).
  - Cache de réponses sous `runs/<ts>/ai/cache/` (clé = hash(prompt+entrée)).
  - Garde-fous: nombre de lignes max/s, reprise après échec, dry-run.

Prochaines étapes (TODO)
- Appliquer automatiquement `actions_todo.jsonl` (canonize/alt_form/exclude) ou générer un patch CSV pour revue.
- Ajouter `short_prefix_policy.json` + filtre deny/allow dans le wizard, et test unitaire associé.
- Étendre `prep-ai` avec prompt FR enrichi (exemples) et post-nettoyage optionnel (suppression “ou”, anglicismes).
- Ajouter `ai-run` pour ollama/LM Studio/llamafile et un cache.
End‑to‑end (local wiktextract) — planned CLI
- Download the latest frwiktionary/enwiktionary XML dumps (bz2) and run wiktextract (consult the upstream README for exact flags).
- `python3 etl/cli.py extract --wiktionary fr --dump frwiktionary-latest-pages-articles.xml.bz2 --out runs/<date>/jsonl/fr.jsonl`
- `python3 etl/cli.py extract --wiktionary en --dump enwiktionary-latest-pages-articles.xml.bz2 --out runs/<date>/jsonl/en.jsonl`
- Continue with `transform`, `review`, `export`.


Merging Strategy (FR + EN + MUL)
- Identity: key on normalized form + type (prefix/root/suffix). Keep raw forms and alt forms in display.
- Field precedence: FR > EN for gloss/labels; Translingual informs origin and examples; always union alt forms and tags.
- Conflict resolution: present in `review` view; default to FR if undecided; attach secondary gloss as note if helpful.

Notes for Kaikki.org usage
- Fetch per‑language JSONL separately (e.g., `fr-extract.jsonl.gz`, optionally `en-extract.jsonl.gz`, and `mul-extract.jsonl.gz` for Translingual). Run `transform` for each and then merge.
- Translingual entries are not included in `fr-extract.jsonl.gz`; to incorporate combining forms from Translingual, include `mul-extract.jsonl.gz` in the pipeline.
 - There is no separate `mul-extract.jsonl.gz` under per‑edition downloads. Instead, use the English‑edition raw dump and filter for `lang_code == "mul"`:
   - Download raw (already compressed): `curl -L -o etl/raw-enwiktionary.jsonl.gz https://kaikki.org/dictionary/raw-wiktextract-data.jsonl.gz`
   - Extract only Translingual lines with on‑the‑fly compression (preferred with jq):
     - `gzip -cd etl/raw-enwiktionary.jsonl.gz | jq -c 'select(.lang_code=="mul")' | gzip -c > etl/mul-extract.jsonl.gz`
   - Fallback without jq (fixed‑string match, one JSON object per line):
     - `gzip -cd etl/raw-enwiktionary.jsonl.gz | rg -F '"lang_code":"mul"' | gzip -c > etl/mul-extract.jsonl.gz`
   - Or feed the raw file directly to the transformer (it reads .gz): `python3 etl/wiktextract_to_neologotron.py --input etl/raw-enwiktionary.jsonl.gz --out-dir etl/out_mul --lang fr --include-translingual --roots-from-translingual --mul-fallback-classical`
   - If you need the post‑processed “All languages combined” dataset, compress while downloading:
     - `curl -L 'https://kaikki.org/dictionary/All%20languages%20combined/kaikki.org-dictionary-all.jsonl' | gzip -c > etl/kaikki-all.jsonl.gz`

Recommended flags for Translingual (mul) inputs
- `--roots-from-translingual`: also produce root rows from classical Translingual prefixes/suffixes.
- `--mul-fallback-classical`: when etymology markers are missing, accept affix‑looking Translingual forms (leading/trailing hyphen) as classical.
- If you want to inspect full coverage, temporarily use `--origin-filter none`.


Open Technical Tasks (Backlog)
- Add `etl/cli.py` with subcommands and run registry under `etl/runs/`.
- Implement `merge.py` to union per‑lang CSVs with precedence and dedup.
- Add small utility to verify CSV schema and counts; fail early if headers mismatch.
- Add `--lang en` support path in `wiktextract_to_neologotron.py` docs and test it with en JSONL.
- Implement `review` TUI (Textual) and decisions store.
- Gradle task `etlExport` that depends on `export` and copies artifacts.
- App: ensure an attribution view for sources.

Completed Adjustments
- Importer updated to use `etymology_text(s)` in addition to lineage/templates when filtering by classical origin, ensuring Kaikki FR entries like `-logie` are retained.


Design Notes & Heuristics
- Connector vowel detection: many classical roots use interfix “o”. Keep the heuristic simple for now (present in form), but allow manual override in review.
- Suffix `pos_out`: can be predicted via a lookup (e.g., `-logie` → noun). Start with null; add a small mapping later.
- Phonological rules: track common assimilation (e.g., in‑ → im‑ before labials) as descriptive text; keep optional string until modeled.
- Topics to domains: keep a minimal mapping to a handful of domains for the app’s filters.
- Performance: process JSONL streaming; expose `--limit-lines` and regex `--match` for sampling; avoid high memory peaks.


Verification & CI Hooks
- Add a script to compare exported CSVs vs previous export: added/removed/changed rows by `id`.
- Validate CSV headers and non‑empty key fields (`id`, `form`).
- Spot‑check 20 random rows with source links.


Appendix: Useful Paths & References
- Transform script: `etl/wiktextract_to_neologotron.py`
- Sample input: `etl/sample_wiktextract.jsonl`
- Android seed destination: `app/src/main/assets/seed/`
- Reader code: `app/src/main/java/com/neologotron/app/data/seed/SeedManager.kt`

Status
- Scripted transform exists and works with sample. The interactive pipeline is implemented:
  - `etl/cli.py wizard` downloads FR + MUL (from raw), transforms, merges, exports to app assets; `--fr-only` supported.
  - Clean progress: single‑line download speed/ETA, spinner during transforms, filtered MUL scan progress, and copy progress.
  - Interactive review: `review` + `apply-review` with filters (`--origin-lang`, `--domains`, `--show-all`).
  - About screen attribution implemented with CC BY‑SA + wiktextract citation and Kaikki links.
