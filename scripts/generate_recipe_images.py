"""Draws the crafting recipes of the mod as images for the reference documentation (docs/reference/images).

Every recipe in src/main/resources/data/c0de/recipe becomes <recipe id>.png: the crafting grid, an arrow and the
result with its count. The icons of the mod come from its textures (blocks show their top and their symbol);
the icons of Minecraft are read from the Minecraft client jar in the Gradle cache of Loom, so this script runs
on a machine that has built the mod, not in the CI.

Usage: python scripts/generate_recipe_images.py
"""

import io
import json
import os
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_textures as textures  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "c0de"
RECIPES = RESOURCES / "data" / "c0de" / "recipe"
OUTPUT = ROOT / "docs" / "reference" / "images"
CLIENT_JAR = Path(os.path.expanduser("~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar"))

SCALE = 2
SLOT = 18
PANEL = (0xC6, 0xC6, 0xC6, 255)
SLOT_INSIDE = (0x8B, 0x8B, 0x8B, 255)
SLOT_DARK = (0x37, 0x37, 0x37, 255)
SLOT_LIGHT = (0xFF, 0xFF, 0xFF, 255)
TEXT = (0x3F, 0x3F, 0x3F, 255)

DIGITS = {
    "0": ["###", "#.#", "#.#", "#.#", "###"], "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["###", "..#", "###", "#..", "###"], "3": ["###", "..#", ".##", "..#", "###"],
    "4": ["#.#", "#.#", "###", "..#", "..#"], "5": ["###", "#..", "###", "..#", "###"],
    "6": ["###", "#..", "###", "#.#", "###"], "7": ["###", "..#", ".#.", ".#.", ".#."],
    "8": ["###", "#.#", "###", "#.#", "###"], "9": ["###", "#.#", "###", "..#", "###"],
}

_jar = None


def jar_texture(path: str):
    """A texture of Minecraft, e.g. item/book, or None."""
    global _jar
    if _jar is None:
        _jar = zipfile.ZipFile(CLIENT_JAR)
    name = "assets/minecraft/textures/" + path + ".png"
    if name not in _jar.namelist():
        return None
    data = _jar.read(name)
    temp = OUTPUT / ".texture.png"
    temp.write_bytes(data)
    image = textures.read_png(temp)
    temp.unlink()
    return image[:16]


def mod_texture(reference: str):
    """A texture referenced by a model, e.g. c0de:block/wrench or minecraft:item/feather."""
    namespace, path = reference.split(":")
    if namespace == "minecraft":
        return jar_texture(path)
    file = ASSETS / "textures" / (path + ".png")
    return textures.read_png(file)[:16] if file.exists() else None


def model(reference: str) -> dict:
    namespace, path = reference.split(":")
    return json.loads((ASSETS / "models" / (path + ".json")).read_text(encoding="utf-8")) if namespace == "c0de" else {}


def overlay(base, top):
    if top is None:
        return base
    result = [list(row) for row in base]
    for y in range(16):
        for x in range(16):
            if top[y][x][3]:
                result[y][x] = top[y][x]
    return result


def tinted(image, color: tuple[int, int, int]):
    """A gray texture in a color, e.g. the hopper of Minecraft in the blue of the I/O block."""
    return [[(r * color[0] // 255, g * color[1] // 255, b * color[2] // 255, a) for r, g, b, a in row]
            for row in image]


def laptop_icon():
    """An open laptop seen from the front: the screen above, the keyboard below."""
    image = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    for y in range(2, 11):
        for x in range(2, 14):
            frame = y in (2, 10) or x in (2, 13)
            image[y][x] = (0x5A, 0x5A, 0x5E, 255) if frame else (0x14, 0x14, 0x14, 255)
    for y, x0, x1 in ((11, 1, 15), (12, 0, 16), (13, 0, 16)):
        for x in range(x0, x1):
            image[y][x] = (0x3A, 0x3A, 0x3E, 255) if y == 12 and x % 2 else (0x6E, 0x6E, 0x72, 255)
    for y, length in ((4, 5), (6, 8)):
        for x in range(4, 4 + length):
            image[y][x] = (0x55, 0xFF, 0x55, 255)
    return image


# Items whose block models can't be drawn from their textures
SPECIAL_ICONS = {
    "io_block": lambda: tinted(jar_texture("item/hopper"), (0x77, 0x77, 0xFF)),
    "laptop": laptop_icon,
    "disk_drive": lambda: mod_texture("c0de:block/disk_drive_front"),
}


def icon(item: str):
    """The icon of an item: its item texture, or the top and the symbol of its block."""
    namespace, name = item.split(":")
    if namespace == "c0de" and name in SPECIAL_ICONS:
        return SPECIAL_ICONS[name]()
    if namespace == "patchouli":
        # The guide books of the mod are red books
        book = jar_texture("item/book")
        return [[(min(255, r + 60), g // 2, b // 2, a) for r, g, b, a in row] for row in book]
    if namespace == "minecraft":
        for path in ("item/" + name, "block/" + name, "block/" + name + "_top", "block/" + name + "_front",
                     "block/" + name + "_side", "item/" + name + "_00"):
            image = jar_texture(path)
            if image is not None:
                return image
        return None
    item_model = model("c0de:item/" + name)
    if "layer0" in item_model.get("textures", {}):
        return mod_texture(item_model["textures"]["layer0"])
    block = model(item_model["parent"])
    names = block.get("textures", {})
    for _ in range(3):
        if names or "parent" not in block:
            break
        block = model(block["parent"])
        names = block.get("textures", {})
    for key in ("top", "up", "all", "outside", "side"):
        if key in names:
            base = mod_texture(names[key])
            symbol = names.get("bottom")
            if symbol and symbol != names[key] and (symbol.endswith("_symbol") or symbol.startswith("minecraft:")):
                return overlay(base, mod_texture(symbol))
            return base
    return None


def fill(canvas, x, y, width, height, color):
    for row in range(y, y + height):
        for column in range(x, x + width):
            canvas[row][column] = color


def slot(canvas, x, y, item):
    fill(canvas, x, y, SLOT, SLOT, SLOT_INSIDE)
    fill(canvas, x, y, SLOT - 1, 1, SLOT_DARK)
    fill(canvas, x, y, 1, SLOT - 1, SLOT_DARK)
    fill(canvas, x + 1, y + SLOT - 1, SLOT - 1, 1, SLOT_LIGHT)
    fill(canvas, x + SLOT - 1, y + 1, 1, SLOT - 1, SLOT_LIGHT)
    image = icon(item) if item else None
    if image is None:
        if item:
            fill(canvas, x + 4, y + 4, 10, 10, SLOT_DARK)
        return
    for row in range(16):
        for column in range(16):
            pixel = image[row][column]
            if pixel[3]:
                canvas[y + 1 + row][x + 1 + column] = pixel


def number(canvas, x, y, value: int):
    text = str(value)
    # White digits with a dark shadow, like the counts of Minecraft
    for offset, color in ((1, SLOT_DARK), (0, SLOT_LIGHT)):
        for i, digit in enumerate(text):
            for row, line in enumerate(DIGITS[digit]):
                for column, c in enumerate(line):
                    if c == "#":
                        # Two pixels per pixel of the digit
                        fill(canvas, x + i * 8 + 2 * column + offset, y + 2 * row + offset, 2, 2, color)


def ingredient(value) -> str | None:
    if isinstance(value, list):
        value = value[0]
    if isinstance(value, dict):
        return value.get("item")
    return None


def grid(recipe: dict) -> list:
    """The 9 items of the crafting grid, row by row; None for empty slots."""
    if recipe["type"] == "minecraft:crafting_shaped":
        cells = [None] * 9
        for row, line in enumerate(recipe["pattern"]):
            for column, key in enumerate(line):
                if key != " ":
                    cells[row * 3 + column] = ingredient(recipe["key"][key])
        return cells
    items = [ingredient(i) for i in recipe["ingredients"]]
    return items + [None] * (9 - len(items))


def draw(recipe: dict):
    width, height = 3 * SLOT + 40 + SLOT + 8, 3 * SLOT + 8
    canvas = [[PANEL] * width for _ in range(height)]
    for i, item in enumerate(grid(recipe)):
        slot(canvas, 4 + (i % 3) * SLOT, 4 + (i // 3) * SLOT, item)
    # The arrow
    arrow_x, middle = 4 + 3 * SLOT + 8, 4 + SLOT + SLOT // 2
    fill(canvas, arrow_x, middle - 1, 16, 3, TEXT)
    for i in range(6):
        fill(canvas, arrow_x + 16 + i, middle - 5 + i, 1, 11 - 2 * i, TEXT)
    result_x = arrow_x + 32
    slot(canvas, result_x, 4 + SLOT, recipe["result"]["id"])
    count = recipe["result"].get("count", 1)
    if count > 1:
        number(canvas, result_x + SLOT - 8 * len(str(count)), 4 + 2 * SLOT - 11, count)
    return [[pixel for pixel in row for _ in range(SCALE)] for row in canvas for _ in range(SCALE)]


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for file in sorted(RECIPES.glob("*.json")):
        recipe = json.loads(file.read_text(encoding="utf-8"))
        textures.write_png(OUTPUT / (file.stem + ".png"), draw(recipe))


if __name__ == "__main__":
    main()
