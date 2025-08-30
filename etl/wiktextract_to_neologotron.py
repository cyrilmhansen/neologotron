#!/usr/bin/env python3
"""
ETL: wiktextract (frwiktionary) JSONL → Neologotron seed CSVs

- Parses wiktextract JSONL (one JSON per line) and extracts prefixes, roots and suffixes
  with compact glosses and richer etymology metadata.
- Produces three CSVs in the target directory:
    * neologotron_prefixes.csv
    * neologotron_racines.csv
    * neologotron_suffixes.csv

Designed to run offline. If you want to attach Wikidata QIDs, provide a local mapping file
via --wikidata-map.

Wiktionary licensing: CC BY-SA. Ensure attribution wherever content is shown.
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
from dataclasses import dataclass, asdict
from typing import Dict, Iterable, List, Optional, Set, Tuple


# ---------------------------
# Output schemas / CSV headers
# ---------------------------

PREFIX_HEADERS = [
    # App seed columns (current)
    "id", "form", "alt_forms", "gloss", "origin", "connector", "phon_rules", "tags", "weight",
    # Extended, optional columns (safe to ignore in app)
    "ety_lang", "ety_desc", "ety_lineage", "proto_form", "ipa", "attest_from", "cognates", "sources", "examples",
]

SUFFIX_HEADERS = [
    # App seed columns (current)
    "id", "form", "alt_forms", "gloss", "origin", "pos_out", "def_template", "tags", "weight",
    # Extended
    "ety_lang", "ety_desc", "ety_lineage", "proto_form", "ipa", "attest_from", "cognates", "sources", "examples",
]

ROOT_HEADERS = [
    # App seed columns (current)
    "id", "form", "alt_forms", "gloss", "origin", "domain", "connector_pref", "examples", "weight",
    # Extended
    "root_lang", "proto_root", "ety_desc", "ety_lineage", "semantic_field", "sources",
]


# ---------------------------
# Helpers: normalization
# ---------------------------

LANG_MAP = {
    # Wiktextract -> ISO 639-like code
    "French": "fr",
    "Translingual": "mul",
    "Ancient Greek": "grc",
    "Greek": "el",
    "Latin": "la",
    "English": "en",
    # Localized labels frequently seen in some dumps
    "Français": "fr",
    "Translingue": "mul",
}

ORIGIN_FR = {
    # Code -> French label used in current seeds
    "grc": "grec",
    "la": "latin",
    "el": "grec (mod.)",
    "fr": "français",
    "mul": "translingue",
    "en": "anglais",
}


def norm_lang(label: Optional[str]) -> Optional[str]:
    if not label:
        return None
    # Already a code
    if label in {"fr", "mul", "grc", "la", "el", "en"}:
        return label
    return LANG_MAP.get(label, label)


def origin_fr_label(code: Optional[str]) -> Optional[str]:
    if not code:
        return None
    return ORIGIN_FR.get(code, code)


def make_id(prefix: str, form: str, used: Set[str]) -> str:
    base = re.sub(r"[^a-z0-9]+", "_", form.lower().strip("- ")
                  .replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e")
                  .replace("ï", "i").replace("î", "i").replace("ô", "o").replace("û", "u").replace("ù", "u"))
    base = base.strip("_")
    if not base:
        base = "x"
    candidate = f"{prefix}_{base}"
    i = 2
    while candidate in used:
        candidate = f"{prefix}_{base}{i}"
        i += 1
    used.add(candidate)
    return candidate


def join_unique(parts: Iterable[str], sep: str = ", ") -> str:
    seen = []
    sset = set()
    for p in parts:
        q = p.strip()
        if q and q not in sset:
            seen.append(q)
            sset.add(q)
    return sep.join(seen)


def first_ipa(entry: dict) -> Optional[str]:
    for snd in entry.get("sounds", []) or []:
        ipa = snd.get("ipa")
        if ipa:
            return ipa
    return None


def ety_text(entry: dict) -> Optional[str]:
    # wiktextract may put etymology in different fields
    txt = entry.get("etymology_text")
    if txt:
        return clean_wiki_markup(txt)
    # Some dumps have a list of etymology texts
    lst = entry.get("etymology_texts")
    if isinstance(lst, list) and lst:
        joined = "; ".join([str(x) for x in lst if x])
        return clean_wiki_markup(joined)
    return None


def ety_lineage_from_templates(entry: dict) -> Optional[str]:
    # Try to build a compact lineage like "fr < la < grc" from etymology_templates
    chain: List[str] = []
    for tpl in entry.get("etymology_templates", []) or []:
        name = tpl.get("name")
        args = tpl.get("args", {})
        # Templates of type {{bor|fr|la|...}} or {{der|fr|grc|...}}
        if name in {"bor", "der", "inh"}:  # borrowed/derived/inherited
            lang_code = args.get("2") or args.get("1")
            if lang_code:
                chain.append(lang_code)
    if chain:
        # Ensure target language at front if missing
        head = norm_lang(entry.get("lang")) or "fr"
        if not chain or chain[0] != head:
            chain.insert(0, head)
        # Normalize a few common codes to our preferred ones
        pretty = [origin_fr_label(c) if len(c) <= 3 else c for c in chain]
        return " < ".join(pretty)
    return None


def clean_wiki_markup(s: str) -> str:
    # Very light cleanup for display/storage; keep it simple and robust
    # Remove wiki links [[...|...]] / [[...]]
    s = re.sub(r"\[\[([^\]|]+)\|([^\]]+)\]\]", r"\2", s)
    s = re.sub(r"\[\[([^\]]+)\]\]", r"\1", s)
    # Remove templates {{...}}
    s = re.sub(r"\{\{[^}]+\}\}", "", s)
    # Collapse spaces
    s = re.sub(r"\s+", " ", s).strip()
    return s


def sense_gloss(entry: dict) -> Optional[str]:
    glosses: List[str] = []
    for s in entry.get("senses", []) or []:
        gs = s.get("glosses") or []
        if gs:
            glosses.extend([g for g in gs if isinstance(g, str)])
    g = join_unique(glosses, "; ")
    return g or None


def topics_tags(entry: dict) -> List[str]:
    tags = []
    for s in entry.get("senses", []) or []:
        # Collect topics and raw tags; normalize lightly
        topics = s.get("topics") or []
        raw = s.get("tags") or []
        tags.extend(topics)
        tags.extend(raw)
    # Light French labelling for common topics
    repl = {
        "biology": "science",
        "chemistry": "science",
        "medicine": "médecine",
        "technology": "tech",
        "phonetics": "langage",
        "linguistics": "langage",
        "politics": "politique",
        "sociology": "société",
        "astronomy": "cosmos",
    }
    out = []
    for t in tags:
        t = t.lower().replace("_", " ")
        out.append(repl.get(t, t))
    return [t for t in sorted(set(out)) if t]


def derived_examples(entry: dict) -> List[str]:
    # Heuristic: use derived/compounds/links if present
    vals: List[str] = []
    for k in ("derived", "compounds", "links"):
        for it in entry.get(k, []) or []:
            if isinstance(it, dict):
                w = it.get("word")
                if w:
                    vals.append(w)
            elif isinstance(it, str):
                vals.append(it)
    return list(dict.fromkeys(vals))[:8]


def _norm_pos(entry: dict) -> str:
    # Prefer canonical 'pos', fallback to localized 'pos_title'
    raw = (entry.get("pos") or entry.get("pos_title") or "").lower()
    # Remove accents lightly for matching
    try:
        import unicodedata
        raw = ''.join(c for c in unicodedata.normalize('NFD', raw) if unicodedata.category(c) != 'Mn')
    except Exception:
        pass
    return raw


def is_affix(entry: dict) -> bool:
    pos = _norm_pos(entry)
    return any(k in pos for k in (
        "prefix",  # en
        "suffix",  # en
        "affix",   # en/fr (affixe)
        "combining form",  # en
        "interfix", "infix",
        "prefixe", "suffixe", "affixe",  # fr (normalized accents removed)
        "confix", "element de composition", "element formant",
    ))


def is_prefix(entry: dict) -> bool:
    pos = _norm_pos(entry)
    return "prefix" in pos


def is_suffix(entry: dict) -> bool:
    pos = _norm_pos(entry)
    return "suffix" in pos


def is_combining_root(entry: dict) -> bool:
    # Treat combining forms or affixes in Translingual/Greek/Latin as roots
    pos = _norm_pos(entry)
    # Use code if available for robust matching
    lang = norm_lang(entry.get("lang_code") or entry.get("lang"))
    return pos in {"combining form", "affix"} and (lang in {"grc", "la", "mul"})


# ---------------------------
# Row models
# ---------------------------

@dataclass
class PrefixRow:
    id: str
    form: str
    alt_forms: Optional[str] = None
    gloss: Optional[str] = None
    origin: Optional[str] = None
    connector: Optional[str] = None
    phon_rules: Optional[str] = None
    tags: Optional[str] = None
    weight: Optional[float] = None
    ety_lang: Optional[str] = None
    ety_desc: Optional[str] = None
    ety_lineage: Optional[str] = None
    proto_form: Optional[str] = None
    ipa: Optional[str] = None
    attest_from: Optional[str] = None
    cognates: Optional[str] = None
    sources: Optional[str] = None
    examples: Optional[str] = None


@dataclass
class SuffixRow:
    id: str
    form: str
    alt_forms: Optional[str] = None
    gloss: Optional[str] = None
    origin: Optional[str] = None
    pos_out: Optional[str] = None
    def_template: Optional[str] = None
    tags: Optional[str] = None
    weight: Optional[float] = None
    ety_lang: Optional[str] = None
    ety_desc: Optional[str] = None
    ety_lineage: Optional[str] = None
    proto_form: Optional[str] = None
    ipa: Optional[str] = None
    attest_from: Optional[str] = None
    cognates: Optional[str] = None
    sources: Optional[str] = None
    examples: Optional[str] = None


@dataclass
class RootRow:
    id: str
    form: str
    alt_forms: Optional[str] = None
    gloss: Optional[str] = None
    origin: Optional[str] = None
    domain: Optional[str] = None
    connector_pref: Optional[str] = None
    examples: Optional[str] = None
    weight: Optional[float] = None
    root_lang: Optional[str] = None
    proto_root: Optional[str] = None
    ety_desc: Optional[str] = None
    ety_lineage: Optional[str] = None
    semantic_field: Optional[str] = None
    sources: Optional[str] = None


# ---------------------------
# Core ETL
# ---------------------------

def load_wikidata_map(path: Optional[str]) -> Dict[str, str]:
    if not path:
        return {}
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def extract_rows(
    entries: Iterable[dict],
    lang_filter: Set[str],
    include_translingual: bool,
    cap_prefix: Optional[int] = None,
    cap_root: Optional[int] = None,
    cap_suffix: Optional[int] = None,
    roots_from_translingual: bool = False,
) -> Tuple[List[PrefixRow], List[RootRow], List[SuffixRow]]:
    prefixes: List[PrefixRow] = []
    roots: List[RootRow] = []
    suffixes: List[SuffixRow] = []
    used_ids: Set[str] = set()

    def _is_classical_from_text(lineage: Optional[str], desc: Optional[str]) -> bool:
        txt = f"{lineage or ''} {desc or ''}".lower()
        keys = ("grec", "latin", "grc", "la", "ancient greek", "classical latin")
        return any(k in txt for k in keys)

    for e in entries:
        # Prefer lang_code if present
        lang = norm_lang(e.get("lang_code") or e.get("lang"))
        if lang not in lang_filter and not (include_translingual and lang == "mul"):
            continue
        if not is_affix(e):
            continue
        word = (e.get("word") or e.get("title") or "").strip()
        if not word:
            continue
        alt_forms = []
        for fm in e.get("forms", []) or []:
            v = fm.get("form")
            if v and v != word:
                alt_forms.append(v)
        gloss = sense_gloss(e)
        tags = topics_tags(e)
        exs = derived_examples(e)
        ipa = first_ipa(e)
        ety_desc = ety_text(e)
        ety_lineage = ety_lineage_from_templates(e)
        origin = origin_fr_label(lang)
        src = f"wiktionary:fr:{e.get('pageid', '') or ''}:{e.get('word', '')}#{e.get('pos', '')}"

        if is_prefix(e):
            row = PrefixRow(
                id=make_id("pre", word, used_ids),
                form=word,
                alt_forms=join_unique(alt_forms) or None,
                gloss=gloss,
                origin=origin,
                connector=("o" if any(k in word for k in ("o-", "-o-")) else None),
                phon_rules=None,
                tags=",".join(tags) or None,
                weight=1.0,
                ety_lang=lang,
                ety_desc=ety_desc,
                ety_lineage=ety_lineage,
                proto_form=None,
                ipa=ipa,
                attest_from=None,
                cognates=None,
                sources=src,
                examples=join_unique(exs, "; ") or None,
            )
            prefixes.append(row)
            # Optionally also treat translingual classical prefixes as roots
            if roots_from_translingual and lang == "mul" and _is_classical_from_text(ety_lineage, ety_desc):
                # Attempt to infer a classical origin code from lineage text
                origin_code = None
                if ety_lineage:
                    low = ety_lineage.lower()
                    if "grec" in low or "grc" in low:
                        origin_code = "grc"
                    elif "latin" in low or "la" in low:
                        origin_code = "la"
                rrow = RootRow(
                    id=make_id("root", word, used_ids),
                    form=word,
                    alt_forms=join_unique(alt_forms) or None,
                    gloss=gloss,
                    origin=origin_fr_label(origin_code) if origin_code else origin_fr_label(lang),
                    domain=",".join(tags) or None,
                    connector_pref=("o" if any(k in word for k in ("o-", "-o-")) else None),
                    examples=join_unique(exs, "; ") or None,
                    weight=1.0,
                    root_lang=origin_code or lang,
                    proto_root=None,
                    ety_desc=ety_desc,
                    ety_lineage=ety_lineage,
                    semantic_field=",".join(tags) or None,
                    sources=src,
                )
                roots.append(rrow)
        elif is_suffix(e):
            row = SuffixRow(
                id=make_id("suf", word, used_ids),
                form=word,
                alt_forms=join_unique(alt_forms) or None,
                gloss=gloss,
                origin=origin,
                pos_out=None,
                def_template=None,
                tags=",".join(tags) or None,
                weight=1.0,
                ety_lang=lang,
                ety_desc=ety_desc,
                ety_lineage=ety_lineage,
                proto_form=None,
                ipa=ipa,
                attest_from=None,
                cognates=None,
                sources=src,
                examples=join_unique(exs, "; ") or None,
            )
            suffixes.append(row)
            # Optionally also treat translingual classical suffixes as roots
            if roots_from_translingual and lang == "mul" and _is_classical_from_text(ety_lineage, ety_desc):
                origin_code = None
                if ety_lineage:
                    low = ety_lineage.lower()
                    if "grec" in low or "grc" in low:
                        origin_code = "grc"
                    elif "latin" in low or "la" in low:
                        origin_code = "la"
                rrow = RootRow(
                    id=make_id("root", word, used_ids),
                    form=word,
                    alt_forms=join_unique(alt_forms) or None,
                    gloss=gloss,
                    origin=origin_fr_label(origin_code) if origin_code else origin_fr_label(lang),
                    domain=",".join(tags) or None,
                    connector_pref=("o" if any(k in word for k in ("o-", "-o-")) else None),
                    examples=join_unique(exs, "; ") or None,
                    weight=1.0,
                    root_lang=origin_code or lang,
                    proto_root=None,
                    ety_desc=ety_desc,
                    ety_lineage=ety_lineage,
                    semantic_field=",".join(tags) or None,
                    sources=src,
                )
                roots.append(rrow)
        elif is_combining_root(e):
            row = RootRow(
                id=make_id("root", word, used_ids),
                form=word,
                alt_forms=join_unique(alt_forms) or None,
                gloss=gloss,
                origin=origin,
                domain=",".join(tags) or None,
                connector_pref=("o" if any(k in word for k in ("o-", "-o-")) else None),
                examples=join_unique(exs, "; ") or None,
                weight=1.0,
                root_lang=lang,
                proto_root=None,
                ety_desc=ety_desc,
                ety_lineage=ety_lineage,
                semantic_field=",".join(tags) or None,
                sources=src,
            )
            roots.append(row)

        # Early stop when caps reached (for quicker sampling on large dumps)
        done_prefix = cap_prefix is not None and len(prefixes) >= cap_prefix
        done_root = cap_root is not None and len(roots) >= cap_root
        done_suffix = cap_suffix is not None and len(suffixes) >= cap_suffix
        # If any cap is specified, and all specified caps are met, break
        caps_spec = [cap_prefix is not None, cap_root is not None, cap_suffix is not None]
        if any(caps_spec):
            ok_prefix = (not caps_spec[0]) or done_prefix
            ok_root = (not caps_spec[1]) or done_root
            ok_suffix = (not caps_spec[2]) or done_suffix
            if ok_prefix and ok_root and ok_suffix:
                break

    return prefixes, roots, suffixes


def read_jsonl(path: str, limit_lines: Optional[int] = None, skip_lines: Optional[int] = None) -> Iterable[dict]:
    """Stream JSONL with optional skip and cap. Supports .gz files."""
    import gzip
    opener = gzip.open if path.endswith(".gz") else open
    with opener(path, "rt", encoding="utf-8") as f:
        for i, line in enumerate(f, 1):
            if skip_lines is not None and i <= skip_lines:
                continue
            if limit_lines is not None and i > limit_lines:
                break
            line = line.strip()
            if not line:
                continue
            try:
                yield json.loads(line)
            except Exception as ex:
                print(f"[WARN] Skipping malformed JSON at line {i}: {ex}", file=sys.stderr)


def write_csv(path: str, headers: List[str], rows: List[dataclass]) -> None:
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=headers)
        w.writeheader()
        for row in rows:
            rec = asdict(row)
            # Ensure all headers exist; extra keys are ignored
            w.writerow({h: rec.get(h) for h in headers})


def main() -> int:
    ap = argparse.ArgumentParser(description="wiktextract JSONL → Neologotron CSVs")
    ap.add_argument("--input", required=True, help="wiktextract JSONL file (frwiktionary)")
    ap.add_argument("--out-dir", required=True, help="output directory for CSVs")
    ap.add_argument("--lang", default="fr", help="target language code (default: fr)")
    ap.add_argument("--include-translingual", action="store_true", help="also include Translingual entries")
    ap.add_argument("--wikidata-map", help="optional local JSON mapping { '<lemma>#<lang>': 'QID' }")
    ap.add_argument("--limit-lines", type=int, help="read at most this many JSONL lines")
    ap.add_argument("--skip-lines", type=int, help="skip this many lines first (coarse paging)")
    ap.add_argument("--match", help="regex to filter entry forms (word/title)")
    ap.add_argument("--limit-prefix", type=int, help="limit number of prefix rows")
    ap.add_argument("--limit-root", type=int, help="limit number of root rows")
    ap.add_argument("--limit-suffix", type=int, help="limit number of suffix rows")
    ap.add_argument("--debug", action="store_true", help="print a filtering report to stderr")
    ap.add_argument("--debug-samples", type=int, default=8, help="number of sample entries to print in debug report")
    ap.add_argument(
        "--origin-filter",
        choices=["classical", "none"],
        default="classical",
        help="filter outputs by likely classical (Greek/Latin) origin (default: classical). Use 'none' to keep all."
    )
    ap.add_argument("--roots-from-translingual", action="store_true", help="treat Translingual classical prefixes/suffixes as roots as well")
    args = ap.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)
    lang_filter = {args.lang}

    entries_iter = read_jsonl(args.input, limit_lines=args.limit_lines, skip_lines=args.skip_lines)

    # Optional early filter by regex on the word/title
    if args.match:
        try:
            rx = re.compile(args.match)
        except re.error as ex:
            print(f"[ERROR] Invalid --match regex: {ex}", file=sys.stderr)
            return 2

        def _filtered():
            for e in entries_iter:
                w = (e.get("word") or e.get("title") or "")
                if rx.search(w or ""):
                    yield e

        entries = _filtered()
    else:
        entries = entries_iter

    # Optional debug scan to understand filtering
    if args.debug:
        from collections import Counter
        c_total = 0
        c_rx = 0
        c_lang_ok = 0
        c_affix = 0
        c_pref = 0
        c_suf = 0
        c_root = 0
        seen_langs = Counter()
        seen_pos = Counter()
        samples = []

        # Recreate the iterator with the same limits for scanning
        scan_iter = read_jsonl(args.input, limit_lines=args.limit_lines, skip_lines=args.skip_lines)
        if args.match:
            def _scan_filtered():
                for e in scan_iter:
                    w = (e.get("word") or e.get("title") or "")
                    if rx.search(w or ""):
                        yield e
            scan_iter = _scan_filtered()

        for e in scan_iter:
            c_total += 1
            w = (e.get("word") or e.get("title") or "")
            if not w:
                continue
            if args.match and rx.search(w or ""):
                c_rx += 1
            lang = norm_lang(e.get("lang_code") or e.get("lang"))
            seen_langs[lang or "?"] += 1
            pos = _norm_pos(e)
            seen_pos[pos or "?"] += 1
            if lang in lang_filter or (args.include_translingual and lang == "mul"):
                c_lang_ok += 1
                if is_affix(e):
                    c_affix += 1
                    # Classify
                    if is_prefix(e):
                        c_pref += 1
                        if len(samples) < args.debug_samples:
                            samples.append({"type": "prefix", "word": w, "pos": pos, "lang": lang})
                    elif is_suffix(e):
                        c_suf += 1
                        if len(samples) < args.debug_samples:
                            samples.append({"type": "suffix", "word": w, "pos": pos, "lang": lang})
                    elif is_combining_root(e):
                        c_root += 1
                        if len(samples) < args.debug_samples:
                            samples.append({"type": "root", "word": w, "pos": pos, "lang": lang})

        def _top(counter: Counter, n=8):
            return ", ".join([f"{k}:{v}" for k, v in counter.most_common(n)])

        print("[DEBUG] Scan report:", file=sys.stderr)
        print(f"  total_scanned={c_total}", file=sys.stderr)
        if args.match:
            print(f"  regex_match_lines≈{c_rx}", file=sys.stderr)
        print(f"  lang_ok={c_lang_ok}  affix_pos={c_affix}  prefix={c_pref}  suffix={c_suf}  root={c_root}", file=sys.stderr)
        print(f"  langs_seen: {_top(seen_langs)}", file=sys.stderr)
        print(f"  pos_seen: {_top(seen_pos)}", file=sys.stderr)
        if samples:
            print(f"  samples ({len(samples)}):", file=sys.stderr)
            for s in samples:
                print(f"    - {s['type']}: {s['word']}  lang={s['lang']}  pos={s['pos']}", file=sys.stderr)

        # Rebuild the entry iterator for extraction after scan
        entries_iter = read_jsonl(args.input, limit_lines=args.limit_lines, skip_lines=args.skip_lines)
        if args.match:
            def _filtered2():
                for e in entries_iter:
                    w = (e.get("word") or e.get("title") or "")
                    if rx.search(w or ""):
                        yield e
            entries = _filtered2()
        else:
            entries = entries_iter

    prefixes, roots, suffixes = extract_rows(
        entries,
        lang_filter,
        args.include_translingual,
        cap_prefix=args.limit_prefix,
        cap_root=args.limit_root,
        cap_suffix=args.limit_suffix,
        roots_from_translingual=args.roots_from_translingual,
    )

    # Light post-filters: keep only affixes/roots that look Greek/Latin for initial dataset
    # Apply optional origin filter
    pre_counts = (len(prefixes), len(roots), len(suffixes))
    if args.origin_filter != "none":
        def likely_classical(origin: Optional[str], lineage: Optional[str]) -> bool:
            # Accept if origin or lineage mentions Greek/Latin; else drop
            keys = ("grec", "latin", "grc", "la")
            test = " ".join([str(origin or ""), str(lineage or "")]).lower()
            return any(k in test for k in keys)

        prefixes = [r for r in prefixes if likely_classical(r.origin, r.ety_lineage)]
        suffixes = [r for r in suffixes if likely_classical(r.origin, r.ety_lineage)]
        roots = [r for r in roots if likely_classical(r.origin, r.ety_lineage)]
    post_counts = (len(prefixes), len(roots), len(suffixes))
    if args.debug:
        print(
            f"[DEBUG] Origin filter '{args.origin_filter}': prefixes {pre_counts[0]}→{post_counts[0]}, roots {pre_counts[1]}→{post_counts[1]}, suffixes {pre_counts[2]}→{post_counts[2]}",
            file=sys.stderr,
        )

    out_prefix = os.path.join(args.out_dir, "neologotron_prefixes.csv")
    out_suffix = os.path.join(args.out_dir, "neologotron_suffixes.csv")
    out_root = os.path.join(args.out_dir, "neologotron_racines.csv")

    # Optional caps on number of rows per type for quick testing
    if args.limit_prefix is not None:
        prefixes = prefixes[: max(0, args.limit_prefix)]
    if args.limit_suffix is not None:
        suffixes = suffixes[: max(0, args.limit_suffix)]
    if args.limit_root is not None:
        roots = roots[: max(0, args.limit_root)]

    write_csv(out_prefix, PREFIX_HEADERS, prefixes)
    write_csv(out_suffix, SUFFIX_HEADERS, suffixes)
    write_csv(out_root, ROOT_HEADERS, roots)

    print(f"Wrote: {out_prefix} ({len(prefixes)})")
    print(f"Wrote: {out_suffix} ({len(suffixes)})")
    print(f"Wrote: {out_root} ({len(roots)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
