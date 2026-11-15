"""Generates the handbooks of the mod (docs/concept/21-handbooks.md) from the documentation: the guide (docs/guide)
and the handbooks (docs/reference), and the index of the handbooks (docs/reference/index.md). The German translation
(docs/de, task 14.6) becomes the de_de pages of the same books and docs/de/reference/index.md; the game shows the
pages in its language and falls back to English.

Guide: every file docs/guide/NN-name.md becomes a category, every "## " section an entry. Handbooks: every file
docs/reference/<book>.md becomes a book, every "## " section a category and every "### " element an entry with
the same parts: a description, the recipes (crafting grids; for functions, the parameters and results) and an
example. The pages are Markdown files for lavender-md (assets/c0de/handbook); the book screen distributes them over
pages. The script also writes the book definitions, the recipes of the binders, their item models and names, and
the crafting grids of the recipes the books show (assets/c0de/handbook/recipes.json, written by
generate_content.py after the recipes). All of them are generated files; generate_content.py calls this script and
writes the names (TRANSLATIONS) into the language files.

A translation has the same headings in the same order as the English file. The IDs of its categories and entries
are those of the English headings at the same position, so a page of the translation replaces the English page.

Usage: python scripts/generate_content.py (which calls this script)
"""

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
GUIDE = DOCS / "guide"
REFERENCE = DOCS / "reference"
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "c0de"
HANDBOOK = ASSETS / "handbook"
# The recipes in the format of Minecraft 1.21.1; generate_content.py converts them to the newer format
RECIPES = ROOT / "src" / "main" / "versioned" / "recipes" / "1.21.1" / "data" / "c0de" / "recipe"
BOOK = "guide"

# The languages of the books and the folders of their documentation; English has no language folder
LANGUAGES = {"en_us": DOCS, "de_de": DOCS / "de"}
ENGLISH = "en_us"

# The names and landing texts of the books and the names of their items by language, for the language files
TRANSLATIONS: dict[str, dict[str, str]] = {language: {} for language in LANGUAGES}

# The recipes the books show; their crafting grids go into recipes.json
SHOWN_RECIPES: set[str] = set()

# The texts of the generated parts by language
TEXTS = {
    "en_us": {
        "guide": "c0de Guide",
        "landing": "Programs built from blocks, written on the greenbar paper of the c0de dimension and run by"
                   " trigger blocks. This guide shows how to write and run them.",
        "index_title": "# Reference",
        "index_intro": "The handbooks of c0de: one page per block, item and function, each with a description, its"
                       " recipe and an example. The [guide](../guide/01-getting-started.md) explains how to write"
                       " and run programs. Every handbook is also a red binder in the game (crafted from a book and"
                       " the item named below).",
        "crafted": "Binder: crafted from a book and {}.",
    },
    "de_de": {
        "guide": "c0de-Anleitung",
        "landing": "Programme aus Blöcken, geschrieben auf das Endlospapier der c0de-Dimension und gestartet von"
                   " Auslöserblöcken. Diese Anleitung zeigt, wie du sie schreibst und startest.",
        "index_title": "# Handbücher",
        "index_intro": "Die Handbücher von c0de: eine Seite für jeden Block, jeden Gegenstand und jede Funktion,"
                       " jeweils mit Beschreibung, Rezept und Beispiel. Die [Anleitung](../guide/01-getting-started.md)"
                       " erklärt, wie du Programme schreibst und startest. Jedes Handbuch ist auch ein roter Ordner"
                       " im Spiel (hergestellt aus einem Buch und dem unten genannten Gegenstand).",
        "crafted": "Ordner: hergestellt aus einem Buch und {}.",
    },
}

# The German names of the items the handbooks are crafted from (en: the ID with spaces)
INGREDIENTS_DE = {
    "c0de:type_base": "einer Typbasis",
    "c0de:structural_base": "einer Strukturbasis",
    "c0de:wrench": "einem Schraubenschlüssel",
    "c0de:trigger_block": "einem Auslöserblock",
    "c0de:display_block": "einem Anzeigeblock",
    "c0de:function_block": "einem FUNCTION-Block",
}

# The labels of the parts of a handbook element, in all languages
MARKERS = {
    "**Recipe**": "recipe", "**Rezept**": "recipe",
    "**Parameters and results**": "parameters", "**Parameter und Ergebnisse**": "parameters",
    "**Example**": "example", "**Beispiel**": "example",
}

# The handbooks: (file in docs/reference without .md, the item that is crafted with a book into the handbook)
HANDBOOKS = [
    ("data", "c0de:type_base"),
    ("statements", "c0de:structural_base"),
    ("tools", "c0de:wrench"),
    ("triggers", "c0de:trigger_block"),
    ("devices", "c0de:display_block"),
    ("functions", "c0de:function_block"),
]
# Images of recipes; translations in docs/de/reference use the images of the English folder
IMAGE = re.compile(r"!\[[^\]]*\]\((?:\.\./\.\./reference/)?images/([a-z0-9_]+)\.png\)")

# The characters lavender-md and c0de's features read as Markdown; plain text escapes them with a backslash
SPECIAL = set("\\*_~{[]!<>%`;")
# Code, bold text and links in the documentation, which stay Markdown
INLINE = re.compile(r"`[^`]+`|\*\*(.+?)\*\*|\[([^\]]+)\]\(([^)]+)\)")

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


def write_page(path: Path, meta: dict, markdown: str) -> None:
    """A category or entry: its metadata as a JSON block, then its Markdown."""
    path.parent.mkdir(parents=True, exist_ok=True)
    text = "```json\n" + json.dumps(meta, indent=2, ensure_ascii=False) + "\n```\n"
    if markdown:
        text += "\n" + markdown + "\n"
    path.write_text(text, encoding="utf-8", newline="\n")


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def escape(text: str) -> str:
    return "".join("\\" + c if c in SPECIAL else c for c in text)


def inline(text: str) -> str:
    """A line of the documentation as Markdown of the books: code and bold stay, links to other files become
    their text (web links stay links), everything else is escaped."""
    result = []
    position = 0
    for match in INLINE.finditer(text):
        result.append(escape(text[position:match.start()]))
        if match.group(0).startswith("`"):
            result.append(match.group(0))
        elif match.group(1) is not None:
            result.append("**" + inline(match.group(1)) + "**")
        elif match.group(3).startswith(("http://", "https://")):
            result.append("[" + inline(match.group(2)) + "](" + match.group(3) + ")")
        else:
            result.append(inline(match.group(2)))
        position = match.end()
    result.append(escape(text[position:]))
    return "".join(result)


def blocks(lines: list[str]) -> list[tuple[str, str]]:
    """The blocks of a section: ("text", paragraph), ("list", items) or ("code", program), as Markdown."""
    result = []
    paragraph: list[str] = []
    code: list[str] | None = None

    def flush() -> None:
        if paragraph:
            items = [line for line in paragraph if line.startswith("- ")]
            if items and len(items) == len(paragraph):
                result.append(("list", "\n".join("- " + inline(item[2:].strip()) for item in items)))
            else:
                result.append(("text", inline(" ".join(line.strip() for line in paragraph))))
            paragraph.clear()

    for line in lines:
        if code is not None:
            if line.startswith("```"):
                result.append(("code", program(code)))
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


def program(lines: list[str]) -> str:
    """A program example; the indentation is kept, two spaces per block, to save width."""
    indented = []
    for line in lines:
        stripped = line.lstrip(" ")
        depth = (len(line) - len(stripped)) // 4
        indented.append("  " * depth + stripped)
    return "```c0de\n" + "\n".join(indented) + "\n```"


def markdown(lines: list[str]) -> str:
    return "\n\n".join(content for _, content in blocks(lines))


def text_only(lines: list[str]) -> str:
    return "\n\n".join(content for kind, content in blocks(lines) if kind == "text")


def language_folder(folder: Path, language: str) -> Path:
    return folder if language == ENGLISH else folder / language


def book_files(book: str, language: str, name: str, landing: str, ingredient: str) -> None:
    """The book definition, the recipe of its binder from a book and an ingredient, and the item model of the
    binder. Name and landing text (Markdown) are translation keys; their texts go into TRANSLATIONS of the
    language."""
    TRANSLATIONS[language]["book.c0de." + book + ".name"] = name
    TRANSLATIONS[language]["book.c0de." + book + ".landing_text"] = landing
    TRANSLATIONS[language]["item.c0de." + book + "_book"] = name
    if language != ENGLISH:
        return
    write(HANDBOOK / "books" / (book + ".json"), {
        "name": "book.c0de." + book + ".name",
        "landing_text": "book.c0de." + book + ".landing_text",
        "texture": "c0de:textures/gui/handbook.png",
    })
    write(RECIPES / (book + "_book.json"), {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": "minecraft:book"}, {"item": ingredient}],
        "result": {"id": "c0de:" + book + "_book", "count": 1},
    })
    write(ASSETS / "models" / "item" / (book + "_book.json"),
          {"parent": "minecraft:item/generated", "textures": {"layer0": "c0de:item/" + book + "_book"}})


def guide_structure(path: Path) -> tuple[str, list[str], list[tuple[str, list[str]]]]:
    """The title, the introduction and the "## " sections of a chapter of the guide."""
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
    return title, intro, sections


def matching(translation: list, original: list, what: str) -> None:
    if len(translation) != len(original):
        raise ValueError(f"{what}: {len(translation)} sections, the English file has {len(original)}")


def guide(language: str) -> None:
    texts = TEXTS[language]
    book_files(BOOK, language, texts["guide"], escape(texts["landing"]), "c0de:program_block")
    categories = language_folder(HANDBOOK / "categories" / BOOK, language)
    entries = language_folder(HANDBOOK / "entries" / BOOK, language)
    for number, path in enumerate(sorted(GUIDE.glob("[0-9][0-9]-*.md"))):
        name = path.stem[3:]
        _, _, english = guide_structure(path)
        source = LANGUAGES[language] / "guide" / path.name
        if not source.exists():
            continue
        title, intro, sections = guide_structure(source)
        matching(sections, english, str(source))
        category = slug(name)
        icon = ICONS.get(name, "c0de:noop_block")
        write_page(categories / (category + ".md"), {"title": title, "icon": icon, "ordinal": number},
                   text_only(intro))
        for index, (heading, section) in enumerate(sections):
            write_page(entries / category / (slug(english[index][0]) + ".md"),
                       {"title": heading, "icon": icon, "category": category, "ordinal": index},
                       markdown(section))


def parts(lines: list[str]) -> dict[str, list[str]]:
    """The parts of a handbook element: description, recipe or parameters, example."""
    result: dict[str, list[str]] = {"description": []}
    current = "description"
    for line in lines:
        marker = line.strip()
        if marker in MARKERS:
            current = MARKERS[marker]
            result.setdefault(current, [])
        else:
            result.setdefault(current, []).append(line)
    return result


def recipe_result(recipe: str) -> str | None:
    file = RECIPES / (recipe + ".json")
    if not file.exists():
        return None
    return json.loads(file.read_text(encoding="utf-8"))["result"]["id"]


def element_page(lines: list[str]) -> tuple[str, str | None]:
    """The Markdown of a handbook element and the item of its first recipe."""
    element = parts(lines)
    result = [markdown(element["description"])]
    icon = None
    if "recipe" in element:
        recipe = element["recipe"]
        images = [m.group(1) for line in recipe for m in IMAGE.finditer(line)]
        for image in images:
            result.append("<recipe;c0de:" + image + ">")
            SHOWN_RECIPES.add(image)
            icon = icon or recipe_result(image)
        result.append(markdown([line for line in recipe if not IMAGE.search(line)]))
    if "parameters" in element:
        result.append(markdown(element["parameters"]))
    if "example" in element:
        result.append(markdown(element["example"]))
    return "\n\n".join(part for part in result if part), icon


def anchor(heading: str) -> str:
    """The anchor GitHub gives a heading: lower case, without punctuation (letters like ä stay), spaces to -."""
    return re.sub(r"[^\w -]", "", heading.lower()).replace(" ", "-")


def handbook_structure(path: Path) -> tuple[str, list[str], list[tuple[str, list[str], list[tuple[str, list[str]]]]]]:
    """The title, the introduction and the categories ("## ") with their elements ("### ") of a handbook."""
    lines = path.read_text(encoding="utf-8").splitlines()
    title = next(line[2:].strip() for line in lines if line.startswith("# "))
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
    return title, intro, categories


def handbooks(language: str) -> None:
    texts = TEXTS[language]
    index = [texts["index_title"], "", texts["index_intro"], ""]
    folder = LANGUAGES[language] / "reference"
    for book, ingredient in HANDBOOKS:
        path = folder / (book + ".md")
        if not path.exists():
            continue
        _, _, english = handbook_structure(REFERENCE / (book + ".md"))
        title, intro, categories = handbook_structure(path)
        matching(categories, english, str(path))
        category_folder = language_folder(HANDBOOK / "categories" / book, language)
        entry_folder = language_folder(HANDBOOK / "entries" / book, language)
        book_files(book, language, "c0de " + title, text_only(intro), ingredient)
        item = ingredient.split(":")[1].replace("_", " ") if language == ENGLISH else INGREDIENTS_DE[ingredient]
        index += ["## [" + title + "](" + book + ".md)", "", texts["crafted"].format(item), ""]
        for number, (category_title, description, elements) in enumerate(categories):
            english_elements = english[number][2]
            matching(elements, english_elements, f"{path}, {category_title}")
            category = slug(english[number][0])
            category_icon = None
            index.append("- **" + category_title + "**: " + ", ".join(
                "[" + name + "](" + book + ".md#" + anchor(name) + ")" for name, _ in elements))
            for position, (name, element) in enumerate(elements):
                page, icon = element_page(element)
                category_icon = category_icon or icon
                write_page(entry_folder / category / (slug(english_elements[position][0]) + ".md"),
                           {"title": name, "icon": icon or "minecraft:book", "category": category,
                            "ordinal": position},
                           page)
            write_page(category_folder / (category + ".md"),
                       {"title": category_title, "icon": category_icon or ingredient, "ordinal": number},
                       text_only(description))
        index.append("")
    if folder.exists():
        (folder / "index.md").write_text("\n".join(index), encoding="utf-8", newline="\n")


def ingredient_items(ingredient) -> list[str]:
    """The items of an ingredient of a recipe in the format of 1.21.1: an item, or a list of items."""
    if isinstance(ingredient, list):
        return [item for part in ingredient for item in ingredient_items(part)]
    if "item" in ingredient:
        return [ingredient["item"]]
    raise ValueError(f"Handbook recipes only show items, not {ingredient}")


def write_recipes() -> None:
    """The crafting grids of the recipes the books show: nine slots with the items they cycle through."""
    recipes = {}
    for name in sorted(SHOWN_RECIPES):
        recipe = json.loads((RECIPES / (name + ".json")).read_text(encoding="utf-8"))
        slots: list[list[str]] = [[] for _ in range(9)]
        if recipe["type"] == "minecraft:crafting_shaped":
            for row, line in enumerate(recipe["pattern"]):
                for column, key in enumerate(line):
                    if key != " ":
                        slots[row * 3 + column] = ingredient_items(recipe["key"][key])
        else:
            for index, ingredient in enumerate(recipe["ingredients"]):
                slots[index] = ingredient_items(ingredient)
        result = recipe["result"]
        recipes["c0de:" + name] = {"slots": slots, "result": result["id"], "count": result.get("count", 1)}
    write(HANDBOOK / "recipes.json", recipes)


def main() -> None:
    # The pages are generated anew, so that pages of removed sections don't stay
    for folder in (HANDBOOK / "categories", HANDBOOK / "entries"):
        if folder.exists():
            shutil.rmtree(folder)
    for language in LANGUAGES:
        guide(language)
        handbooks(language)


if __name__ == "__main__":
    main()
    write_recipes()
