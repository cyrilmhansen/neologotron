import hashlib
from pathlib import Path
import sys

sys.path.append(str(Path(__file__).resolve().parents[2]))

from etl.cli import _ai_fetch


class DummyArgs:
    model = "test"
    endpoint = "http://example"
    temperature = 0.0
    max_tokens = 1
    timeout = 1.0


def test_ai_fetch_uses_cache(tmp_path: Path):
    calls = []

    def fake_request(endpoint: str, payload: dict, timeout: float):
        calls.append(payload["prompt"])
        yield "{\"id\": \"1\"}"

    args = DummyArgs()
    prompt = "hello"
    cache_dir = tmp_path
    first = _ai_fetch(prompt, args, cache_dir, request_fn=fake_request)
    second = _ai_fetch(prompt, args, cache_dir, request_fn=fake_request)
    assert first == "{\"id\": \"1\"}"
    assert second == "{\"id\": \"1\"}"
    assert len(calls) == 1
    key = hashlib.sha256(prompt.encode("utf-8")).hexdigest()
    assert (cache_dir / f"{key}.txt").exists()


def test_ai_fetch_misses_cache(tmp_path: Path):
    calls = []

    def fake_request(endpoint: str, payload: dict, timeout: float):
        calls.append(payload["prompt"])
        yield "ok"

    args = DummyArgs()
    cache_dir = tmp_path
    _ai_fetch("a", args, cache_dir, request_fn=fake_request)
    _ai_fetch("b", args, cache_dir, request_fn=fake_request)
    assert calls == ["a", "b"]

