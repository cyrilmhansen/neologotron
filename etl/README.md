Neologotron ETL (wizard + wiktextract → CSV)

Overview
- Two ways to generate seed CSVs for the app:
  1) Interactive wizard (recommended): fetches Kaikki.org datasets, transforms, merges FR+MUL, and exports to the app assets.
  2) Standalone transform script: converts a wiktextract/Kaikki JSONL into CSVs.

- Output CSVs (compatible with the app):
  - `neologotron_prefixes.csv`
  - `neologotron_racines.csv`
  - `neologotron_suffixes.csv`

Inputs
- Wiktextract JSONL for French and/or Translingual entries. Generate with the wiktextract toolchain from a frwiktionary dump.
- Optionally, a local JSON mapping file for Wikidata QIDs (offline; see `--wikidata-map`).

Outputs
- CSVs include both current app columns and extra, optional etymology columns (kept nullable). The app can ignore unknown columns; extend entities to consume them when ready.

Quickstart (recommended wizard)
```
python3 etl/cli.py wizard
```
- Prompts once to confirm the default Kaikki URLs, then runs end‑to‑end:
  - Downloads: FR extract + All‑languages raw (for Translingual, unless `--fr-only`)
  - Filters Translingual (mul) from raw dump
  - Transforms FR and MUL with sensible flags
  - Merges FR‑preferred + MUL and copies CSVs to `app/src/main/assets/seed/`
  - Writes run metadata under `etl/runs/<timestamp>/run.json`

Options
- FR only (skip Translingual):
```
python3 etl/cli.py wizard --fr-only
```
- Interactive review (optional but useful):
```
python3 etl/cli.py review --run <timestamp> --csv all --limit 50
python3 etl/cli.py apply-review --run <timestamp>
```
- Review filters:
  - `--origin-lang grc,la,mul`  (match ety_lang/root_lang)
  - `--domains science,medicine,tech`  (match tags/domain substrings)
  - `--show-all`  (not only “uncertain” entries)

AI-assisted gloss refinement (local LLM)
```
python3 etl/cli.py prep-ai --run <timestamp> --count 20
python3 etl/cli.py ai-run --run <timestamp> --endpoint http://localhost:11434/v1/chat/completions \
  --model mistral --batch 4 --temperature 0.7 --max-tokens 200 --timeout 30
python3 etl/cli.py import-ai --run <timestamp> --ai-jsonl etl/runs/<timestamp>/ai/output.jsonl
```
- `ai-run` streams responses and caches them under `etl/runs/<timestamp>/ai/cache/` so reruns reuse previous results.

Reseed the app database
- Open the app → Settings → Debug → “Reset database” to load the new seeds from assets.

Standalone transform script
```
python3 wiktextract_to_neologotron.py \
  --input frwiktionary-wiktextract.jsonl \
  --out-dir ../app/src/main/assets/seed \
  --lang fr --include-translingual
```

Notes
- Licensing: Wiktionary content is CC BY-SA. Keep source attribution; the script emits a `sources` column with page anchors for traceability.
- Offline: No network access is required. If you want Wikidata IDs, provide a local map file with `{ "<lemma>#<lang>": "Q123" }` pairs.
- Filtering: The script targets entries with `pos` in {prefix, suffix, affix, combining form}. It heuristically classifies roots from combining forms and well-known Greek/Latin stems.

See also
- `etl/AGENTS.MD` for the full plan and design notes.
