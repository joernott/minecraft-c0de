"""Generates the provisional textures of the mod (16x16 PNG files).

The textures are placeholders until the final artwork is made (sprint 11). They are generated
deterministically, so running the script again produces the same files.

Usage: python scripts/generate_textures.py
"""

import random
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "c0de" / "textures"

Pixel = tuple[int, int, int, int]
Image = list[list[Pixel]]


def write_png(path: Path, image: Image) -> None:
    """Writes an RGBA image as PNG."""
    height = len(image)
    width = len(image[0])
    raw = b"".join(b"\x00" + b"".join(bytes(pixel) for pixel in row) for row in image)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def rgb(color: int, alpha: int = 255) -> Pixel:
    return (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, alpha


def shade(pixel: Pixel, delta: int) -> Pixel:
    r, g, b, a = pixel
    return max(0, min(255, r + delta)), max(0, min(255, g + delta)), max(0, min(255, b + delta)), a


def concrete(color: int, seed: str, amount: int = 4) -> Image:
    """A flat color with a slight noise, similar to concrete."""
    rnd = random.Random(seed)
    base = rgb(color)
    return [[shade(base, rnd.randint(-amount, amount)) for _ in range(16)] for _ in range(16)]


def cursor_top() -> Image:
    """Gray concrete with a rail running from north to south (top to bottom of the texture)."""
    image = concrete(0x7D7D73, "cursor_top")
    for y in range(16):
        for x in (6, 9):
            image[y][x] = rgb(0xB8B8C0)  # rails
        for x in (7, 8):
            image[y][x] = shade(image[y][x], -12)
        if y % 4 == 1:
            for x in range(5, 11):
                image[y][x] = rgb(0x6B4A2B)  # sleepers
            image[y][6] = rgb(0xB8B8C0)
            image[y][9] = rgb(0xB8B8C0)
    return image


def trigger_frame() -> Image:
    """A transparent block with a glass edge and copper corners."""
    image = [[CLEAR for _ in range(16)] for _ in range(16)]
    for i in range(16):
        for edge in (0, 15):
            image[i][edge] = rgb(0xA0B8C0)
            image[edge][i] = rgb(0xA0B8C0)
    for cx, cy in ((0, 0), (12, 0), (0, 12), (12, 12)):
        for y in range(4):
            for x in range(4):
                image[cy + y][cx + x] = rgb(0xC15A36 if (x + y) % 3 else 0xE77C56)
    return image


def ball(image: Image, light: int, dark: int, rim: int) -> None:
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            distance = dx * dx + dy * dy
            if distance <= 9:
                image[y][x] = rgb(light if dx + dy < -1 else dark)
            elif distance <= 12.5:
                image[y][x] = rgb(rim)


def trigger_block() -> Image:
    """Trigger block: a transparent block with copper corners and a golden ball in its center."""
    image = trigger_frame()
    ball(image, 0xFDF55F, 0xE9B115, 0xB26411)
    return image


def debug_trigger_block() -> Image:
    """Debug trigger block: like the trigger block, with a red ball."""
    image = trigger_frame()
    ball(image, 0xFF6060, 0xC81E1E, 0x781010)
    return image


def timer_block() -> Image:
    """Timer block: like the trigger block, with a clock."""
    image = trigger_frame()
    icon(image, "clock")
    return image


def pid_block() -> Image:
    """The PID block: almost black; its PID is drawn by the renderer."""
    return concrete(0x222222, "pid", 2)


def wrench() -> Image:
    """Provisional wrench: an iron open-end wrench with a red handle, drawn diagonally."""
    clear = (0, 0, 0, 0)
    image = [[clear for _ in range(16)] for _ in range(16)]
    iron, iron_dark, red, red_dark = rgb(0xD8D8D8), rgb(0x8C8C8C), rgb(0xB02E26), rgb(0x6E1A16)
    # Handle from the bottom left to the center
    for i in range(2, 10):
        x, y = i, 15 - i
        image[y][x] = red
        image[y][x - 1] = red_dark
        image[y - 1][x] = red
    # Shaft
    for i in range(9, 12):
        x, y = i, 15 - i
        image[y][x] = iron
        image[y][x - 1] = iron_dark
        image[y - 1][x] = iron
    # Open jaw at the top right
    for x, y in ((11, 2), (12, 1), (13, 1), (14, 2), (14, 3), (13, 4), (11, 3), (12, 5), (13, 5), (10, 3), (10, 4)):
        image[y][x] = iron
    for x, y in ((12, 2), (13, 2), (12, 3), (13, 3)):
        image[y][x] = clear
    for x, y in ((11, 4), (12, 4)):
        image[y][x] = iron_dark
    return image


CLEAR: Pixel = (0, 0, 0, 0)
WHITE = 0xFFFFFF

# 5x7 glyphs for the symbols on the blocks
GLYPHS = {
    "?": [".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#.."],
    "¿": ["..#..", ".....", "..#..", ".#...", "#....", "#...#", ".###."],
    "ƒ": ["...##", "..#..", ".####", "..#..", "..#..", "..#..", "##..."],
    "#": [".#.#.", ".#.#.", "#####", ".#.#.", "#####", ".#.#.", ".#.#."],
}


def blank() -> Image:
    return [[CLEAR for _ in range(16)] for _ in range(16)]


def framed(color: int, seed: str) -> Image:
    """A flat color with a slight noise and a darker border."""
    image = concrete(color, seed, 3)
    for i in range(16):
        for edge in (0, 15):
            image[i][edge] = shade(image[i][edge], -40)
            image[edge][i] = shade(image[edge][i], -40)
    return image


def paint(image: Image, inside, color: int) -> None:
    """Sets the pixels whose centers (relative to the center of the texture) satisfy the predicate."""
    for y in range(16):
        for x in range(16):
            if inside(x - 7.5, y - 7.5):
                image[y][x] = rgb(color)


def glyph(image: Image, char: str, color: int, left: int = 5, top: int = 4) -> None:
    for y, row in enumerate(GLYPHS[char]):
        for x, c in enumerate(row):
            if c == "#":
                image[top + y][left + x] = rgb(color)


def mask(image: Image, rows: list[str], palette: dict[str, int], left: int, top: int) -> None:
    """Draws a picture given as rows of characters; '.' is left unchanged."""
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c != ".":
                image[top + y][left + x] = rgb(palette[c])


def square(dx: float, dy: float) -> bool:
    return abs(dx) <= 4.5 and abs(dy) <= 4.5


def diamond(dx: float, dy: float) -> bool:
    return abs(dx) + abs(dy) <= 6


def circle(dx: float, dy: float) -> bool:
    return dx * dx + dy * dy <= 30


def octagon(dx: float, dy: float) -> bool:
    return abs(dx) <= 6 and abs(dy) <= 6 and abs(dx) + abs(dy) <= 9


def ring(dx: float, dy: float) -> bool:
    """A circular arrow: a ring with a gap on the right."""
    return 16 <= dx * dx + dy * dy <= 36 and not (dx > 0 and -2 < dy < 1)


def noop() -> Image:
    """Transparent, with metal corners and wooden edges between them."""
    image = blank()
    for i in range(16):
        for edge in (0, 15):
            image[i][edge] = rgb(0x8F6A3A)
            image[edge][i] = rgb(0x8F6A3A)
    for cx in (0, 13):
        for cy in (0, 13):
            for y in range(3):
                for x in range(3):
                    image[cy + y][cx + x] = rgb(0xD8D8D8 if (x + y) % 2 else 0xA8A8A8)
    return image


def comment() -> Image:
    """Glass with a yellow sticky note that shows //."""
    image = blank()
    for i in range(16):
        for edge in (0, 15):
            image[i][edge] = rgb(0xD6E8EE)
            image[edge][i] = rgb(0xD6E8EE)
    for y in range(4, 12):
        for x in range(4, 12):
            image[y][x] = rgb(0xFFF176)
    for x, y in ((6, 10), (6, 9), (7, 8), (7, 7), (8, 6), (8, 5), (8, 10), (8, 9), (9, 8), (9, 7), (10, 6), (10, 5)):
        image[y][x] = rgb(0x555555)
    return image


ICONS: dict[str, tuple[list[str], dict[str, int]]] = {
    "book": ([
        ".bbbbbbbb.",
        ".bwwwwwwb.",
        ".bwbbbbwb.",
        ".bwwwwwwb.",
        ".bwbbbbwb.",
        ".bwwwwwwb.",
        ".bbbbbbbb.",
        ".bwwwwwww.",
    ], {"b": 0x6B3A1E, "w": 0xF0E6C8}),
    "map": ([
        "wwwwwwwwww",
        "wggwwbbwww",
        "wgggwbbbww",
        "wwggwwbbww",
        "wwwwrwwwww",
        "wbbwwwggww",
        "wbbbwgggww",
        "wwwwwwwwww",
    ], {"w": 0xE8DDB5, "g": 0x6FA35A, "b": 0x4A7CC2, "r": 0xC03030}),
    "door": ([
        "dddddd.",
        "dwdwwd.",
        "dwdwwd.",
        "dddddd.",
        "dlllld.",
        "dllllde",
        "dlllld.",
        "dlllld.",
        "dddddd.",
    ], {"d": 0x8C8C8C, "w": 0xE0E8F0, "l": 0xC8C8C8, "e": 0x505050}),
    "spruce_door": ([
        "dddddd.",
        "dllldd.",
        "dllldd.",
        "dddddd.",
        "dlllld.",
        "dllllde",
        "dlllld.",
        "dddddd.",
    ], {"d": 0x5A3D1E, "l": 0x7A5430, "e": 0xC8C8C8}),
    "stick": ([
        "........ss",
        ".......ss.",
        "......ss..",
        "..........",
        "....ss....",
        "...ss.....",
        "..ss......",
        ".ss.......",
    ], {"s": 0x8F6A3A}),
    "hopper": ([
        "gggggggggg",
        "gddddddddg",
        ".gddddddg.",
        "..gddddg..",
        "...gddg...",
        "....gg....",
        "....gg....",
    ], {"g": 0x4A4A4A, "d": 0x2A2A2A}),
    "phone": ([
        ".kk....kk.",
        "kkkkkkkkkk",
        "kk......kk",
        "..........",
        "...kkkk...",
        "..kkwwkk..",
        ".kkkkkkkk.",
        ".kkkkkkkk.",
    ], {"k": 0x202020, "w": 0xDDDDDD}),
    "torch": ([
        ".rr.",
        "rrrr",
        ".rr.",
        ".ss.",
        ".ss.",
        ".ss.",
        ".ss.",
    ], {"r": 0xE02020, "s": 0x8F6A3A}),
    "heap": ([
        "...hh...",
        "..hhhh..",
        ".hhhhhh.",
        "hhhhhhhh",
    ], {"h": 0}),
    "plank": ([
        "pppppppp",
        "qqqqqqqq",
        "pppppppp",
        "qqqqqqqq",
        "pppppppp",
    ], {"p": 0xB8945F, "q": 0x9C7A48}),
    "string": ([
        "s.......",
        ".s....s.",
        "..s..s.s",
        "...ss...",
    ], {"s": 0x505050}),
    "barrel": ([
        ".bbbbbb.",
        "bwwwwwwb",
        "bbbbbbbb",
        "bwwwwwwb",
        "bbbbbbbb",
        ".bbbbbb.",
    ], {"b": 0x5A3D1E, "w": 0x8F6A3A}),
    "chest": ([
        "cccccccc",
        "cwwwwwwc",
        "cccgcccc",
        "cwwgwwwc",
        "cwwwwwwc",
        "cccccccc",
    ], {"c": 0x5A3D1E, "w": 0xA77A3C, "g": 0xE0C040}),
    "scaffolding": ([
        "yyyyyyyy",
        "yy....yy",
        "y.y..y.y",
        "y..yy..y",
        "y.y..y.y",
        "yyyyyyyy",
    ], {"y": 0xC8A050}),
    "clock": ([
        "..gggg..",
        ".gwwwwg.",
        "gwwkwwwg",
        "gwwkwwwg",
        "gwwkkkwg",
        ".gwwwwg.",
        "..gggg..",
    ], {"g": 0xE0C040, "w": 0xF8F0D0, "k": 0x202020}),
    "campfire": ([
        "...oo...",
        "..oyyo..",
        "..oyyo..",
        ".llllll.",
        "llllllll",
        ".llllll.",
    ], {"o": 0xF08020, "y": 0xFFE060, "l": 0x6B4A2B}),
    "tag": ([
        "..wwwwwwww",
        ".wwwwwwwww",
        "wwkwwwwwww",
        ".wwwwwwwww",
        "..wwwwwwww",
    ], {"w": 0xFFFFFF, "k": 0x553311}),
}


def icon(image: Image, name: str, color: int | None = None) -> None:
    """Draws an icon in the center of the texture, optionally in a single color."""
    rows, palette = ICONS[name]
    if color is not None:
        palette = {k: color for k in palette}
    mask(image, rows, palette, (16 - len(rows[0])) // 2, (16 - len(rows)) // 2)


def upside_down(image: Image) -> None:
    image.reverse()


def trapezoid(wide_at_bottom: bool):
    def inside(dx: float, dy: float) -> bool:
        grow = dy + 3 if wide_at_bottom else 3 - dy
        return -3 <= dy <= 3 and abs(dx) <= 2 + grow * 0.5
    return inside


def triangle(dx: float, dy: float) -> bool:
    """A triangle pointing to the left."""
    return -4 <= dx <= 4 and abs(dy) <= (dx + 4) * 0.6


def triangle_right(dx: float, dy: float) -> bool:
    return triangle(-dx, dy)


def dot(dx: float, dy: float) -> bool:
    """The tip of an arrow pointing at the viewer."""
    return dx * dx + dy * dy <= 9


def cross(dx: float, dy: float) -> bool:
    """The tail of an arrow pointing away from the viewer."""
    return abs(dx) <= 4.5 and abs(dy) <= 4.5 and (abs(dx - dy) <= 0.8 or abs(dx + dy) <= 0.8)


# The sides of the ASSIGN block: its triangle points to positive x on every side (see generate_content.py)
ASSIGN_SIDES = {"left": triangle, "right": triangle_right, "dot": dot, "cross": cross}


def frame(dx: float, dy: float) -> bool:
    return square(dx, dy) and not (abs(dx) <= 3.5 and abs(dy) <= 3.5)


def small_square(dx: float, dy: float) -> bool:
    return abs(dx) <= 2 and abs(dy) <= 2


# (block ID, valid color, invalid color, function that draws the symbol)
CODE_BLOCKS = [
    ("label_block", 0xFFFFFF, 0xAAAAAA, lambda i: None),
    ("literal_block", 0xCCCCCC, 0xAAAAAA, lambda i: None),
    ("operator_block", 0xFF5555, 0xAA0000, lambda i: paint(i, circle, WHITE)),
    ("tag_block", 0xAA7744, 0x553311, lambda i: icon(i, "tag")),
    ("program_block", 0x555555, 0x000000, lambda i: icon(i, "book")),
    ("include_block", 0x555555, 0x000000, lambda i: icon(i, "map")),
    ("exit_block", 0x555555, 0x000000, lambda i: icon(i, "door")),
    ("begin_block", 0xFFFF55, 0xFFAA00, lambda i: paint(i, trapezoid(True), WHITE)),
    ("end_block", 0xFFFF55, 0xFFAA00, lambda i: paint(i, trapezoid(False), WHITE)),
    ("break_block", 0xFFFF55, 0xFFAA00, lambda i: icon(i, "stick")),
    ("variable_block", 0x5555FF, 0x0000AA, lambda i: paint(i, square, WHITE)),
    ("constant_block", 0x5555FF, 0x0000AA, lambda i: paint(i, diamond, WHITE)),
    ("function_block", 0xAAAAAA, 0x555555, lambda i: (paint(i, circle, WHITE), glyph(i, "ƒ", 0x202020))),
    ("parameter_block", 0xFF55FF, 0xAA00AA, lambda i: icon(i, "hopper")),
    ("result_block", 0xFF55FF, 0xAA00AA, lambda i: (upside_down(i), icon(i, "hopper"), upside_down(i))),
    ("return_block", 0xFF55FF, 0xAA00AA, lambda i: icon(i, "spruce_door")),
    ("call_block", 0xAAAAAA, 0x555555, lambda i: icon(i, "phone")),
    ("if_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, diamond, WHITE), glyph(i, "?", 0x00AA00, 5, 5))),
    ("else_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, diamond, WHITE), glyph(i, "¿", 0x00AA00, 5, 4))),
    ("for_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, ring, WHITE), glyph(i, "#", WHITE, 5, 5))),
    ("each_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, ring, WHITE), paint(i, small_square, 0x8A8A8A))),
    ("while_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, ring, WHITE), glyph(i, "?", WHITE, 5, 5))),
    ("repeat_block", 0x55FF55, 0x00AA00, lambda i: (paint(i, frame, WHITE), glyph(i, "?", WHITE, 5, 5))),
]

# (block ID, icon, color of the icon, or None for its own colors)
TYPE_BLOCKS = [
    ("type_boolean_block", "torch", None),
    ("type_integer_block", "heap", 0xF0D040),
    ("type_float_block", "heap", 0xF09030),
    ("type_char_block", "plank", None),
    ("type_string_block", "string", None),
    ("type_array_block", "barrel", None),
    ("type_hash_block", "chest", None),
    ("type_struct_block", "scaffolding", None),
    ("type_time_block", "clock", None),
    ("type_error_block", "campfire", None),
]


def base_item(color: int, symbol: str | None) -> Image:
    """A small cube in the color of the blocks it is used for."""
    image = blank()
    for y in range(2, 14):
        for x in range(2, 14):
            image[y][x] = rgb(color) if 2 < x < 13 and 2 < y < 13 else shade(rgb(color), -50)
    if symbol:
        glyph(image, symbol, 0x000000, 5, 4)
    return image


def code_block_textures(blocks: Path) -> None:
    write_png(blocks / "noop_block.png", noop())
    write_png(blocks / "comment_block.png", comment())
    for block, valid, invalid, draw in CODE_BLOCKS:
        for suffix, color in (("_valid", valid), ("_invalid", invalid)):
            image = framed(color, block + suffix)
            draw(image)
            write_png(blocks / (block + suffix + ".png"), image)
    for side, shape in ASSIGN_SIDES.items():
        for suffix, color in (("_valid", 0x55FFFF), ("_invalid", 0x00AAAA)):
            image = framed(color, "assign_block" + suffix)
            paint(image, shape, WHITE)
            write_png(blocks / ("assign_block" + suffix + "_" + side + ".png"), image)
    for block, name, color in TYPE_BLOCKS:
        image = framed(0xCC2222, block)
        paint(image, octagon, WHITE)
        icon(image, name, color)
        write_png(blocks / (block + ".png"), image)
    write_png(TEXTURES / "item" / "type_base.png", base_item(0xCC2222, "?"))
    structural = base_item(0x55FF55, None)
    paint(structural, trapezoid(True), WHITE)
    write_png(TEXTURES / "item" / "structural_base.png", structural)


def card(border: int | None) -> Image:
    """A punch card: cream with light gray markings and the upper right corner cut off, optionally framed."""
    image = blank()
    for y in range(2, 14):
        for x in range(1, 15):
            if x - y > 10:
                continue  # the cut corner
            image[y][x] = rgb(0xFFFEE9)
    for y in (5, 8, 11):
        for x in range(3, 12, 2):
            image[y][x] = rgb(0xCCCCCC)
    if border is not None:
        for y in range(2, 14):
            for x in range(1, 15):
                if image[y][x] == CLEAR:
                    continue
                edge = x in (1, 14) or y in (2, 13) or x - y == 10 or (x - y == 9 and image[y][x + 1] == CLEAR)
                if edge:
                    image[y][x] = rgb(border)
    return image


# Type items and the icons of their type blocks
TYPE_ITEM_ICONS = [
    ("BOOLEAN", "torch", None),
    ("INTEGER", "heap", 0xF0D040),
    ("FLOAT", "heap", 0xF09030),
    ("CHAR", "plank", None),
    ("STRING", "string", None),
    ("ARRAY", "barrel", None),
    ("HASH", "chest", None),
    ("STRUCT", "scaffolding", None),
    ("TIME", "clock", None),
    ("ERROR", "campfire", None),
]


def item_textures() -> None:
    items = TEXTURES / "item"
    write_png(items / "data_item.png", card(None))
    write_png(items / "variable_item.png", card(0x5555FF))
    write_png(items / "constant_item.png", card(0xFF55FF))
    for type_name, name, color in TYPE_ITEM_ICONS:
        image = card(0xCC2222)
        icon(image, name, color)
        write_png(items / ("type_" + type_name.lower() + "_item.png"), image)


def debug_tool() -> Image:
    """A fly swatter: a wooden handle and a leather square at the top."""
    image = blank()
    for i in range(2, 10):
        image[15 - i][i] = rgb(0x8F6A3A)
        image[16 - i][i] = rgb(0x6B4A2B)
    for y in range(1, 8):
        for x in range(8, 15):
            image[y][x] = rgb(0xA0602E if (x + y) % 3 else 0x7A4520)
    return image


def marker() -> Image:
    """A yellow feather."""
    image = blank()
    for i in range(1, 14):
        image[15 - i][i] = rgb(0xC8A000)
    for i in range(4, 13):
        for d in (1, 2, 3):
            if 1 <= i - d and 15 - i - d >= 0:
                image[15 - i - d + 1][i - d + 1] = rgb(0xFFE040 if d < 3 else 0xF0C820)
            if i + d <= 15 and 15 - i + d <= 15:
                image[15 - i + d - 1][i + d - 1] = rgb(0xFFE040 if d < 3 else 0xF0C820)
    return image


def hammer() -> Image:
    """An iron hammer with a red handle: a heavy head across the upper end of the handle."""
    image = blank()
    for i in range(1, 11):
        image[15 - i][i] = rgb(0xB02E26)
        image[16 - i][i] = rgb(0x6E1A16)
    # The head is a block across the handle, from the upper left to the lower right
    for t in range(-4, 5):
        for d in range(0, 4):
            for dx in (0, 1):
                x, y = 9 + t + d + dx, 6 + t - d
                if 0 <= x < 16 and 0 <= y < 16:
                    edge = t in (-4, 4) or d in (0, 3)
                    image[y][x] = rgb(0x8C8C8C if edge else 0xD0D0D0)
    return image


def disk() -> Image:
    """A black floppy disk with a metal shutter and a white label."""
    image = blank()
    for y in range(1, 15):
        for x in range(1, 15):
            image[y][x] = rgb(0x202020 if (x + y) % 5 else 0x2C2C2C)
    image[1][14] = (0, 0, 0, 0)
    for y in range(1, 6):
        for x in range(4, 12):
            image[y][x] = rgb(0xB8B8B8 if x != 9 else 0x606060)
    for y in range(8, 14):
        for x in range(3, 13):
            image[y][x] = rgb(0xF0F0F0 if y != 10 else 0x3A9A3A)
    return image


def trash() -> Image:
    """A grey trash can for the button that clears the fields of a dialog."""
    image = blank()
    for x in range(6, 10):
        image[1][x] = rgb(0xC8C8C8)
    for x in range(2, 14):
        image[2][x] = rgb(0xC8C8C8)
        image[3][x] = rgb(0x8C8C8C)
    for y in range(4, 15):
        for x in range(3, 13):
            stripe = x in (5, 8, 11) and 5 <= y <= 13
            image[y][x] = rgb(0x6E6E6E if stripe else 0xB4B4B4)
    return image


def main() -> None:
    blocks = TEXTURES / "block"
    write_png(blocks / "white_c0de_bedrock.png", concrete(0xF5F7F3, "white"))
    write_png(blocks / "light_green_c0de_bedrock.png", concrete(0xD2EBD7, "green"))
    write_png(blocks / "black_c0de_bedrock.png", concrete(0x000000, "black", 0))
    write_png(blocks / "cursor_c0de_bedrock_top.png", cursor_top())
    write_png(blocks / "cursor_c0de_bedrock_side.png", concrete(0x7D7D73, "cursor_side"))
    write_png(blocks / "trigger_block.png", trigger_block())
    write_png(blocks / "timer_block.png", timer_block())
    write_png(blocks / "debug_trigger_block.png", debug_trigger_block())
    write_png(blocks / "pid_block.png", pid_block())
    write_png(TEXTURES / "item" / "wrench.png", wrench())
    code_block_textures(blocks)
    item_textures()
    write_png(TEXTURES / "item" / "debug_tool.png", debug_tool())
    write_png(TEXTURES / "item" / "marker.png", marker())
    write_png(TEXTURES / "item" / "hammer.png", hammer())
    write_png(TEXTURES / "item" / "c0de_disk.png", disk())
    write_png(TEXTURES / "gui" / "trash.png", trash())


if __name__ == "__main__":
    main()
