"""Adds links to the guide (docs/guide and docs/de/guide): the first mention of a term in a section links to the
section that explains it. Headings, code, images and existing links stay as they are, and a section doesn't link
to itself. The German links get the English anchors; scripts/translation_links.py then translates them.

Usage: python scripts/guide_links.py [--write] (without --write, it only counts the new links); run
py scripts/translation_links.py afterwards. Links that exist stay; the script adds the missing ones.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'scripts'))
from generate_book import anchor  # noqa: E402

GS, LANG, RUN, TOOLS, LIBS = ('01-getting-started.md', '02-language.md', '03-running-programs.md', '04-tools.md',
                              '05-libraries.md')

# (English pattern, German pattern, target file, English heading of the target, chapters where the term is left out)
# Patterns are regular expressions; lower-case ones ignore case at the first letter only where written so.
TERMS = [
    # Running programs
    (r"trigger dialog", r"Auslöser-Dialog(?:s|e)?", RUN, "The trigger dialog", ()),
    (r"exit codes?", r"Exit-Codes?", RUN, "Exit codes", ()),
    (r"uninitialized (?:variable |constant )?items?", r"nicht initialisierten? (?:Variablen|Konstanten)?[Kk]arten?", RUN, "Uninitialized items", ()),
    (r"PID blocks?", r"PID-Bl(?:ock|öcke)s?", RUN, "PID blocks", ()),
    (r"debug cursors?", r"Debug-Cursors?", RUN, "The cursor", ()),
    (r"(?:laptops?|tablets?)", r"(?:Laptops?|Tablets?)", RUN, "Talking to players", ()),
    (r"display blocks?", r"Anzeigebl(?:ock|öcke)s?", RUN, "Talking to players", ()),
    (r"I/O blocks?", r"E/A-Bl(?:ock|öcke)s?", RUN, "Talking to players", ()),
    (r"(?:debug trigger blocks?|timer blocks?|trigger blocks?)", r"(?:Debug-Auslöserbl(?:ock|öcke)s?|Zeitgeberbl(?:ock|öcke)s?|Auslöserbl(?:ock|öcke)s?)", RUN, "Three trigger blocks", ()),
    # Tools and items
    (r"wrench(?:es)?", r"Schraubenschlüssel[sn]?", TOOLS, "Wrench", ()),
    (r"text markers?", r"Textmarkers?", TOOLS, "Text marker", ()),
    (r"hammer", r"Hammers?", TOOLS, "Hammer", ()),
    (r"pliers", r"Zange", TOOLS, "Pliers", ()),
    (r"boots", r"Stiefel[n]?", TOOLS, "Boots", ()),
    (r"debugging tool", r"Debugging-Werkzeugs?", TOOLS, "Debugging tool", ()),
    (r"c0de disks?", r"c0de-Disketten?", TOOLS, "c0de disk", ()),
    (r"disk drives?", r"Diskettenlaufwerks?", TOOLS, "Disk drive", ()),
    (r"data items?", r"Datenkarten?", TOOLS, "Data item", ()),
    (r"type items?", r"Typkarten?", TOOLS, "Type items", ()),
    (r"variable items?", r"Variablenkarten?", TOOLS, "Variable items", ()),
    (r"constant items?", r"Konstantenkarten?", TOOLS, "Constant items", ()),
    (r"type blocks?", r"Typbl(?:ock|öcke)s?", TOOLS, "Type blocks", ()),
    (r"structural blocks?", r"Strukturbl(?:ock|öcke)s?", TOOLS, "Structural blocks", ()),
    (r"guide book", r"Anleitungsbuchs?", TOOLS, "Guide book", ()),
    (r"handbooks", r"Handbücher[n]?", TOOLS, "Handbooks", ()),
    # Getting started
    (r"line numbers?", r"Zeilennummern?", GS, "Line numbers", ()),
    (r"text notation", r"Textschreibweise", GS, "Reading the examples", ()),
    # The language
    (r"indentation", r"Einrückung", LANG, "Lines and indentation", ()),
    (r"code blocks?", r"Codebl(?:ock|öcke)s?", LANG, "Lines and indentation", ()),
    (r"expressions?", r"Ausdrücke[n]?|Ausdrucks?", LANG, "Expressions", (LIBS,)),
    (r"operators", r"Operatoren", LANG, "Operators", (LIBS,)),
    (r"literals?", r"Literale?", LANG, "Literals", ()),
    (r"structs?", r"Structs?", LANG, "Structs", ()),
    (r"data types?", r"Datentypen?", LANG, "Types", ()),
    (r"NOOP(?: lines?)?", r"NOOP(?:-Zeilen?)?", LANG, "NOOP and COMMENT", ()),
    (r"COMMENT", r"COMMENT", LANG, "NOOP and COMMENT", ()),
    (r"VARIABLE", r"VARIABLE", LANG, "Variables and constants", ()),
    (r"CONSTANT", r"CONSTANT", LANG, "Variables and constants", ()),
    (r"ASSIGN", r"ASSIGN", LANG, "ASSIGN", ()),
    (r"FUNCTION", r"FUNCTION", LANG, "Functions", ()),
    (r"CALL", r"CALL", LANG, "CALL", ()),
    (r"RETURN", r"RETURN", LANG, "RETURN", ()),
    (r"IF|ELSE", r"IF|ELSE", LANG, "IF and ELSE", ()),
    (r"FOR", r"FOR", LANG, "FOR loops", ()),
    (r"EACH", r"EACH", LANG, "EACH loops", ()),
    (r"WHILE|REPEAT", r"WHILE|REPEAT", LANG, "WHILE and REPEAT", ()),
    (r"BREAK", r"BREAK", LANG, "BREAK", ()),
    (r"EXIT", r"EXIT", LANG, "EXIT", ()),
    (r"RAISE", r"RAISE", LANG, "Your own errors", ()),
    # Libraries
    (r"INCLUDE(?: lines?)?", r"INCLUDE(?:-Zeilen?)?", LIBS, "Including libraries", ()),
    (r"built-in functions?", r"eingebauten? Funktionen?", LIBS, "Built-in functions", ()),
    (r"c0de/math", r"c0de/math", LIBS, "c0de/math", ()),
    (r"c0de/strings", r"c0de/strings", LIBS, "c0de/strings", ()),
    (r"c0de/time", r"c0de/time", LIBS, "c0de/time", ()),
    (r"c0de/minecraft", r"c0de/minecraft", LIBS, "c0de/minecraft", ()),
    (r"c0de/security", r"c0de/security", LIBS, "c0de/security", ()),
    (r"FORMAT", r"FORMAT", LIBS, "FORMAT", ()),
    (r"vault", r"Tresors?", LIBS, "Secrets in the vault", ()),
    (r"permissions?", r"Berechtigungen?", LIBS, "Owner and permissions", ()),
]

def anchors(target, heading):
    """The anchors of a section in English and German (the translation has its headings in the same order)."""
    result = {anchor(heading)}
    english = [line[3:].strip() for line in (ROOT / "docs" / "guide" / target).read_text(encoding="utf-8").splitlines()
               if line.startswith("## ")]
    german_file = ROOT / "docs" / "de" / "guide" / target
    if german_file.exists() and heading in english:
        german = [line[3:].strip() for line in german_file.read_text(encoding="utf-8").splitlines()
                  if line.startswith("## ")]
        if len(german) == len(english):
            result.add(anchor(german[english.index(heading)]))
    return result


INLINE = re.compile(r"`[^`]*`|!?\[[^\]]*\]\([^)]*\)")


def sections(lines):
    """The sections of a file: (English-position index, heading or None for the introduction, line numbers)."""
    result = [[None, []]]
    for number, line in enumerate(lines):
        if line.startswith("## "):
            result.append([line[3:].strip(), []])
        elif not line.startswith("# "):
            result[-1][1].append(number)
    return result


def link_file(name, folder, german, english_headings, stats):
    path = folder / name
    lines = path.read_text(encoding="utf-8").split("\n")
    compiled = []
    for en, de, target, heading, skip in TERMS:
        term = de if german else en
        # Terms in lower case also match at the start of a sentence
        flags = re.IGNORECASE if next(c for c in term if c.isalpha()).islower() else 0
        compiled.append((re.compile(r"(?<![\w/\-])(?:" + term + r")(?![\w/\-])", flags), target, heading, skip))
    for index, (heading, numbers) in enumerate(sections(lines)):
        own = english_headings[index - 1] if index > 0 else None
        linked = set()
        # Targets the section links to already, by their file and anchor
        existing = set()
        labels = []
        for number in numbers:
            for target in re.findall(r"\]\(([^)]*\.md)?#([^)]*)\)", lines[number]):
                existing.add(((target[0] or name), target[1]))
            labels.extend(re.findall(r"(?<!!)\[([^\]]*)\]\(", lines[number]))
        code = False
        for number in numbers:
            line = lines[number]
            if line.startswith("```"):
                code = not code
                continue
            if code or not line.strip() or line.startswith("!["):
                continue
            # The parts of the line outside code and links
            parts = []
            position = 0
            for match in INLINE.finditer(line):
                parts.append((position, match.start(), True))
                parts.append((match.start(), match.end(), False))
                position = match.end()
            parts.append((position, len(line), True))
            out = []
            for start, end, free in parts:
                text = line[start:end]
                if not free:
                    out.append(text)
                    continue
                spans = []
                for pattern, target, target_heading, skip in compiled:
                    key = (target, target_heading)
                    if (name in skip or key in linked or (target == name and target_heading == own)
                            or any((target, a) in existing for a in anchors(target, target_heading))
                            or any(pattern.fullmatch(label) for label in labels)):
                        continue
                    for match in pattern.finditer(text):
                        if any(s < match.end() and match.start() < e for s, e, *_ in spans):
                            continue
                        spans.append((match.start(), match.end(), target, target_heading))
                        linked.add(key)
                        break
                spans.sort()
                result = []
                cursor = 0
                for s, e, target, target_heading in spans:
                    link = ("" if target == name else target) + "#" + anchor(target_heading)
                    result.append(text[cursor:s] + "[" + text[s:e] + "](" + link + ")")
                    cursor = e
                    stats[name] = stats.get(name, 0) + 1
                result.append(text[cursor:])
                out.append("".join(result))
            lines[number] = "".join(out)
    return "\n".join(lines)


def main():
    write = "--write" in sys.argv
    english = ROOT / "docs" / "guide"
    for german, folder in ((False, english), (True, ROOT / "docs" / "de" / "guide")):
        stats = {}
        for path in sorted(english.glob("[0-9][0-9]-*.md")):
            headings = [line[3:].strip() for line in path.read_text(encoding="utf-8").split("\n")
                        if line.startswith("## ")]
            text = link_file(path.name, folder, german, headings, stats)
            if write:
                (folder / path.name).write_text(text, encoding="utf-8", newline="\n")
        print("de" if german else "en", stats, sum(stats.values()))


main()
