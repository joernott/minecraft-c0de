"""Generates the Patchouli books of the mod from the documentation: the guide (docs/guide) and the handbooks
(docs/reference), and the index of the handbooks (docs/reference/index.md).

Guide: every file docs/guide/NN-name.md becomes a category, every "## " section an entry. Handbooks: every file
docs/reference/<book>.md becomes a book, every "## " section a category and every "### " element an entry with
the same parts: a description, the recipes (crafting pages; for functions, the parameters and results) and an
example. The paragraphs are distributed over text pages; program examples get pages of their own. The books,
their recipes (only loaded if Patchouli is installed) and the pages are generated files; generate_content.py
calls this script.

Usage: python scripts/generate_book.py
"""

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GUIDE = ROOT / "docs" / "guide"
REFERENCE = ROOT / "docs" / "reference"
RESOURCES = ROOT / "src" / "main" / "resources"
BOOK = "guide"
CONTENT = RESOURCES / "assets" / "c0de" / "patchouli_books" / BOOK / "en_us"

# The handbooks: (file in docs/reference without .md, the item that is crafted with a book into the handbook)
HANDBOOKS = [
    ("data", "c0de:type_base"),
    ("statements", "c0de:structural_base"),
    ("tools", "c0de:wrench"),
    ("triggers", "c0de:trigger_block"),
    ("functions", "c0de:function_block"),
]
IMAGE = re.compile(r"!\[[^\]]*\]\(images/([a-z0-9_]+)\.png\)")

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


def book_files(book: str, name: str, landing: str, ingredient: str) -> None:
    """The book definition and its recipe from a book and an ingredient, only loaded if Patchouli is installed."""
    write(RESOURCES / "data" / "c0de" / "patchouli_books" / book / "book.json", {
        "name": name,
        "landing_text": landing,
        "version": 1,
        "use_resource_pack": True,
        "model": "patchouli:book_red",
        "book_texture": "patchouli:textures/gui/book_red.png",
        "creative_tab": "c0de:c0de",
        "show_progress": False,
    })
    write(RESOURCES / "data" / "c0de" / "recipe" / (book + "_book.json"), {
        "fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["patchouli"]}],
        "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "patchouli"}],
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": "minecraft:book"}, {"item": ingredient}],
        "result": {"id": "patchouli:guide_book", "count": 1, "components": {"patchouli:book": "c0de:" + book}},
    })


def guide() -> None:
    book_files(BOOK, "c0de Guide", "Programs built from blocks, written on the greenbar paper of the c0de dimension"
               " and run by trigger blocks. This guide shows how to write and run them.", "c0de:program_block")
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


def parts(lines: list[str]) -> dict[str, list[str]]:
    """The parts of a handbook element: description, recipe or parameters, example."""
    result: dict[str, list[str]] = {"description": []}
    current = "description"
    for line in lines:
        marker = line.strip()
        if marker == "**Recipe**":
            current = "recipe"
        elif marker == "**Parameters and results**":
            current = "parameters"
        elif marker == "**Example**":
            current = "example"
        else:
            result.setdefault(current, []).append(line)
            continue
        result.setdefault(current, [])
    return result


def recipe_result(recipe: str) -> str | None:
    file = RESOURCES / "data" / "c0de" / "recipe" / (recipe + ".json")
    if not file.exists():
        return None
    result = json.loads(file.read_text(encoding="utf-8"))["result"]["id"]
    return "minecraft:book" if result.startswith("patchouli:") else result


def element_pages(lines: list[str]) -> tuple[list[dict], str | None]:
    """The pages of a handbook element and the item of its first recipe."""
    element = parts(lines)
    result = pages(element["description"])
    icon = None
    if "recipe" in element:
        recipe = element["recipe"]
        images = [m.group(1) for line in recipe for m in IMAGE.finditer(line)]
        note = " ".join(line.strip() for line in recipe if line.strip() and not IMAGE.search(line))
        for index, image in enumerate(images):
            page = {"type": "patchouli:crafting", "recipe": "c0de:" + image}
            # A short sentence fits below the crafting grid, a longer one gets a page of its own
            if index == 0 and note and len(note) <= 110:
                page["text"] = inline(note)
            result.append(page)
            icon = icon or recipe_result(image)
        if note and (not images or len(note) > 110):
            result.append({"type": "patchouli:text", "text": inline(note)})
    if "parameters" in element:
        result.extend(pages(element["parameters"]))
    if "example" in element:
        result.extend(pages(element["example"]))
    return result, icon


def anchor(heading: str) -> str:
    """The anchor GitHub gives a heading."""
    return re.sub(r"[^a-z0-9 _-]", "", heading.lower()).replace(" ", "-")


def handbooks() -> None:
    index = ["# Reference", "",
             "The handbooks of c0de: one page per block, item and function, each with a description, its recipe"
             " and an example. The [guide](../guide/01-getting-started.md) explains how to write and run programs."
             " With the mod Patchouli, every handbook is also a red book in the game (crafted from a book and the"
             " item named below).", ""]
    for book, ingredient in HANDBOOKS:
        path = REFERENCE / (book + ".md")
        if not path.exists():
            continue
        lines = path.read_text(encoding="utf-8").splitlines()
        title = next(line[2:].strip() for line in lines if line.startswith("# "))
        content = RESOURCES / "assets" / "c0de" / "patchouli_books" / book / "en_us"
        if content.exists():
            shutil.rmtree(content)
        intro: list[str] = []
        categories: list[tuple[str, list[str], list[tuple[str, list[str]]]]] = []
        for line in lines:
            if line.startswith("# "):
                continue
            if line.startswith("## "):
                categories.append((line[3:].strip(), [], []))
            elif line.startswith("### "):
                categories[-1][2].append((line[4:].strip(), []))
            elif categories and categories[-1][2]:
                categories[-1][2][-1][1].append(line)
            elif categories:
                categories[-1][1].append(line)
            else:
                intro.append(line)
        landing = " ".join(text for kind, text in blocks(intro) if kind == "text")
        book_files(book, "c0de " + title, landing, ingredient)
        index += ["## [" + title + "](" + book + ".md)", "",
                  "Book: crafted from a book and " + ingredient.split(":")[1].replace("_", " ") + ".", ""]
        for number, (category_title, description, elements) in enumerate(categories):
            category = slug(category_title)
            category_icon = None
            index.append("- **" + category_title + "**: " + ", ".join(
                "[" + name + "](" + book + ".md#" + anchor(name) + ")" for name, _ in elements))
            for position, (name, element) in enumerate(elements):
                element_page_list, icon = element_pages(element)
                category_icon = category_icon or icon
                write(content / "entries" / category / (slug(name) + ".json"), {
                    "name": name,
                    "icon": icon or "minecraft:book",
                    "category": "c0de:" + category,
                    "sortnum": position,
                    "pages": element_page_list,
                })
            write(content / "categories" / (category + ".json"), {
                "name": category_title,
                "description": " ".join(text for kind, text in blocks(description) if kind == "text"),
                "icon": category_icon or ingredient,
                "sortnum": number,
            })
        index.append("")
    (REFERENCE / "index.md").write_text("\n".join(index), encoding="utf-8", newline="\n")


def main() -> None:
    # The pages are generated anew, so that pages of removed sections don't stay
    if CONTENT.exists():
        shutil.rmtree(CONTENT)
    guide()
    handbooks()


if __name__ == "__main__":
    main()
