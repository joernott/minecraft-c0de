"""Exports the blocks of the mod as 3D models (glTF 2.0) for the website c0de.at (task 14.7).

The website shows the example programs of its tutorials as 3D models of the sheet. This script turns the block
models of the mod into glTF files, so the website can build those scenes:

  - Block models (JSON) become meshes, including their parents of Minecraft (cube, slab, hopper, orientable).
  - The language blocks with diagonal or round sides get the shapes of ExtrudedModel (client), which JSON models
    can't describe: trapezoids, octagon, triangle, diamonds, cylinders and the round PROGRAM block, with their
    symbols lying on the top.
  - The textures of the mod are copied. Textures of Minecraft (the bookshelf of INCLUDE, the items on the type
    blocks, the iron of the display) are not ours to publish: they are replaced by simple stand-ins drawn here.

The output in build/models/ is the content of c0de-models-<version>.zip (task 14.8):

  index.json            the blocks and their block states, the models and the layout of a sheet
  models/<model>.gltf   one model per block model, with its buffer embedded
  textures/...          the textures (PNG, 16 x 16 pixels; draw them with nearest-neighbor filtering)

One block is one unit; the origin of a model is the lower north-west corner of its block; x points east, y up and
z south, like in Minecraft.

Usage: python scripts/export_models.py [--zip]
"""

import base64
import json
import math
import re
import shutil
import struct
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import block_models  # noqa: E402
import generate_textures as tex  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "c0de"
# The models of the language blocks of the committed node (1.21.1); newer versions only add a model loader
VERSIONED_MODELS = ROOT / "src" / "main" / "versioned" / "block-models" / "1.21.1" / "assets" / "c0de" / "models"
OUT = ROOT / "build" / "models"

# --------------------------------------------------------------------------------------- parents of Minecraft

SIDES = ("down", "up", "north", "south", "west", "east")


def cube(textures: dict) -> dict:
    return {"textures": textures,
            "elements": [{"from": [0, 0, 0], "to": [16, 16, 16],
                          "faces": {side: {"texture": "#" + side} for side in SIDES}}]}


def hopper() -> dict:
    elements = json.loads(json.dumps(block_models.hopper()).replace("#outside", "#side"))
    return {"elements": elements}


VANILLA = {
    "minecraft:block/block": {},
    "minecraft:block/cube": cube({}),
    "minecraft:block/cube_all": {"parent": "minecraft:block/cube",
                                 "textures": {side: "#all" for side in SIDES}},
    "minecraft:block/cube_bottom_top": {"parent": "minecraft:block/cube",
                                        "textures": dict({s: "#side" for s in SIDES[2:]}, up="#top", down="#bottom")},
    "minecraft:block/orientable": {"parent": "minecraft:block/cube",
                                   "textures": {"up": "#top", "down": "#top", "north": "#front", "south": "#side",
                                                "west": "#side", "east": "#side"}},
    "minecraft:block/slab": {"elements": [{"from": [0, 0, 0], "to": [16, 8, 16],
                                           "faces": dict({s: {"texture": "#side"} for s in SIDES[2:]},
                                                         down={"texture": "#bottom"}, up={"texture": "#top"})}]},
    "minecraft:block/hopper": hopper(),
}

# ------------------------------------------------------------------------- stand-ins for textures of Minecraft

Image = tex.Image


def blank() -> Image:
    return [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]


def iron_block() -> Image:
    image = tex.concrete(0xD8D8D8, "iron", 6)
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            image[y][x] = tex.rgb(0xB0B0B0)
    return image


def planks(seed: str = "planks") -> Image:
    image = tex.concrete(0xA2834F, seed, 8)
    for y in (3, 7, 11, 15):
        for x in range(16):
            image[y][x] = tex.rgb(0x7A5F37)
    return image


def sand(color: int, seed: str) -> Image:
    return tex.concrete(color, seed, 12)


def bookshelf() -> Image:
    image = planks("bookshelf")
    colors = [0x8B2E2E, 0x2E5A8B, 0x3C7A3C, 0xB08A2E, 0x6A3C8B, 0x8B5A2E, 0x2E7A7A]
    for shelf in (1, 9):
        for x in range(1, 15):
            color = colors[(x + shelf) % len(colors)]
            for y in range(shelf, shelf + 6):
                image[y][x] = tex.shade(tex.rgb(color), -10 if x % 2 else 0)
    return image


def barrel_top() -> Image:
    image = planks("barrel")
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 4 <= d < 5.5:
                image[y][x] = tex.rgb(0x5C4428)
    return image


def scaffolding_top() -> Image:
    image = blank()
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i), (i, i), (15 - i, i)):
            image[y][x] = tex.rgb(0xC9A55A)
    return image


def item(rows: list[str], palette: dict[str, int]) -> Image:
    """An item drawn from 16 rows of characters; '.' is transparent."""
    return [[tex.rgb(palette[c]) if c != "." else (0, 0, 0, 0) for c in row] for row in rows]


def redstone_torch() -> Image:
    rows = ["." * 16] * 5 + ["......rr........", "......rr........"] + ["......ss........"] * 7 + ["." * 16] * 2
    return item([row.ljust(16, ".") for row in rows], {"r": 0xD02020, "s": 0x6B4A2B})


def string() -> Image:
    rows = ["." * 16] * 16
    for i in range(3, 13):
        x = 3 + i // 2 + (1 if i % 4 < 2 else 0)
        rows[i] = rows[i][:x] + "w" + rows[i][x + 1:]
    return item(rows, {"w": 0xF0F0F0})


def clock() -> Image:
    image = blank()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6:
                image[y][x] = tex.rgb(0xE8C547 if d >= 4.5 else 0x3A6EA5 if y < 8 else 0x2A2A2A)
    return image


def campfire() -> Image:
    rows = ["." * 16] * 6 + ["......ff........", ".....fyyf.......", "....fyyyyf......", "....fyyyyf......",
                              ".....ffff.......", ".llllllllllll...", "...llllllllllll.", ".llllllllllll..."]
    rows += ["." * 16] * (16 - len(rows))
    return item([row.ljust(16, ".") for row in rows], {"f": 0xE0601A, "y": 0xF6C03A, "l": 0x6B4A2B})


def book() -> Image:
    rows = ["." * 16] * 3 + ["...bbbbbbbbbb..."] + ["...bwwwwwwwwb..."] * 8 + ["...bbbbbbbbbb..."] + ["." * 16] * 3
    return item(rows, {"b": 0x8B4A2A, "w": 0xEDE3C8})


STAND_INS = {
    "minecraft:block/iron_block": iron_block,
    "minecraft:block/oak_planks": planks,
    "minecraft:block/sand": lambda: sand(0xDBCFA3, "sand"),
    "minecraft:block/red_sand": lambda: sand(0xBE6621, "red_sand"),
    "minecraft:block/bookshelf": bookshelf,
    "minecraft:block/barrel_top": barrel_top,
    "minecraft:block/scaffolding_top": scaffolding_top,
    "minecraft:block/redstone_torch": redstone_torch,
    "minecraft:item/string": string,
    "minecraft:item/clock_00": clock,
    "minecraft:item/campfire": campfire,
    "minecraft:item/book": book,
}

# ------------------------------------------------------------------------------------------------- models


def model_file(ref: str) -> Path:
    namespace, path = ref.split(":")
    if namespace != "c0de":
        raise KeyError(ref)
    for base in (VERSIONED_MODELS, ASSETS / "models"):
        file = base / (path + ".json")
        if file.exists():
            return file
    raise FileNotFoundError(ref)


def load(ref: str) -> dict:
    return VANILLA[ref] if ref in VANILLA else json.loads(model_file(ref).read_text(encoding="utf-8"))


def resolve(ref: str) -> tuple[list, dict]:
    """The elements and the textures of a model, with those of its parents."""
    chain = []
    while ref:
        model = load(ref)
        chain.append(model)
        ref = model.get("parent")
    textures, elements = {}, None
    for model in reversed(chain):
        textures.update(model.get("textures", {}))
        if "elements" in model:
            elements = model["elements"]
    return elements or [], textures


def texture_ref(textures: dict, name: str) -> str | None:
    seen = set()
    while name.startswith("#"):
        if name in seen:
            return None
        seen.add(name)
        name = textures.get(name[1:], "")
    return name or None


class Textures:
    """The textures of the export, copied or drawn when a model uses them first."""

    def __init__(self, out: Path):
        self.out = out
        self.written = {}

    def uri(self, ref: str) -> str:
        if ref not in self.written:
            namespace, path = ref.split(":")
            target = self.out / "textures" / namespace / (path + ".png")
            if namespace == "c0de":
                source = ASSETS / "textures" / (path + ".png")
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(source, target)
            elif ref in STAND_INS:
                tex.write_png(target, STAND_INS[ref]())
            else:
                raise KeyError("No stand-in for the texture " + ref)
            self.written[ref] = target.relative_to(self.out).as_posix()
        return self.written[ref]


# Corners of a face as seen from outside: top left, bottom left, bottom right, top right
def face_corners(side: str, a: list, b: list) -> list:
    x1, y1, z1 = a
    x2, y2, z2 = b
    return {
        "north": [(x2, y2, z1), (x2, y1, z1), (x1, y1, z1), (x1, y2, z1)],
        "south": [(x1, y2, z2), (x1, y1, z2), (x2, y1, z2), (x2, y2, z2)],
        "west": [(x1, y2, z1), (x1, y1, z1), (x1, y1, z2), (x1, y2, z2)],
        "east": [(x2, y2, z2), (x2, y1, z2), (x2, y1, z1), (x2, y2, z1)],
        "up": [(x1, y2, z1), (x1, y2, z2), (x2, y2, z2), (x2, y2, z1)],
        "down": [(x1, y1, z2), (x1, y1, z1), (x2, y1, z1), (x2, y1, z2)],
    }[side]


def rotate(point: tuple, rotation: dict | None) -> tuple:
    """The rotation of an element: around its origin, about one axis, optionally rescaled like Minecraft."""
    if not rotation or not rotation.get("angle"):
        return point
    angle = math.radians(rotation["angle"])
    ox, oy, oz = rotation.get("origin", [8, 8, 8])
    x, y, z = point[0] - ox, point[1] - oy, point[2] - oz
    c, s = math.cos(angle), math.sin(angle)
    scale = 1 / c if rotation.get("rescale") else 1
    axis = rotation["axis"]
    if axis == "x":
        y, z = (y * c - z * s), (y * s + z * c)
        y, z = y * scale, z * scale
    elif axis == "y":
        x, z = (x * c + z * s), (-x * s + z * c)
        x, z = x * scale, z * scale
    else:
        x, y = (x * c - y * s), (x * s + y * c)
        x, y = x * scale, y * scale
    return x + ox, y + oy, z + oz


class Mesh:
    """Quads grouped by texture, in pixels (1/16 block) and UV pixels."""

    def __init__(self):
        self.quads = {}

    def add(self, texture: str, corners: list, uvs: list):
        self.quads.setdefault(texture, []).append((corners, uvs))


def element_quads(mesh: Mesh, elements: list, textures: dict):
    for e in elements:
        for side, face in e.get("faces", {}).items():
            ref = texture_ref(textures, face["texture"])
            if ref is None:
                continue
            u1, v1, u2, v2 = face.get("uv") or block_models.auto_uv(e["from"], e["to"], side)
            uvs = [(u1, v1), (u1, v2), (u2, v2), (u2, v1)]
            turns = (face.get("rotation", 0) // 90) % 4
            uvs = [uvs[(i + turns) % 4] for i in range(4)]
            corners = [rotate(p, e.get("rotation")) for p in face_corners(side, e["from"], e["to"])]
            mesh.add(ref, corners, uvs)


# ExtrudedModel: the polygons of the language blocks with diagonal or round sides (corners in pixels, x and z)
ROUND_EDGES = 12
HEIGHT = 8
TRAPEZOID_INSET = 4
OCTAGON_CUT = 16 / (2 + math.sqrt(2))
SQUARE = [(0, 0), (0, 16), (16, 16), (16, 0)]


def half_round() -> list:
    points = [(8 + 8 * math.cos(math.pi * 1.5 - math.pi * k / ROUND_EDGES),
               8 + 8 * math.sin(math.pi * 1.5 - math.pi * k / ROUND_EDGES)) for k in range(ROUND_EDGES + 1)]
    return points + [(16, 16), (16, 0)]


def circle() -> list:
    edges = 2 * ROUND_EDGES
    return [(8 + 8 * math.cos(math.pi * 1.5 - 2 * math.pi * k / edges),
             8 + 8 * math.sin(math.pi * 1.5 - 2 * math.pi * k / edges)) for k in range(edges)]


def octagon() -> list:
    c = OCTAGON_CUT
    return [(c, 0), (0, c), (0, 16 - c), (c, 16), (16 - c, 16), (16, 16 - c), (16, c), (16 - c, 0)]


def polygon(path: str) -> list | None:
    """Like ExtrudedModel.polygon: the shape of a block model, or None if it keeps its JSON model."""
    shapes = {
        "begin_block": [(TRAPEZOID_INSET, 0), (0, 16), (16, 16), (16 - TRAPEZOID_INSET, 0)],
        "end_block": [(0, 0), (TRAPEZOID_INSET, 16), (16 - TRAPEZOID_INSET, 16), (16, 0)],
        "program_block": half_round(),
        "literal_block": octagon(),
        "assign_block": [(0, 0), (0, 16), (16, 8)],
        "if_block": [(8, 0), (0, 8), (8, 16), (16, 8)],
        "else_block": [(8, 0), (0, 8), (8, 16), (16, 8)],
    }
    if path in shapes:
        return shapes[path]
    if path in ("for_block", "each_block", "while_block", "repeat_block"):
        return circle()
    if path in ("tag_block", "exit_block", "break_block", "function_block", "return_block", "call_block") or (
            path.startswith("type_") and path.endswith("_block")):
        return SQUARE
    return None


ITEM_SIZE = 10
ITEM_THICKNESS = 0.5


def extruded_quads(mesh: Mesh, shape: list, textures: dict):
    top = texture_ref(textures, "#top")
    side = texture_ref(textures, "#side")
    symbol = texture_ref(textures, "#bottom")
    n = len(shape)
    for i in range(1, n - 1):
        triangle = [shape[0], shape[i], shape[i + 1], shape[i + 1]]
        mesh.add(top, [(x, HEIGHT, z) for x, z in triangle], [(x, z) for x, z in triangle])
        reversed_triangle = [shape[0], shape[i + 1], shape[i + 1], shape[i]]
        mesh.add(top, [(x, 0, z) for x, z in reversed_triangle], [(x, 16 - z) for x, z in reversed_triangle])
    cx = sum(p[0] for p in shape) / n
    cz = sum(p[1] for p in shape) / n
    for i in range(n):
        a, b = shape[i], shape[(i + 1) % n]
        nx, nz = b[1] - a[1], a[0] - b[0]
        if nx * ((a[0] + b[0]) / 2 - cx) + nz * ((a[1] + b[1]) / 2 - cz) < 0:
            nx, nz = -nx, -nz
        face = ("east" if nx > 0 else "west") if abs(nx) >= abs(nz) else ("south" if nz > 0 else "north")
        left_x, left_z = -nz, nx
        left = a if a[0] * left_x + a[1] * left_z >= b[0] * left_x + b[1] * left_z else b
        right = b if left is a else a

        def u(p):
            if face in ("north", "south"):
                return p[0] if face == "south" else 16 - p[0]
            return p[1] if face == "west" else 16 - p[1]

        corners = [(left[0], HEIGHT, left[1]), (left[0], 0, left[1]), (right[0], 0, right[1]),
                   (right[0], HEIGHT, right[1])]
        uvs = [(u(left), 16 - HEIGHT), (u(left), 16), (u(right), 16), (u(right), 16 - HEIGHT)]
        mesh.add(side, corners, uvs)
    if symbol and symbol != top:
        size = ITEM_SIZE if symbol.startswith("minecraft:") else 16
        x0 = (16 - size) / 2
        y = HEIGHT + ITEM_THICKNESS
        corners = [(x0, y, x0), (x0, y, x0 + size), (x0 + size, y, x0 + size), (x0 + size, y, x0)]
        mesh.add(symbol, corners, [(0, 0), (0, 16), (16, 16), (16, 0)])


def build(path: str) -> Mesh:
    ref = "c0de:block/" + path
    elements, textures = resolve(ref)
    mesh = Mesh()
    shape = polygon(path)
    if shape is not None:
        extruded_quads(mesh, shape, textures)
    else:
        element_quads(mesh, elements, textures)
    if not mesh.quads:
        raise ValueError("The model " + ref + " has no geometry")
    return mesh


def gltf(mesh: Mesh, textures: Textures) -> dict:
    """A glTF document with one mesh: a primitive per texture, the buffer embedded as data URI."""
    data = bytearray()
    views, accessors, primitives, materials, images, gl_textures = [], [], [], [], [], []

    def add_view(payload: bytes, target: int) -> int:
        while len(data) % 4:
            data.append(0)
        views.append({"buffer": 0, "byteOffset": len(data), "byteLength": len(payload), "target": target})
        data.extend(payload)
        return len(views) - 1

    def add_accessor(view: int, component: int, count: int, kind: str, **extra) -> int:
        accessors.append(dict({"bufferView": view, "componentType": component, "count": count, "type": kind}, **extra))
        return len(accessors) - 1

    for ref, quads in mesh.quads.items():
        positions, normals, uvs, indices = [], [], [], []
        for corners, quad_uvs in quads:
            p = [(x / 16, y / 16, z / 16) for x, y, z in corners]
            # The normal of the quad; a triangle with a repeated corner takes the other edge
            normal = (0.0, 0.0, 0.0)
            for i in range(4):
                a, b, c = p[i], p[(i + 1) % 4], p[(i + 2) % 4]
                u = [b[k] - a[k] for k in range(3)]
                v = [c[k] - b[k] for k in range(3)]
                n = (u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])
                length = math.sqrt(sum(k * k for k in n))
                if length > 1e-9:
                    normal = tuple(k / length for k in n)
                    break
            base = len(positions)
            positions.extend(p)
            normals.extend([normal] * 4)
            uvs.extend([(u / 16, v / 16) for u, v in quad_uvs])
            indices.extend([base, base + 1, base + 2, base, base + 2, base + 3])
        count = len(positions)
        mins = [min(p[k] for p in positions) for k in range(3)]
        maxs = [max(p[k] for p in positions) for k in range(3)]
        pos = add_accessor(add_view(struct.pack("<%df" % (3 * count), *[c for p in positions for c in p]), 34962),
                           5126, count, "VEC3", min=mins, max=maxs)
        nor = add_accessor(add_view(struct.pack("<%df" % (3 * count), *[c for p in normals for c in p]), 34962),
                           5126, count, "VEC3")
        tc = add_accessor(add_view(struct.pack("<%df" % (2 * count), *[c for p in uvs for c in p]), 34962),
                          5126, count, "VEC2")
        idx = add_accessor(add_view(struct.pack("<%dH" % len(indices), *indices), 34963), 5123, len(indices),
                           "SCALAR")
        images.append({"uri": "../" + textures.uri(ref)})
        gl_textures.append({"sampler": 0, "source": len(images) - 1})
        materials.append({"name": ref, "pbrMetallicRoughness": {"baseColorTexture": {"index": len(gl_textures) - 1},
                                                               "metallicFactor": 0, "roughnessFactor": 1},
                          "alphaMode": "MASK", "alphaCutoff": 0.5})
        primitives.append({"attributes": {"POSITION": pos, "NORMAL": nor, "TEXCOORD_0": tc}, "indices": idx,
                           "material": len(materials) - 1})
    return {
        "asset": {"version": "2.0", "generator": "c0de scripts/export_models.py"},
        "scene": 0,
        "scenes": [{"nodes": [0]}],
        "nodes": [{"mesh": 0}],
        "meshes": [{"primitives": primitives}],
        "materials": materials,
        "textures": gl_textures,
        "images": images,
        # Nearest-neighbor filtering keeps the pixels of the textures sharp
        "samplers": [{"magFilter": 9728, "minFilter": 9728, "wrapS": 33071, "wrapT": 33071}],
        "buffers": [{"byteLength": len(data),
                     "uri": "data:application/octet-stream;base64," + base64.b64encode(bytes(data)).decode()}],
        "bufferViews": views,
        "accessors": accessors,
    }


def mod_version() -> str:
    text = (ROOT / "stonecutter.properties.toml").read_text(encoding="utf-8")
    return re.search(r'^mod\.version = "([^"]+)"', text, re.MULTILINE).group(1)


def registered_blocks() -> set[str]:
    """The IDs of the blocks the mod registers (C0deBlocks and one per constant of CodeBlockType)."""
    content = ROOT / "src" / "main" / "java" / "de" / "joernott" / "c0de" / "content"
    blocks = set(re.findall(r'register\(\s*"([a-z0-9_]+)"', (content / "C0deBlocks.java").read_text(encoding="utf-8")))
    types = (content / "CodeBlockType.java").read_text(encoding="utf-8")
    blocks.update(name.lower() + "_block" for name in re.findall(r"^    ([A-Z_]+)\(", types, re.MULTILINE))
    return blocks


def export(out: Path = OUT) -> dict:
    """Writes the models, the textures and the index; returns the index.

    Fails if a registered block has no block state, or a block state no model with geometry.
    """
    states = {file.stem for file in (ASSETS / "blockstates").glob("*.json")}
    missing = registered_blocks() - states
    if missing:
        raise ValueError("Blocks without block states: " + ", ".join(sorted(missing)))
    if out.exists():
        shutil.rmtree(out)
    textures = Textures(out)
    written = {}
    blocks = {}
    for state_file in sorted((ASSETS / "blockstates").glob("*.json")):
        variants = json.loads(state_file.read_text(encoding="utf-8")).get("variants")
        if not variants:
            raise ValueError(state_file.name + " has no variants")
        states = {}
        for key, variant in variants.items():
            variant = variant[0] if isinstance(variant, list) else variant
            path = variant["model"].split(":")[1].removeprefix("block/")
            if path not in written:
                document = gltf(build(path), textures)
                target = out / "models" / (path + ".gltf")
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(json.dumps(document, separators=(",", ":")), encoding="utf-8")
                written[path] = "models/" + path + ".gltf"
            state = {"model": written[path]}
            if variant.get("y"):
                state["y"] = variant["y"]
            states[key] = state
        first = next(iter(states.values()))
        entry = {"model": first["model"]}
        if len(states) > 1 or "" not in states:
            entry["states"] = states
        blocks["c0de:" + state_file.stem] = entry
    index = {
        "version": mod_version(),
        "units": "1 unit is 1 block; the origin of a model is the lower north-west corner of its block; x points "
                 "east, y up, z south. Block states with y rotate the model clockwise around the center of the "
                 "block, seen from above.",
        "blocks": blocks,
        "sheet": {
            "width": 32,
            "floor": "At relative x 4 c0de:cursor_c0de_bedrock; at x 0 and 31 c0de:black_c0de_bedrock; in the "
                     "even lines (z % 2 == 0) c0de:white_c0de_bedrock with c0de:black_c0de_bedrock at x 2 and 29; "
                     "in the odd lines c0de:light_green_c0de_bedrock. The floor is at y -1, the blocks at y 0.",
            "cursor_x": 4,
            "code_x": [5, 28],
            "line_number_x": 2,
            "line_number_colors": {"valid": "#00AA00", "incomplete": "#B0A000", "invalid": "#AA0000",
                                   "neutral_on_black": "#2A2A2A", "neutral_on_green": "#B4CCB9"},
        },
    }
    (out / "index.json").write_text(json.dumps(index, indent=2) + "\n", encoding="utf-8")
    return index


def main() -> None:
    index = export()
    print(f"Exported {len(index['blocks'])} blocks to {OUT.relative_to(ROOT)}")
    if "--zip" in sys.argv:
        archive = ROOT / "build" / f"c0de-models-{index['version']}.zip"
        with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as zip_file:
            for file in sorted(OUT.rglob("*")):
                if file.is_file():
                    zip_file.write(file, file.relative_to(OUT).as_posix())
        print(f"Wrote {archive.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
