Neologotron ETL (wiktextract → CSV)

Overview
- Converts a wiktextract JSONL dump (from frwiktionary) into three CSV files compatible with Neologotron seeds:
  - `neologotron_prefixes.csv`
  - `neologotron_racines.csv`
  - `neologotron_suffixes.csv`

Inputs
- Wiktextract JSONL for French and/or Translingual entries. Generate with the wiktextract toolchain from a frwiktionary dump.
- Optionally, a local JSON mapping file for Wikidata QIDs (offline; see `--wikidata-map`).

Outputs
- CSVs include both current app columns and extra, optional etymology columns (kept nullable). The app can ignore unknown columns; extend entities to consume them when ready.

Usage
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

