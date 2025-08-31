#!/usr/bin/env python3
"""
Neologotron ETL Interactive CLI

Guided flow to fetch Kaikki.org datasets (FR + Translingual), transform to
Neologotron CSVs, merge, and export to the Android app assets.

User interaction is minimal by design: confirm or override default URLs upfront,
then the tool runs end-to-end.

Dependencies: Python 3.10+. No external Unix tools required.
"""
from __future__ import annotations

import argparse
import csv
import gzip
import io
import json
import os
import shutil
import sys
import time
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple
from urllib.request import urlopen, Request
from subprocess import run, CalledProcessError


REPO_ROOT = Path(__file__).resolve().parents[1]
ETL_DIR = REPO_ROOT / "etl"
APP_SEED_DIR = REPO_ROOT / "app" / "src" / "main" / "assets" / "seed"

DEFAULT_URL_FR = "https://kaikki.org/dictionary/downloads/fr/fr-extract.jsonl.gz"
DEFAULT_URL_ALL_RAW = "https://kaikki.org/dictionary/raw-wiktextract-data.jsonl.gz"


def _prompt(default: str, question: str) -> str:
    print(f"{question}\n  [Enter to accept]: {default}")
    resp = input("> ").strip()
    return resp or default


def _ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def _download(url: str, dest: Path) -> None:
    print(f"Downloading:\n  URL: {url}\n  → {dest}")
    _ensure_dir(dest.parent)
    req = Request(url, headers={"User-Agent": "neologotron-etl/1.0"})
    with urlopen(req) as r, open(dest, "wb") as f:
        # Stream with a reasonable chunk to avoid memory peaks
        total = 0
        chunk = 1024 * 1024
        last_report = time.time()
        while True:
            buf = r.read(chunk)
            if not buf:
                break
            f.write(buf)
            total += len(buf)
            now = time.time()
            if now - last_report > 1.5:
                print(f"  … {total/1_000_000:.1f} MB")
                last_report = now
    print(f"  Done: {dest.stat().st_size/1_000_000:.1f} MB")


def _filter_mul_lines(input_gz: Path, output_gz: Path) -> Tuple[int, int]:
    """Filter only Translingual (lang_code == 'mul') lines from an enwiktionary raw dump.
    Returns (read_lines, kept_lines).
    """
    print(f"Filtering Translingual from:\n  {input_gz}\n  → {output_gz}")
    _ensure_dir(output_gz.parent)
    read = kept = 0
    with gzip.open(input_gz, "rt", encoding="utf-8") as inp, gzip.open(output_gz, "wt", encoding="utf-8") as outp:
        for line in inp:
            read += 1
            if not line.strip():
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            if obj.get("lang_code") == "mul":
                kept += 1
                # Write compact JSON line
                outp.write(json.dumps(obj, ensure_ascii=False) + "\n")
            if read % 500000 == 0:
                print(f"  … scanned {read:,} lines; kept {kept:,}")
    print(f"  Kept {kept:,} / {read:,} lines")
    return read, kept


def _run_transform(input_path: Path, out_dir: Path, *, lang: str, include_translingual: bool,
                   roots_from_translingual: bool = False, mul_fallback_classical: bool = False,
                   origin_filter: str = "classical") -> None:
    """Invoke wiktextract_to_neologotron.py with the desired flags."""
    script = ETL_DIR / "wiktextract_to_neologotron.py"
    cmd = [sys.executable, str(script),
           "--input", str(input_path),
           "--out-dir", str(out_dir),
           "--lang", lang,
           "--origin-filter", origin_filter]
    if include_translingual:
        cmd.append("--include-translingual")
    if roots_from_translingual:
        cmd.append("--roots-from-translingual")
    if mul_fallback_classical:
        cmd.append("--mul-fallback-classical")
    print("Running:", " ".join(cmd))
    res = run(cmd)
    if res.returncode != 0:
        raise CalledProcessError(res.returncode, cmd)


CSV_FILES = [
    "neologotron_prefixes.csv",
    "neologotron_suffixes.csv",
    "neologotron_racines.csv",
]


def _merge_csvs(fr_dir: Path, mul_dir: Path, out_dir: Path) -> Dict[str, int]:
    """Merge FR-first with MUL supplemental by 'form'. Keep FR on conflicts."""
    _ensure_dir(out_dir)
    counts: Dict[str, int] = {}
    for name in CSV_FILES:
        src_fr = fr_dir / name
        src_mul = mul_dir / name
        dst = out_dir / name
        print(f"Merging {name}\n  FR : {src_fr}\n  MUL: {src_mul}\n  →  {dst}")
        rows: List[Dict[str, str]] = []
        forms_seen: Dict[str, Dict[str, str]] = {}
        headers: List[str] = []

        def _load(path: Path) -> List[Dict[str, str]]:
            if not path.exists():
                return []
            with open(path, "r", encoding="utf-8", newline="") as f:
                r = csv.DictReader(f)
                nonlocal headers
                if not headers:
                    headers = r.fieldnames or []
                return list(r)

        fr_rows = _load(src_fr)
        mul_rows = _load(src_mul)

        # First FR rows
        for rec in fr_rows:
            forms_seen[rec.get("form", "")] = rec
        # Then MUL rows only if new form
        for rec in mul_rows:
            f = rec.get("form", "")
            if f and f not in forms_seen:
                forms_seen[f] = rec

        rows = list(forms_seen.values())
        with open(dst, "w", encoding="utf-8", newline="") as f:
            w = csv.DictWriter(f, fieldnames=headers)
            w.writeheader()
            w.writerows(rows)
        counts[name] = len(rows)
        print(f"  Wrote {len(rows)} rows")
    return counts


def _copy_to_assets(src_dir: Path) -> None:
    _ensure_dir(APP_SEED_DIR)
    for name in CSV_FILES:
        src = src_dir / name
        dst = APP_SEED_DIR / name
        print(f"Copying {src} → {dst}")
        shutil.copyfile(src, dst)


def _decisions_path(run_dir: Path) -> Path:
    return run_dir / "review" / "decisions.jsonl"


def _load_decisions(path: Path) -> Dict[str, dict]:
    m: Dict[str, dict] = {}
    if not path.exists():
        return m
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            key = obj.get("id")
            if key:
                m[key] = obj
    return m


def _append_decision(path: Path, record: dict) -> None:
    _ensure_dir(path.parent)
    with open(path, "a", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False) + "\n")


def _is_uncertain(rec: Dict[str, str]) -> bool:
    gloss = (rec.get("gloss") or "").strip()
    origin = (rec.get("origin") or "").strip()
    tags = (rec.get("tags") or rec.get("domain") or "").strip()
    form = (rec.get("form") or "").strip()
    # Simple heuristics: short/empty gloss, missing origin, empty tags, suspicious form
    if len(gloss) < 6:
        return True
    if not origin:
        return True
    if not tags:
        return True
    if any(c.isupper() for c in form if c.isalpha()):
        return True
    return False


def _review_loop(headers: List[str], rows: List[Dict[str, str]], decisions: Dict[str, dict], dec_path: Path, limit: int | None, show_all: bool) -> int:
    count = 0
    for rec in rows:
        rid = rec.get("id") or rec.get("form")
        if not rid:
            continue
        if rid in decisions:
            continue
        if not show_all and not _is_uncertain(rec):
            continue
        # Show compact view
        print("-" * 60)
        print(f"id: {rec.get('id','')}  form: {rec.get('form','')}  origin: {rec.get('origin','')}")
        print(f"gloss: {rec.get('gloss','')}")
        if 'tags' in rec:
            print(f"tags: {rec.get('tags','')}")
        if 'domain' in rec:
            print(f"domain: {rec.get('domain','')}")
        print("Actions: [a]ccept  [e]dit  [r]eject  [s]kip  [q]uit")
        while True:
            cmd = input("> ").strip().lower()
            if cmd in ("a", ""):
                _append_decision(dec_path, {"id": rid, "action": "accept"})
                break
            if cmd == "r":
                _append_decision(dec_path, {"id": rid, "action": "reject"})
                break
            if cmd == "s":
                # Leave undecided for future sessions
                break
            if cmd == "e":
                print("Enter field=value (comma-separated). Known fields include: gloss, origin, tags, domain, connector, pos_out, def_template, weight")
                raw = input("edit> ").strip()
                if not raw:
                    continue
                updates: Dict[str, str] = {}
                for pair in raw.split(","):
                    if "=" in pair:
                        k, v = pair.split("=", 1)
                        updates[k.strip()] = v.strip()
                _append_decision(dec_path, {"id": rid, "action": "edit", "updates": updates})
                break
            if cmd == "q":
                return count
        count += 1
        if limit and count >= limit:
            break
    return count


def cmd_review(args) -> int:
    run_dir = ETL_DIR / "runs" / args.run if args.run else _latest_run_dir()
    if not run_dir or not run_dir.exists():
        print("No run directory found. Run 'python etl/cli.py wizard' first.", file=sys.stderr)
        return 2
    merged_dir = run_dir / "merged"
    dec_path = _decisions_path(run_dir)
    decisions = _load_decisions(dec_path)
    targets = []
    if args.csv in ("prefixes", "all"):
        targets.append(merged_dir / "neologotron_prefixes.csv")
    if args.csv in ("suffixes", "all"):
        targets.append(merged_dir / "neologotron_suffixes.csv")
    if args.csv in ("roots", "all"):
        targets.append(merged_dir / "neologotron_racines.csv")
    total = 0
    origin_langs = None
    if args.origin_lang:
        origin_langs = {x.strip().lower() for x in args.origin_lang.split(",") if x.strip()}
    domain_terms = None
    if args.domains:
        domain_terms = [x.strip().lower() for x in args.domains.split(",") if x.strip()]

    for path in targets:
        if not path.exists():
            continue
        headers, rows = _load_csv(path)
        # Apply pre-filters
        if origin_langs:
            def _row_lang(rec: Dict[str, str]) -> str | None:
                return (rec.get("root_lang") or rec.get("ety_lang") or "").lower() or None
            rows = [r for r in rows if _row_lang(r) in origin_langs]
        if domain_terms:
            def _has_domain(rec: Dict[str, str]) -> bool:
                text = (rec.get("domain") or rec.get("tags") or "").lower()
                return any(t in text for t in domain_terms)
            rows = [r for r in rows if _has_domain(r)]

        reviewed = _review_loop(headers, rows, decisions, dec_path, args.limit, args.show_all)
        total += reviewed
    print(f"Saved decisions to: {dec_path}")
    print(f"Reviewed {total} entries")
    return 0


def cmd_apply_review(args) -> int:
    run_dir = ETL_DIR / "runs" / args.run if args.run else _latest_run_dir()
    if not run_dir or not run_dir.exists():
        print("No run directory found. Run 'python etl/cli.py wizard' first.", file=sys.stderr)
        return 2
    merged_dir = run_dir / "merged"
    out_dir = Path(args.out_dir) if args.out_dir else (run_dir / "export_reviewed")
    dec_path = _decisions_path(run_dir)
    decisions = _load_decisions(dec_path)
    print(f"Applying decisions from {decisions and dec_path or '(none)'}")
    for name in CSV_FILES:
        src = merged_dir / name
        if not src.exists():
            continue
        headers, rows = _load_csv(src)
        out_rows: List[Dict[str, str]] = []
        for rec in rows:
            rid = rec.get("id") or rec.get("form")
            dec = decisions.get(rid)
            if not dec:
                out_rows.append(rec)
                continue
            if dec.get("action") == "reject":
                continue
            if dec.get("action") == "edit":
                updates = dec.get("updates", {})
                new_rec = dict(rec)
                for k, v in updates.items():
                    if k in headers:
                        new_rec[k] = v
                out_rows.append(new_rec)
            else:
                out_rows.append(rec)
        _write_csv(out_dir / name, headers, out_rows)
        print(f"  {name}: {len(out_rows)} rows")
    # Copy to assets
    _copy_to_assets(out_dir)
    print("Exported reviewed CSVs and updated app assets. Use Debug → Reset database to reload.")
    return 0


def _latest_run_dir() -> Path | None:
    runs_dir = ETL_DIR / "runs"
    if not runs_dir.exists():
        return None
    dirs = [p for p in runs_dir.iterdir() if p.is_dir()]
    if not dirs:
        return None
    return sorted(dirs)[-1]


def _load_csv(path: Path) -> Tuple[List[str], List[Dict[str, str]]]:
    with open(path, "r", encoding="utf-8", newline="") as f:
        r = csv.DictReader(f)
        headers = r.fieldnames or []
        return headers, list(r)


def _write_csv(path: Path, headers: List[str], rows: List[Dict[str, str]]) -> None:
    _ensure_dir(path.parent)
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=headers)
        w.writeheader()
        w.writerows(rows)


def wizard(args=None) -> int:
    print("Neologotron ETL Wizard — guided end-to-end setup")
    print("This will: download FR + Translingual dumps, transform, merge, and export to the app.")
    url_fr = _prompt(DEFAULT_URL_FR, "Enter French (fr) extract URL if changed")
    url_all = None if (args and args.fr_only) else _prompt(DEFAULT_URL_ALL_RAW, "Enter 'All languages raw' URL (for mul) if changed")

    # Prepare run directory
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    run_dir = ETL_DIR / "runs" / stamp
    raw_dir = run_dir / "raw"
    out_fr = run_dir / "csv_fr"
    out_mul = run_dir / "csv_mul"
    merged_dir = run_dir / "merged"
    export_dir = run_dir / "export"
    _ensure_dir(raw_dir)

    # 1) Download
    fr_path = raw_dir / "fr-extract.jsonl.gz"
    all_path = raw_dir / "raw-enwiktionary.jsonl.gz"
    _download(url_fr, fr_path)
    if not (args and args.fr_only):
        _download(url_all, all_path)

    # 2) Filter Translingual
    mul_path = None
    if not (args and args.fr_only):
        mul_path = raw_dir / "mul-extract.jsonl.gz"
        _filter_mul_lines(all_path, mul_path)

    # 3) Transform
    _run_transform(fr_path, out_fr, lang="fr", include_translingual=False, origin_filter="classical")
    if not (args and args.fr_only):
        _run_transform(mul_path, out_mul, lang="fr", include_translingual=True,
                       roots_from_translingual=True, mul_fallback_classical=True, origin_filter="classical")

    # 4) Merge, FR preferred
    if args and args.fr_only:
        # Use FR outputs directly as merged set
        _ensure_dir(merged_dir)
        for name in CSV_FILES:
            shutil.copyfile(out_fr / name, merged_dir / name)
    else:
        _merge_csvs(out_fr, out_mul, merged_dir)

    # 5) Export to app assets
    _copy_to_assets(merged_dir)

    # 6) Run metadata
    meta = {
        "timestamp": stamp,
        "fr_url": url_fr,
        "all_raw_url": url_all,
        "fr_only": bool(args and args.fr_only),
        "outputs": {name: str((merged_dir / name).resolve()) for name in CSV_FILES},
    }
    with open(run_dir / "run.json", "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)
    print(f"Run recorded: {run_dir / 'run.json'}")
    print("Done. You can now run an optional interactive review:")
    print(f"  python3 etl/cli.py review --run {stamp}")
    print("Then apply decisions:")
    print(f"  python3 etl/cli.py apply-review --run {stamp}")
    print("Or open the app and use Debug → Reset database to load the merged seeds now.")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser(description="Neologotron ETL interactive CLI")
    sub = ap.add_subparsers(dest="cmd", required=True)

    pw = sub.add_parser("wizard", help="Run the guided end-to-end flow (recommended)")
    pw.add_argument("--fr-only", action="store_true", help="only use FR extract (skip Translingual merge)")

    pr = sub.add_parser("review", help="Interactive curation of merged CSVs; stores decisions for later application")
    pr.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    pr.add_argument("--csv", choices=["prefixes", "suffixes", "roots", "all"], default="all", help="which CSV to review")
    pr.add_argument("--limit", type=int, help="maximum items to review")
    pr.add_argument("--origin-lang", help="comma-separated language codes to include (matches ety_lang/root_lang), e.g. 'grc,la,mul'")
    pr.add_argument("--domains", help="comma-separated domain/tag substrings to include, case-insensitive (e.g. 'science,medecine,tech')")
    pr.add_argument("--show-all", action="store_true", help="review all entries, not only 'uncertain'")

    pa = sub.add_parser("apply-review", help="Apply saved decisions to merged CSVs and export to app assets")
    pa.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    pa.add_argument("--out-dir", help="optional output dir (defaults to runs/<run>/export_reviewed)")

    args = ap.parse_args()
    if args.cmd == "wizard":
        return wizard(args)
    if args.cmd == "review":
        return cmd_review(args)
    if args.cmd == "apply-review":
        return cmd_apply_review(args)
    return 0


if __name__ == "__main__":
    sys.exit(main())
