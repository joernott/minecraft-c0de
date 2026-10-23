"""The block models of the language blocks, used by generate_content.py.

All language blocks are as high as a slab (8 pixels). Their colors don't change with the state of the line; the
line numbers on the floor show it. Most blocks are slabs with their symbol on the top. Some have a shape instead:
  - PARAMETER is a hopper and RESULT an upside-down hopper, both scaled to half their size,
  - VARIABLE is a chest and CONSTANT a barrel, scaled to the height of a slab,
  - OPERATOR is the symbol of its operator and COMMENT two slashes, extruded from the floor,
  - INCLUDE carries a bookshelf, as if it lay in an invisible item frame,
  - BEGIN, END, PROGRAM, LITERAL, ASSIGN, IF, ELSE and the loops (FOR, EACH, WHILE, REPEAT) are slabs in their
    JSON models; ExtrudedModel (client) gives them their shapes (trapezoid, semicircle, octagon, triangle, diamond
    and cylinder), because JSON models can't have diagonal or round sides. The bottom texture of PROGRAM is the book that ExtrudedModel lays on its top.
The outline and collision shapes in CodeBlockShapes MUST match.
"""

SLAB = 8
SIDES_4 = ("north", "south", "west", "east")


def auto_uv(start: list, end: list, side: str) -> list:
    """The UV of a face as Minecraft computes it from the coordinates of the element."""
    (x1, y1, z1), (x2, y2, z2) = start, end
    return {
        "up": [x1, z1, x2, z2], "down": [x1, 16 - z2, x2, 16 - z1],
        "north": [16 - x2, 16 - y2, 16 - x1, 16 - y1], "south": [x1, 16 - y2, x2, 16 - y1],
        "west": [z1, 16 - y2, z2, 16 - y1], "east": [16 - z2, 16 - y2, 16 - z1, 16 - y1],
    }[side]


def number(value: float):
    return int(value) if float(value).is_integer() else round(value, 4)


def element(start: list, end: list, faces: dict, uv: dict | None = None) -> dict:
    """A cuboid. Faces on the border of the block are culled; uv maps sides to explicit UVs."""
    border = {"down": start[1] == 0, "up": end[1] == 16, "north": start[2] == 0, "south": end[2] == 16,
              "west": start[0] == 0, "east": end[0] == 16}
    result = {}
    for side, texture in faces.items():
        face = {"texture": texture}
        if uv and side in uv:
            face["uv"] = [number(v) for v in uv[side]]
        if border[side]:
            face["cullface"] = side
        result[side] = face
    return {"from": [number(v) for v in start], "to": [number(v) for v in end], "faces": result}


def scaled(elements: list, factor: float) -> list:
    """Elements scaled around the center of the bottom of the block. The UVs stay those of the full size, so the
    textures are scaled with the shape instead of being cut."""
    result = []
    for e in elements:
        start, end = e["from"], e["to"]
        uv = {side: f.get("uv") or auto_uv(start, end, side) for side, f in e["faces"].items()}
        faces = {side: f["texture"] for side, f in e["faces"].items()}
        new_start = [8 + (start[0] - 8) * factor, start[1] * factor, 8 + (start[2] - 8) * factor]
        new_end = [8 + (end[0] - 8) * factor, end[1] * factor, 8 + (end[2] - 8) * factor]
        result.append(element(new_start, new_end, faces, uv))
    return result


def flipped(elements: list, height: float) -> list:
    """The elements upside down within the given height: y is mirrored and the up and down faces swap."""
    result = []
    for e in elements:
        swap = {"up": "down", "down": "up"}
        faces = {swap.get(s, s): f["texture"] for s, f in e["faces"].items()}
        uv = {swap.get(s, s): f["uv"] for s, f in e["faces"].items() if "uv" in f}
        result.append(element([e["from"][0], height - e["to"][1], e["from"][2]],
                              [e["to"][0], height - e["from"][1], e["to"][2]], faces, uv))
    return result


def hopper() -> list:
    """A hopper: a bowl with a rim, a middle part and a spout, like the vanilla hopper."""
    def walls(start, end, inner):
        faces = {side: "#outside" for side in SIDES_4}
        faces.update({"up": "#top", "down": "#outside", inner: "#inside"})
        return element(start, end, faces)
    return [
        element([0, 10, 0], [16, 11, 16], dict({side: "#outside" for side in SIDES_4}, up="#inside", down="#outside")),
        walls([0, 11, 0], [2, 16, 16], "east"),
        walls([14, 11, 0], [16, 16, 16], "west"),
        walls([2, 11, 0], [14, 16, 2], "south"),
        walls([2, 11, 14], [14, 16, 16], "north"),
        element([4, 4, 4], [12, 10, 12], dict({side: "#outside" for side in SIDES_4}, down="#outside")),
        element([6, 0, 6], [10, 4, 10], dict({side: "#outside" for side in SIDES_4}, down="#outside")),
    ]


FULL = [0, 0, 16, 16]


def chest() -> list:
    """A chest (14 pixels wide and high, the latch in the south) scaled to the height of a slab."""
    body = element([1, 0, 1], [15, 14, 15],
                   {"up": "#top", "down": "#top", "north": "#side", "west": "#side", "east": "#side", "south": "#front"},
                   {side: FULL for side in ("up", "down", "north", "south", "west", "east")})
    latch = element([7, 7, 15], [9, 11, 16], {side: "#latch" for side in ("up", "down", "south", "west", "east")},
                    {side: FULL for side in ("up", "down", "south", "west", "east")})
    return scaled([body, latch], SLAB / 14)


def barrel() -> list:
    """A barrel scaled to the height of a slab."""
    body = element([0, 0, 0], [16, 16, 16], dict({side: "#side" for side in SIDES_4}, up="#top", down="#top"),
                   {side: FULL for side in ("up", "down", "north", "south", "west", "east")})
    return scaled([body], SLAB / 16)


# Glyphs of the operators, lying on the floor and readable from the south (the first row is in the north).
# Symbols are 7 x 7 pixels; words use letters of 3 x 5 pixels (N: 4 x 5).
LETTERS = {
    "A": [".#.", "#.#", "###", "#.#", "#.#"],
    "D": ["##.", "#.#", "#.#", "#.#", "##."],
    "N": ["#..#", "##.#", "#.##", "#..#", "#..#"],
    "O": [".#.", "#.#", "#.#", "#.#", ".#."],
    "R": ["##.", "#.#", "##.", "#.#", "#.#"],
    "T": ["###", ".#.", ".#.", ".#.", ".#."],
    "X": ["#.#", "#.#", ".#.", "#.#", "#.#"],
}


def word(text: str) -> list:
    rows = ["" for _ in range(5)]
    for i, letter in enumerate(text):
        for r in range(5):
            rows[r] += ("." if i else "") + LETTERS[letter][r]
    return rows


def mirrored(glyph: list) -> list:
    return [row[::-1] for row in glyph]


LESS = ["......#", "....##.", "..##...", "##.....", "..##...", "....##.", "......#"]
LESS_OR_EQUAL = ["....##.", "..##...", "##.....", "..##...", "....##.", ".......", "#######"]
LEFT = [".......", "..#....", ".#.....", "#######", ".#.....", "..#....", "......."]
OPEN = ["....#..", "...#...", "..#....", "..#....", "..#....", "...#...", "....#.."]
SLASH = ["......#", ".....#.", "....#..", "...#...", "..#....", ".#.....", "#......"]
MINUS = [".......", ".......", ".......", "#######", ".......", ".......", "......."]

# The block state operator: none and the constants of the enum Operator in lower case (OperatorSymbol)
GLYPHS = {
    "none": [".#####.", "#.....#", "......#", "...###.", "...#...", ".......", "...#..."],
    "bitwise_not": [".......", ".......", "#######", "......#", "......#", ".......", "......."],
    "not": word("NOT"),
    "power": ["...#...", "..#.#..", ".#...#.", "#.....#", ".......", ".......", "......."],
    "negate": MINUS,
    "multiply": ["...#...", "#..#..#", ".#.#.#.", "..###..", ".#.#.#.", "#..#..#", "...#..."],
    "divide": SLASH,
    "modulus": mirrored(SLASH),
    "add": ["...#...", "...#...", "...#...", "#######", "...#...", "...#...", "...#..."],
    "subtract": MINUS,
    "shift_left": LEFT,
    "shift_right": mirrored(LEFT),
    "concat": ["..###..", ".#.#.#.", "#..#..#", "#######", "#..#..#", ".#.#.#.", "..###.."],
    "equal": [".......", ".......", "#######", ".......", "#######", ".......", "......."],
    "not_equal": [".....#.", "....#..", "#######", "...#...", "#######", "..#....", ".#....."],
    "less": LESS,
    "greater": mirrored(LESS),
    "less_or_equal": LESS_OR_EQUAL,
    "greater_or_equal": mirrored(LESS_OR_EQUAL),
    "bitwise_and": [".##....", "#..#...", "#..#...", ".##..#.", "#..##..", "#...#..", ".###.#."],
    "bitwise_xor": ["#.....#", "#.....#", ".#...#.", ".#...#.", "..#.#..", "...#...", "#######"],
    "bitwise_or": ["...#..."] * 7,
    "and": word("AND"),
    "xor": word("XOR"),
    "or": word("OR"),
    "open": OPEN,
    "close": mirrored(OPEN),
    "end_of_expression": [".......", "...##..", "...##..", ".......", "...##..", "...##..", "..##..."],
}

def slashes() -> list:
    """The two slashes of a COMMENT: each 2 pixels wide, together almost as wide and deep as the block."""
    rows = []
    for r in range(14):
        row = ["."] * 14
        start = round(5 - r * 5 / 13)
        for offset in (0, 7):
            for c in (start + offset, start + offset + 1):
                row[c] = "#"
        rows.append("".join(row))
    return rows


# The height of the extruded operator symbols
OPERATOR_HEIGHT = 4


def glyph_elements(glyph: list) -> list:
    """The pixels of a glyph as cuboids (one per run of pixels in a row), scaled to fit 14 x 14 pixels."""
    height, width = len(glyph), len(glyph[0])
    size = min(14 / width, 14 / height, 2)
    left = (16 - width * size) / 2
    top = (16 - height * size) / 2
    faces = dict({side: "#side" for side in SIDES_4}, up="#top", down="#top")
    elements = []
    for r, row in enumerate(glyph):
        c = 0
        while c < width:
            if row[c] != "#":
                c += 1
                continue
            start = c
            while c < width and row[c] == "#":
                c += 1
            elements.append(element([left + start * size, 0, top + r * size],
                                    [left + c * size, OPERATOR_HEIGHT, top + (r + 1) * size], faces))
    return elements


def slab(top: str, side: str, transparent: bool = False, bottom: str | None = None) -> dict:
    model = {"parent": "minecraft:block/slab", "textures": {"bottom": bottom or top, "top": top, "side": side}}
    if transparent:
        # NeoForge reads the render type from the model; Fabric sets it in the client entry point
        model["render_type"] = "minecraft:cutout"
    return model


def shaped(elements: list, textures: dict) -> dict:
    textures = dict(textures)
    textures.setdefault("particle", next(iter(textures.values())))
    return {"parent": "minecraft:block/block", "ambientocclusion": False, "textures": textures, "elements": elements}


def texture(name: str) -> str:
    return "c0de:block/" + name


def block_model(name: str, bid: str, transparent: bool) -> dict:
    """The model of a language block (not OPERATOR, see operator_models)."""
    if name in ("PARAMETER", "RESULT"):
        elements = scaled(hopper(), 0.5)
        if name == "RESULT":
            elements = flipped(elements, SLAB)
        return shaped(elements, {n: texture("parameter_block_" + n) for n in ("outside", "top", "inside")})
    if name == "VARIABLE":
        return shaped(chest(), {n: texture("variable_block_" + n) for n in ("side", "top", "front", "latch")})
    if name == "CONSTANT":
        return shaped(barrel(), {n: texture("constant_block_" + n) for n in ("side", "top")})
    if name in ("BEGIN", "END"):
        return slab(texture(bid), texture("begin_block_side"))
    if name == "PROGRAM":
        # The book needs the cutout render type for its transparent pixels
        return slab(texture(bid), texture(bid + "_side"), True, "minecraft:item/book")
    if name == "INCLUDE":
        return shaped([
            element([0, 0, 0], [16, SLAB, 16], dict({side: "#side" for side in SIDES_4}, up="#top", down="#top")),
            # The bookshelf lies in an invisible item frame on the top
            element([2, SLAB, 2], [14, SLAB + 1, 14], dict({side: "#planks" for side in SIDES_4}, up="#shelf"),
                    {"up": FULL}),
        ], {"side": texture(bid + "_side"), "top": texture(bid), "shelf": "minecraft:block/bookshelf",
            "planks": "minecraft:block/oak_planks"})
    if name == "COMMENT":
        return shaped(glyph_elements(slashes()), {"top": texture("comment_block"), "side": texture("comment_block_side")})
    if name in ("IF", "ELSE"):
        return slab(texture(bid), texture("if_block_side"))
    return slab(texture(bid), texture(bid + "_side"), transparent)


def operator_models() -> dict:
    """The models of the operator symbols by the value of the block state operator."""
    textures = {"top": texture("operator_block"), "side": texture("operator_block_side")}
    return {symbol: shaped(glyph_elements(glyph), textures) for symbol, glyph in GLYPHS.items()}
