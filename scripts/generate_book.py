"""Generates the Patchouli guide book of the mod from the player guide in docs/guide.

Every file docs/guide/NN-name.md becomes a category, every "## " section an entry. The paragraphs of a section
are distributed over text pages; program examples get pages of their own. The book itself, its recipe (only
loaded if Patchouli is installed) and the pages are generated files; generate_content.py calls this script.

Usage: python scripts/generate_book.py
"""

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GUIDE = ROOT / "docs" / "guide"
RESOURCES = ROOT / "src" / "main" / "resources"
BOOK = "guide"
CONTENT = RESOURCES / "assets" / "c0de" / "patchouli_books" / BOOK / "en_us"

# The number of characters that fit on a text page; the first page of an entry also shows its title
PAGE_CHARACTERS = 320
FIRST_PAGE_CHARACTERS = 260
# The number of lines of a program on one page; longer programs continue on the next page
CODE_LINES = 12

# The icons of the categories, by the file name without its number
ICONS = {
    "getting-started": "c0de:wrench",
    "language": "c0de:program_block",
    "running-programs": "c0de:trigger_block",
    "tools": "c0de:hammer",
    "libraries": "c0de:include_block",
    "examples": "c0de:function_block",
}


def write(path: Path, content: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def inline(text: str) -> str:
    """Markdown bold and code in the formatting codes of Patchouli."""
    text = re.sub(r"\*\*(.+?)\*\*", r"$(l)\1$()", text)
    return re.sub(r"`(.+?)`", r"$(1)\1$()", text)


def blocks(lines: list[str]) -> list[tuple[str, str]]:
    """The blocks of a section: ("text", paragraph), ("list", items joined) or ("code", program)."""
    result = []
    paragraph: list[str] = []
    code: list[str] | None = None

    def flush() -> None:
        if paragraph:
            items = [line for line in paragraph if line.startswith("- ")]
            if items and len(items) == len(paragraph):
                result.append(("list", "".join("$(li)" + inline(item[2:].strip()) for item in items)))
            else:
                text = " ".join(line.strip() for line in paragraph)
                result.append(("text", inline(text)))
            paragraph.clear()

    for line in lines:
        if code is not None:
            if line.startswith("```"):
                result.append(("code", "\n".join(code)))
                code = None
            else:
                code.append(line.rstrip())
        elif line.startswith("```"):
            flush()
            code = []
        elif not line.strip():
            flush()
        else:
            paragraph.append(line)
    flush()
    return result


def code_pages(program: str) -> list[dict]:
    """Program lines, the indentation kept with non-breaking spaces (two per block, to save width)."""
    lines = []
    for line in program.split("\n"):
        stripped = line.lstrip(" ")
        depth = (len(line) - len(stripped)) // 4
        lines.append("  " * depth + stripped)
    pages = []
    for start in range(0, len(lines), CODE_LINES):
        pages.append({"type": "patchouli:text", "text": "$(1)" + "$(br)".join(lines[start:start + CODE_LINES]) + "$()"})
    return pages


def pages(section: list[str]) -> list[dict]:
    result = []
    text = ""
    limit = FIRST_PAGE_CHARACTERS
    for kind, content in blocks(section):
        if kind == "code":
            if text:
                result.append({"type": "patchouli:text", "text": text})
                text = ""
            result.extend(code_pages(content))
            limit = PAGE_CHARACTERS
            continue
        if text and len(text) + len(content) + 6 > limit:
            result.append({"type": "patchouli:text", "text": text})
            text = ""
            limit = PAGE_CHARACTERS
        text = content if not text else text + "$(br2)" + content
    if text:
        result.append({"type": "patchouli:text", "text": text})
    return result


def main() -> None:
    # The pages are generated anew, so that pages of removed sections don't stay
    if CONTENT.exists():
        shutil.rmtree(CONTENT)
    write(RESOURCES / "data" / "c0de" / "patchouli_books" / BOOK / "book.json", {
        "name": "c0de Guide",
        "landing_text": "Programs built from blocks, written on the greenbar paper of the c0de dimension and run by"
                        " trigger blocks. This guide shows how to write and run them.",
        "version": 1,
        "use_resource_pack": True,
        "model": "patchouli:book_red",
        "book_texture": "patchouli:textures/gui/book_red.png",
        "creative_tab": "c0de:c0de",
        "show_progress": False,
    })
    # The book is crafted from a book and a PROGRAM block, but only if Patchouli is installed
    write(RESOURCES / "data" / "c0de" / "recipe" / "guide_book.json", {
        "fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["patchouli"]}],
        "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "patchouli"}],
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": "minecraft:book"}, {"item": "c0de:program_block"}],
        "result": {"id": "patchouli:guide_book", "count": 1, "components": {"patchouli:book": "c0de:" + BOOK}},
    })
    for number, path in enumerate(sorted(GUIDE.glob("[0-9][0-9]-*.md"))):
        name = path.stem[3:]
        lines = path.read_text(encoding="utf-8").splitlines()
        title = next(line[2:].strip() for line in lines if line.startswith("# "))
        intro: list[str] = []
        sections: list[tuple[str, list[str]]] = []
        for line in lines:
            if line.startswith("# "):
                continue
            if line.startswith("## "):
                sections.append((line[3:].strip(), []))
            elif sections:
                sections[-1][1].append(line)
            else:
                intro.append(line)
        category = slug(name)
        description = " ".join(kind_text for kind, kind_text in blocks(intro) if kind == "text")
        write(CONTENT / "categories" / (category + ".json"), {
            "name": title,
            "description": description,
            "icon": ICONS.get(name, "c0de:noop_block"),
            "sortnum": number,
        })
        for index, (heading, section) in enumerate(sections):
            write(CONTENT / "entries" / category / (slug(heading) + ".json"), {
                "name": heading,
                "icon": ICONS.get(name, "c0de:noop_block"),
                "category": "c0de:" + category,
                "sortnum": index,
                "pages": pages(section),
            })


if __name__ == "__main__":
    main()
