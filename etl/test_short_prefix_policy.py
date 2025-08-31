import csv
import json
import tempfile
from pathlib import Path
import unittest

from etl import cli


class ShortPrefixPolicyTest(unittest.TestCase):
    def test_deny_list_excludes_prefix(self):
        policy = {"allow": [], "deny": ["bi"]}
        with tempfile.TemporaryDirectory() as tmp:
            csv_dir = Path(tmp)
            prefixes = csv_dir / "neologotron_prefixes.csv"
            rows = [
                {"id": "1", "form": "bi-", "alt_forms": "", "gloss": "", "origin": "", "connector": "", "phon_rules": "", "tags": "", "weight": ""},
                {"id": "2", "form": "anti-", "alt_forms": "", "gloss": "", "origin": "", "connector": "", "phon_rules": "", "tags": "", "weight": ""},
            ]
            with open(prefixes, "w", encoding="utf-8", newline="") as f:
                writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
                writer.writeheader()
                writer.writerows(rows)
            cli._apply_short_prefix_policy(csv_dir, policy)
            with open(prefixes, "r", encoding="utf-8", newline="") as f:
                remaining = list(csv.DictReader(f))
            forms = [r["form"] for r in remaining]
            self.assertNotIn("bi-", forms)
            self.assertIn("anti-", forms)


if __name__ == "__main__":
    unittest.main()
