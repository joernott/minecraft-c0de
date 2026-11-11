"""Converts the anchors of the links in the translations of the documentation (docs/<language>, task 14.3).

Translators keep the link targets of the English file, including anchors like `functions.md#changing-values`.
A translated heading has another anchor, so this script replaces every anchor that doesn't exist in the
translated target with the anchor of the heading at the same position in the translation. Anchors that exist
already are kept, so the script can run again after every change. It fails on links whose target or anchor
doesn't exist in English either.

Usage: python scripts/translation_links.py
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
LINK = re.compile(r"(\]\()([^)#\s]*)(#[^)\s]+)?(\))")


def anchor(heading: str) -> str:
    """The anchor GitHub gives a heading: lower case, without punctuation (letters like ä stay), spaces to -."""
    text = re.sub(r"\[([^\]]*)\]\([^)]*\)", r"\1", heading)
    text = text.replace("`", "")
    return re.sub(r"[^\w -]", "", text.strip().lower()).replace(" ", "-")


def headings(path: Path) -> list[str]:
    """The anchors of the headings of a file in their order, with the suffixes of GitHub for repeated anchors."""
    result, seen, code = [], {}, False
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("```"):
            code = not code
        elif not code and re.match(r"#{1,6} ", line):
            base = anchor(line.lstrip("#").strip())
            count = seen.get(base, 0)
            seen[base] = count + 1
            result.append(base if count == 0 else f"{base}-{count}")
    return result


def convert(path: Path, language_root: Path) -> list[str]:
    """Rewrites the anchors of one translated file; returns the problems."""
    relative = path.relative_to(language_root)
    english_file = DOCS / relative
    text = path.read_text(encoding="utf-8")
    problems = []

    def replace(match: re.Match) -> str:
        target, fragment = match.group(2), match.group(3)
        if not fragment or target.startswith(("http:", "https:")):
            return match.group(0)
        translated_target = (path.parent / target).resolve() if target else path
        english_target = (english_file.parent / target).resolve() if target else english_file
        if not translated_target.exists() or not english_target.exists():
            problems.append(f"{path}: {target}{fragment}: the target doesn't exist")
            return match.group(0)
        translated = headings(translated_target)
        if fragment[1:] in translated:
            return match.group(0)
        english = headings(english_target)
        if fragment[1:] not in english:
            problems.append(f"{path}: {target}{fragment}: the anchor doesn't exist in English")
            return match.group(0)
        position = english.index(fragment[1:])
        if position >= len(translated):
            problems.append(f"{path}: {target}{fragment}: the translation has fewer headings")
            return match.group(0)
        return match.group(1) + target + "#" + translated[position] + match.group(4)

    converted = LINK.sub(replace, text)
    if converted != text:
        path.write_text(converted, encoding="utf-8", newline="\n")
    return problems


def main() -> None:
    problems = []
    for language_root in sorted(p for p in DOCS.iterdir() if p.is_dir() and re.fullmatch(r"[a-z]{2}", p.name)):
        for path in sorted(language_root.rglob("*.md")):
            problems += convert(path, language_root)
    for problem in problems:
        print(problem)
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()
