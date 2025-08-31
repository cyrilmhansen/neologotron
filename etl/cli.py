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
import re
import json
import os
import shutil
import sys
import time
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple
from urllib.request import urlopen, Request
from subprocess import run, CalledProcessError, Popen, PIPE


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


def _fmt_bytes(n: float) -> str:
    units = ["B", "KB", "MB", "GB", "TB"]
    i = 0
    while n >= 1024 and i < len(units) - 1:
        n /= 1024.0
        i += 1
    return f"{n:.1f} {units[i]}"


def _fmt_eta(seconds: float) -> str:
    if seconds <= 0 or seconds != seconds:
        return "--:--"
    m, s = divmod(int(seconds), 60)
    h, m = divmod(m, 60)
    if h:
        return f"{h:d}:{m:02d}:{s:02d}"
    return f"{m:d}:{s:02d}"


def _download(url: str, dest: Path) -> None:
    print(f"Downloading:\n  URL: {url}\n  → {dest}")
    _ensure_dir(dest.parent)
    req = Request(url, headers={"User-Agent": "neologotron-etl/1.0"})
    start = time.time()
    with urlopen(req) as r, open(dest, "wb") as f:
        total_len = r.headers.get("Content-Length")
        total_len = int(total_len) if total_len and total_len.isdigit() else None
        downloaded = 0
        chunk = 1024 * 512  # 512KB chunks
        last_draw = 0.0
        line_len = 0
        while True:
            buf = r.read(chunk)
            if not buf:
                break
            f.write(buf)
            downloaded += len(buf)
            now = time.time()
            if now - last_draw >= 0.25:  # redraw 4x/sec
                elapsed = now - start
                speed = downloaded / elapsed if elapsed > 0 else 0.0
                if total_len:
                    remain = total_len - downloaded
                    eta = _fmt_eta(remain / speed if speed > 0 else 0)
                    pct = f"{(downloaded/total_len)*100:5.1f}%"
                    msg = f"  {pct} {_fmt_bytes(downloaded)}/{_fmt_bytes(total_len)}  at {_fmt_bytes(speed)}/s  ETA {eta}"
                else:
                    msg = f"  {_fmt_bytes(downloaded)}  at {_fmt_bytes(speed)}/s"
                pad = max(0, line_len - len(msg))
                sys.stdout.write("\r" + msg + (" " * pad))
                sys.stdout.flush()
                line_len = len(msg)
                last_draw = now
        # Final line
        elapsed = max(0.001, time.time() - start)
        speed = downloaded / elapsed
        if total_len:
            msg = f"  100.0% {_fmt_bytes(downloaded)}/{_fmt_bytes(total_len)}  at {_fmt_bytes(speed)}/s  ETA 0:00"
        else:
            msg = f"  {_fmt_bytes(downloaded)}  at {_fmt_bytes(speed)}/s"
        pad = max(0, line_len - len(msg))
        sys.stdout.write("\r" + msg + (" " * pad) + "\n")
        sys.stdout.flush()


def _filter_mul_lines(input_gz: Path, output_gz: Path) -> Tuple[int, int]:
    """Filter only Translingual (lang_code == 'mul') lines from an enwiktionary raw dump.
    Returns (read_lines, kept_lines).
    """
    print(f"Filtering Translingual from:\n  {input_gz}\n  → {output_gz}")
    _ensure_dir(output_gz.parent)
    read = kept = 0
    file_size = input_gz.stat().st_size if input_gz.exists() else None
    start = time.time()
    last_draw = 0.0
    line_len = 0
    with gzip.open(input_gz, "rt", encoding="utf-8") as inp, gzip.open(output_gz, "wt", encoding="utf-8") as outp:
        while True:
            line = inp.readline()
            if not line:
                break
            read += 1
            if line.strip():
                try:
                    obj = json.loads(line)
                except Exception:
                    obj = None
                if obj and obj.get("lang_code") == "mul":
                    kept += 1
                    outp.write(json.dumps(obj, ensure_ascii=False) + "\n")
            now = time.time()
            if now - last_draw >= 0.5:  # redraw 2x/sec
                elapsed = now - start
                speed = read / elapsed if elapsed > 0 else 0.0
                # Try to estimate percent by compressed bytes consumed if available via file position; gzip doesn't expose reliably, so show counts.
                msg = f"  scanned {read:,} lines; kept {kept:,}  (~{speed:,.0f} l/s)"
                pad = max(0, line_len - len(msg))
                sys.stdout.write("\r" + msg + (" " * pad))
                sys.stdout.flush()
                line_len = len(msg)
                last_draw = now
    sys.stdout.write("\n")
    sys.stdout.flush()
    print(f"  Kept {kept:,} / {read:,} lines")
    return read, kept


def _run_transform(input_path: Path, out_dir: Path, *, lang: str, include_translingual: bool,
                   roots_from_translingual: bool = False, mul_fallback_classical: bool = False,
                   origin_filter: str = "classical") -> None:
    """Invoke wiktextract_to_neologotron.py with a spinner until completion."""
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

    # Show spinner while the subprocess runs; capture output to print after
    label = f"Transform {input_path.name} → {out_dir.name}"
    print("Running:", " ".join(cmd))
    p = Popen(cmd, stdout=PIPE, stderr=PIPE, text=True)
    frames = ["⠋","⠙","⠹","⠸","⠼","⠴","⠦","⠧","⠇","⠏"]
    i = 0
    start = time.time()
    line_len = 0
    try:
        while True:
            ret = p.poll()
            now = time.time()
            elapsed = now - start
            spinner = frames[i % len(frames)]
            msg = f"  {spinner} {label}  elapsed {_fmt_eta(elapsed)}"
            pad = max(0, line_len - len(msg))
            sys.stdout.write("\r" + msg + (" " * pad))
            sys.stdout.flush()
            line_len = len(msg)
            if ret is not None:
                break
            time.sleep(0.1)
            i += 1
        out, err = p.communicate()
    finally:
        # Clear spinner line
        sys.stdout.write("\r" + (" " * line_len) + "\r")
        sys.stdout.flush()

    # Print subprocess output succinctly
    if out:
        sys.stdout.write(out)
    if err:
        sys.stderr.write(err)
    if p.returncode != 0:
        raise CalledProcessError(p.returncode, cmd)


CSV_FILES = [
    "neologotron_prefixes.csv",
    "neologotron_suffixes.csv",
    "neologotron_racines.csv",
]

SHORT_PREFIX_POLICY_PATH = ETL_DIR / "short_prefix_policy.json"


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


def _apply_short_prefix_policy(csv_dir: Path, policy: Dict[str, List[str]]) -> None:
    """Remove short prefixes not explicitly allowed and any denied prefixes."""
    prefixes_csv = csv_dir / "neologotron_prefixes.csv"
    if not prefixes_csv.exists():
        return
    allow = {p.lower() for p in policy.get("allow", [])}
    deny = {p.lower() for p in policy.get("deny", [])}
    with open(prefixes_csv, "r", encoding="utf-8", newline="") as f:
        reader = list(csv.DictReader(f))
        if not reader:
            return
        headers = reader[0].keys()
    filtered: List[Dict[str, str]] = []
    removed = 0
    for row in reader:
        form = (row.get("form") or "").lower()
        base = form.strip().strip("-")
        if base in deny:
            removed += 1
            continue
        if len(base) <= 2 and base not in allow:
            removed += 1
            continue
        filtered.append(row)
    if removed:
        print(f"  short prefix policy: removed {removed} prefixes from {csv_dir}")
    with open(prefixes_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=headers)
        writer.writeheader()
        writer.writerows(filtered)


def _copy_to_assets(src_dir: Path) -> None:
    _ensure_dir(APP_SEED_DIR)
    for name in CSV_FILES:
        src = src_dir / name
        dst = APP_SEED_DIR / name
        print(f"Copying {src.name} → {dst}")
        # Copy with progress
        total = src.stat().st_size if src.exists() else 0
        copied = 0
        start = time.time()
        last_draw = 0.0
        line_len = 0
        with open(src, "rb") as fsrc, open(dst, "wb") as fdst:
            while True:
                buf = fsrc.read(1024 * 1024)
                if not buf:
                    break
                fdst.write(buf)
                copied += len(buf)
                now = time.time()
                if now - last_draw >= 0.25:
                    elapsed = now - start
                    speed = copied / elapsed if elapsed > 0 else 0.0
                    if total:
                        eta = _fmt_eta((total - copied) / speed if speed > 0 else 0)
                        pct = f"{(copied/total)*100:5.1f}%"
                        msg = f"  {pct} {_fmt_bytes(copied)}/{_fmt_bytes(total)}  at {_fmt_bytes(speed)}/s  ETA {eta}"
                    else:
                        msg = f"  {_fmt_bytes(copied)}  at {_fmt_bytes(speed)}/s"
                    pad = max(0, line_len - len(msg))
                    sys.stdout.write("\r" + msg + (" " * pad))
                    sys.stdout.flush()
                    line_len = len(msg)
                    last_draw = now
        # Finalize line
        elapsed = max(0.001, time.time() - start)
        speed = copied / elapsed
        if total:
            msg = f"  100.0% {_fmt_bytes(copied)}/{_fmt_bytes(total)}  at {_fmt_bytes(speed)}/s  ETA 0:00"
        else:
            msg = f"  {_fmt_bytes(copied)}  at {_fmt_bytes(speed)}/s"
        pad = max(0, line_len - len(msg))
        sys.stdout.write("\r" + msg + (" " * pad) + "\n")
        sys.stdout.flush()


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

    # Optional ID restriction
    ids_set = None
    if getattr(args, 'ids_file', None):
        p = Path(args.ids_file)
        if p.exists():
            ids_set = {line.strip() for line in p.read_text(encoding='utf-8').splitlines() if line.strip()}

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
        if ids_set is not None:
            rows = [r for r in rows if (r.get('id') or '') in ids_set]

        reviewed = _review_loop(headers, rows, decisions, dec_path, args.limit, args.show_all)
        total += reviewed
    print(f"Saved decisions to: {dec_path}")
    print(f"Reviewed {total} entries")
    return 0


def cmd_import_ai(args) -> int:
    run_dir = ETL_DIR / "runs" / args.run if args.run else _latest_run_dir()
    if not run_dir or not run_dir.exists():
        print("No run directory found. Run 'python etl/cli.py wizard' first.", file=sys.stderr)
        return 2
    merged_dir = run_dir / "merged"
    out_dir = Path(args.out_dir) if args.out_dir else (run_dir / "ai_imported")
    ai_path = Path(args.ai_jsonl)
    if not ai_path.exists():
        print(f"AI JSONL not found: {ai_path}", file=sys.stderr)
        return 2

    # Load AI suggestions
    suggestions: Dict[str, Dict[str, str]] = {}
    edited_ids: List[str] = []
    actions_plan: List[Dict[str, str]] = []
    with ai_path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            _id = (obj.get("id") or "").strip()
            if not _id:
                continue
            if obj.get("keep") is False:
                # Skip edits if explicitly rejected
                continue
            sg = (obj.get("short_gloss_fr") or obj.get("short_gloss") or "").strip()
            po = (obj.get("pos_out") or "").strip()
            dup = (obj.get("duplicate_of") or "").strip()
            altfor = (obj.get("alt_form_for") or "").strip()
            prefer = obj.get("prefer_canon")
            rat = (obj.get("rationale") or "").strip()
            suggestions[_id] = {"gloss": sg, "pos_out": po}
            # Build an action plan entry if advanced fields present or keep==false was skipped above
            action = None
            target = None
            if dup:
                action = "alt_form"
                target = dup
            elif altfor:
                action = "alt_form"
                target = altfor
            elif isinstance(prefer, bool):
                action = "canonize" if prefer else None
            if action:
                actions_plan.append({
                    "id": _id,
                    "action": action,
                    **({"target_form": target} if target else {}),
                    **({"rationale": rat} if rat else {}),
                })

    if not suggestions:
        print("No applicable AI suggestions found.")
        return 0

    # Index merged rows by id to enrich actions with form/type
    id_to_meta: Dict[str, Dict[str, str]] = {}
    for name in CSV_FILES:
        path = merged_dir / name
        if not path.exists():
            continue
        headers, rows = _load_csv(path)
        typ = "prefix" if "prefixes" in name else ("suffix" if "suffixes" in name else "root")
        for rec in rows:
            rid = rec.get("id") or ""
            if rid:
                id_to_meta[rid] = {"form": rec.get("form") or "", "type": typ}

    # Enrich actions_plan with form/type
    for a in actions_plan:
        meta = id_to_meta.get(a["id"], {})
        if meta:
            a.setdefault("form", meta.get("form", ""))
            a.setdefault("type", meta.get("type", ""))

    # Apply to CSVs (only gloss/pos_out). Canon/alt/exclude are written as a plan for review.
    changed_total = 0
    for name in CSV_FILES:
        src = merged_dir / name
        if not src.exists():
            continue
        headers, rows = _load_csv(src)
        changed = 0
        for rec in rows:
            rid = rec.get("id") or ""
            if rid in suggestions:
                sug = suggestions[rid]
                sg = sug.get("gloss") or ""
                po = sug.get("pos_out") or ""
                if sg:
                    rec["gloss"] = sg
                if po and "suffixes" in name:
                    rec["pos_out"] = po
                changed += 1
                edited_ids.append(rid)
        if changed:
            _write_csv(out_dir / name, headers, rows)
            changed_total += changed
            print(f"  Applied AI to {name}: {changed} rows")
        else:
            # Still write original if other files changed, to keep set complete
            if out_dir.exists():
                _write_csv(out_dir / name, headers, rows)

    if changed_total == 0:
        print("No matching IDs from AI file were found in merged CSVs.")
        # Still write actions plan if any
        if actions_plan:
            ai_dir = run_dir / "ai"
            _ensure_dir(ai_dir)
            plan_path = ai_dir / "actions_todo.jsonl"
            with open(plan_path, "w", encoding="utf-8") as f:
                for obj in actions_plan:
                    f.write(json.dumps(obj, ensure_ascii=False) + "\n")
            print(f"Wrote action plan: {plan_path}")
        return 0

    # Write IDs file for focused review
    ai_dir = run_dir / "ai"
    _ensure_dir(ai_dir)
    ids_file = ai_dir / "edited_ids.txt"
    ids_file.write_text("\n".join(edited_ids) + "\n", encoding="utf-8")
    # Also write action plan file for canonicalisation/alt_forms/exclusion decisions
    plan_path = ai_dir / "actions_todo.jsonl"
    if actions_plan:
        with open(plan_path, "w", encoding="utf-8") as f:
            for obj in actions_plan:
                f.write(json.dumps(obj, ensure_ascii=False) + "\n")
    print(f"Wrote updated CSVs to: {out_dir}")
    print("Review the edited entries only:")
    print(f"  python3 etl/cli.py review --run {run_dir.name} --ids-file {ids_file}")
    if actions_plan:
        print("Additionally review canonicalisation/alt_forms/exclusion plan:")
        print(f"  {plan_path}")
    print("Then apply and export:")
    print(f"  python3 etl/cli.py apply-review --run {run_dir.name}")
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


def _shorten_gloss(text: str, max_chars: int) -> str:
    if not text:
        return text
    # Remove parenthetical segments and wiki bullets
    s = re.sub(r"\([^)]*\)", "", text)
    # Split at common separators and pick the first informative chunk
    for sep in [';', '.', '—', ':']:
        if sep in s:
            s = s.split(sep, 1)[0]
            break
    s = re.sub(r"\s+", " ", s).strip().strip('"').strip()
    if len(s) <= max_chars:
        return s
    # Trim at last space before limit and add ellipsis
    cut = s.rfind(' ', 0, max_chars)
    if cut < 0:
        cut = max_chars
    return s[:cut].rstrip() + "…"


def _load_map(path: Path | None) -> Dict[str, str]:
    if not path or not path.exists():
        return {}
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def _apply_map(text: str, m: Dict[str, str]) -> str:
    if not text or not m:
        return text
    # Exact phrase map first
    if text in m:
        return m[text]
    # Try lowercase match
    low = text.lower()
    for k, v in m.items():
        if low == k.lower():
            return v
    return text


def cmd_polish(args) -> int:
    run_dir = ETL_DIR / "runs" / args.run if args.run else _latest_run_dir()
    if not run_dir or not run_dir.exists():
        print("No run directory found. Run 'python etl/cli.py wizard' first.", file=sys.stderr)
        return 2
    merged_dir = run_dir / "merged"
    out_dir = Path(args.out_dir) if args.out_dir else (run_dir / "polished")
    fmap = _load_map(Path(args.map) if args.map else None)
    total = 0
    for name in CSV_FILES:
        src = merged_dir / name
        if not src.exists():
            continue
        headers, rows = _load_csv(src)
        for rec in rows:
            g = rec.get("gloss") or ""
            g = _apply_map(g, fmap)
            g = _shorten_gloss(g, args.max_chars)
            rec["gloss"] = g
        _write_csv(out_dir / name, headers, rows)
        total += len(rows)
        print(f"  Polished {name}: {len(rows)} rows (max {args.max_chars} chars)")
    _copy_to_assets(out_dir)
    print("Copied polished CSVs into app assets. Use Debug → Reset database to reload.")
    return 0


def _lang_hint_is_english(text: str) -> bool:
    if not text:
        return False
    t = text.lower()
    # Quick heuristic: common English stopwords and absence of accented chars
    en_stops = {" the "," of "," and "," to "," in "," with "," for "," by "," from "," that "," which "," used "," use "," form "}
    # pad with spaces to reduce false positives at boundaries
    tt = f" {t} "
    if any(w in tt for w in en_stops):
        return True
    # Likely French has « » or accented letters; if none present and many letters, might be EN
    if not any(c in t for c in "àâäèéêëîïôöùûüçœ«»") and sum(ch.isalpha() for ch in t) >= 10:
        return True
    return False


def cmd_prep_ai(args) -> int:
    import random
    run_dir = ETL_DIR / "runs" / args.run if args.run else _latest_run_dir()
    if not run_dir or not run_dir.exists():
        print("No run directory found. Run 'python etl/cli.py wizard' first.", file=sys.stderr)
        return 2
    merged_dir = run_dir / "merged"
    ai_dir = run_dir / "ai"
    _ensure_dir(ai_dir)

    # Collect candidates from all CSVs
    candidates: List[Dict[str, str]] = []
    for name in CSV_FILES:
        path = merged_dir / name
        if not path.exists():
            continue
        headers, rows = _load_csv(path)
        typ = "prefix" if "prefixes" in name else ("suffix" if "suffixes" in name else "root")
        for rec in rows:
            gloss = (rec.get("gloss") or "").strip()
            ety = (rec.get("ety_lang") or rec.get("root_lang") or "").strip()
            if len(gloss) >= args.min_len or _lang_hint_is_english(gloss) or ety.lower() in {"mul", "en"}:
                candidates.append({
                    "id": rec.get("id") or "",
                    "type": typ,
                    "form": rec.get("form") or "",
                    "gloss": gloss,
                    "ety_lang": ety,
                })
    random.shuffle(candidates)
    pick = candidates[: max(0, args.count)]
    out_jsonl = ai_dir / "candidates.jsonl"
    with open(out_jsonl, "w", encoding="utf-8") as f:
        for obj in pick:
            f.write(json.dumps(obj, ensure_ascii=False) + "\n")
    # Write a prompt template file for convenience
    prompt = f"""
You are helping refine morphological glosses for a French neologism generator (Neologotron).

Given JSON Lines with fields:
- id: string (stable identifier)
- type: one of [prefix, root, suffix]
- form: the morph form (e.g., "bio-", "-logie", "cephalo-")
- gloss: current gloss (may be English or too long)
- ety_lang: language code (e.g., fr, mul, grc, la)

For each input line, output one JSON line with:
- id: same as input
- short_gloss_fr: concise French gloss (<= 80 chars), lower-case, noun/adjective phrase, no trailing period; suitable for composing definitions; avoid unnecessary "de/du/d'"
- keep: true/false (false only if the entry is irrelevant for word-building)
- pos_out (optional, suffix only): one of [adj, adv, verb, noun, agent_noun, action_noun, result_noun] when evident; otherwise omit

Guidelines:
- Do not invent etymology; only translate/summarize meaning.
- Prefer compact fragments: prefixes → idea fragment (e.g., "eau", "au-dessus"); roots → key concept (e.g., "tête"); suffixes → product/type (e.g., "étude", "agent", "qualité").
- Avoid full sentences, examples, or proper names. Keep neutrality.
- No extra commentary. Output ONLY JSON lines.
""".strip()
    with open(ai_dir / "prompt.txt", "w", encoding="utf-8") as f:
        f.write(prompt + "\n")
    print(f"Prepared {len(pick)} AI candidates:\n  {out_jsonl}\nPrompt template:\n  {ai_dir / 'prompt.txt'}")
    return 0


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

    # Load short prefix policy
    try:
        with open(SHORT_PREFIX_POLICY_PATH, "r", encoding="utf-8") as f:
            short_policy = json.load(f)
    except FileNotFoundError:
        short_policy = {}

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
    _apply_short_prefix_policy(out_fr, short_policy)
    if not (args and args.fr_only):
        _run_transform(mul_path, out_mul, lang="fr", include_translingual=True,
                       roots_from_translingual=True, mul_fallback_classical=True, origin_filter="classical")
        _apply_short_prefix_policy(out_mul, short_policy)

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
    pr.add_argument("--ids-file", help="file with IDs (one per line) to restrict review to specific entries")

    pa = sub.add_parser("apply-review", help="Apply saved decisions to merged CSVs and export to app assets")
    pa.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    pa.add_argument("--out-dir", help="optional output dir (defaults to runs/<run>/export_reviewed)")

    pp = sub.add_parser("polish", help="Shorten and optionally translate glosses, then copy to assets")
    pp.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    pp.add_argument("--max-chars", type=int, default=80, help="max characters for gloss (default: 80)")
    pp.add_argument("--map", help="optional JSON mapping file for phrase→FR gloss replacements")
    pp.add_argument("--out-dir", help="optional output dir (defaults to runs/<run>/polished)")

    paip = sub.add_parser("prep-ai", help="Prepare a random set of EN/long glosses for AI polishing (no API calls)")
    paip.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    paip.add_argument("--count", type=int, default=10, help="number of candidates (default 10)")
    paip.add_argument("--min-len", type=int, default=90, help="minimum gloss length to consider 'long' (default 90)")

    paii = sub.add_parser("import-ai", help="Apply AI JSONL (id→short_gloss_fr,pos_out) into merged CSVs, then suggest review")
    paii.add_argument("--run", help="run timestamp under etl/runs; defaults to latest run")
    paii.add_argument("--ai-jsonl", required=True, help="path to AI output JSONL (fields: id, short_gloss_fr, keep?, pos_out?)")
    paii.add_argument("--out-dir", help="optional output dir (defaults to runs/<run>/ai_imported)")

    args = ap.parse_args()
    if args.cmd == "wizard":
        return wizard(args)
    if args.cmd == "review":
        return cmd_review(args)
    if args.cmd == "apply-review":
        return cmd_apply_review(args)
    if args.cmd == "polish":
        return cmd_polish(args)
    if args.cmd == "prep-ai":
        return cmd_prep_ai(args)
    if args.cmd == "import-ai":
        return cmd_import_ai(args)
    return 0


if __name__ == "__main__":
    sys.exit(main())
