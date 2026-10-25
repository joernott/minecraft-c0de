#!/usr/bin/env python3
"""The logo of c0de (docs/images/logo.svg): the COMMENT block, two green slashes extruded from the floor, seen from
a slight angle, after the picture docs/images/logo.png. The slashes are the voxels of the COMMENT model
(block_models.slashes()), projected from a camera south of the block, a little to the east and above it, and drawn
from back to front: light tops, dark fronts and darker sides.

Writes docs/images/logo.svg and, to compare it with the picture, a rasterized preview to the path given as first
argument (optional). Only the standard library is used.
"""
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "scripts"))

import block_models  # noqa: E402

SIZE = 256

TOP = "#00A800"
FRONT = "#005E00"
SIDE = "#004600"
BACKGROUND = "#000000"

HEIGHT = 6.0  # the height of the slashes: a little taller than in the model, like in the picture
YAW = math.radians(5)  # the camera is a little east of south
PITCH = math.radians(30)  # and above the block
# Where the slashes lie in the picture: the box of the green pixels of docs/images/logo.png
TARGET = (32, 72, 225, 211)


def boxes() -> list[tuple[float, float, float, float]]:
    """The voxels of the slashes as (x0, z0, x1, z1): one box per run of pixels in a row of the glyph."""
    result = []
    for r, row in enumerate(block_models.slashes()):
        c = 0
        while c < len(row):
            if row[c] == "#":
                start = c
                while c < len(row) and row[c] == "#":
                    c += 1
                result.append((start, r, c, r + 1))
            else:
                c += 1
    return result


def project(x: float, y: float, z: float) -> tuple[float, float, float]:
    """Screen x, screen y (down) and the distance to the camera (larger is nearer)."""
    sin_yaw, cos_yaw = math.sin(YAW), math.cos(YAW)
    sin_pitch, cos_pitch = math.sin(PITCH), math.cos(PITCH)
    right = (cos_yaw, 0.0, -sin_yaw)
    up = (-sin_yaw * sin_pitch, cos_pitch, -cos_yaw * sin_pitch)
    toward_camera = (sin_yaw * cos_pitch, sin_pitch, cos_yaw * cos_pitch)
    point = (x - 7, y, z - 7)

    def dot(a, b):
        return sum(p * q for p, q in zip(a, b))

    return dot(point, right), -dot(point, up), dot(point, toward_camera)


def faces() -> list[tuple[str, list[tuple[float, float]]]]:
    """The visible faces of all voxels as (color, polygon in picture coordinates), in drawing order."""
    raw = []
    for x0, z0, x1, z1 in boxes():
        h = HEIGHT
        corners = {
            "top": [(x0, h, z0), (x1, h, z0), (x1, h, z1), (x0, h, z1)],
            "front": [(x0, h, z1), (x1, h, z1), (x1, 0, z1), (x0, 0, z1)],
            "side": [(x1, h, z0), (x1, h, z1), (x1, 0, z1), (x1, 0, z0)],
        }
        depth = project((x0 + x1) / 2, h / 2, (z0 + z1) / 2)[2]
        for name, color in (("side", SIDE), ("front", FRONT), ("top", TOP)):
            raw.append((depth, color, [project(*corner)[:2] for corner in corners[name]]))
    raw.sort(key=lambda face: face[0])
    # Scaled and moved onto the place of the slashes in the picture
    xs = [x for _, _, polygon in raw for x, _ in polygon]
    ys = [y for _, _, polygon in raw for _, y in polygon]
    left, top, right, bottom = TARGET
    scale = min((right - left) / (max(xs) - min(xs)), (bottom - top) / (max(ys) - min(ys)))
    offset_x = (left + right) / 2 - scale * (min(xs) + max(xs)) / 2
    offset_y = (top + bottom) / 2 - scale * (min(ys) + max(ys)) / 2
    return [
        (color, [(offset_x + scale * x, offset_y + scale * y) for x, y in polygon]) for _, color, polygon in raw
    ]


def svg() -> str:
    lines = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{SIZE}" height="{SIZE}" viewBox="0 0 {SIZE} {SIZE}">',
        "  <title>c0de</title>",
        f'  <rect width="{SIZE}" height="{SIZE}" fill="{BACKGROUND}"/>',
        # A thin outline in the color of each face closes the seams that antialiasing leaves between faces
        '  <g stroke-width="0.6" stroke-linejoin="round">',
    ]
    for color, polygon in faces():
        points = " ".join(f"{x:.2f},{y:.2f}" for x, y in polygon)
        lines.append(f'    <polygon points="{points}" fill="{color}" stroke="{color}"/>')
    lines += ["  </g>", "</svg>"]
    return "\n".join(lines) + "\n"


def inside(polygon, px, py) -> bool:
    result = False
    for i in range(len(polygon)):
        (x1, y1), (x2, y2) = polygon[i], polygon[(i + 1) % len(polygon)]
        if (y1 > py) != (y2 > py) and px < x1 + (py - y1) * (x2 - x1) / (y2 - y1):
            result = not result
    return result


def preview(path: Path) -> None:
    import generate_textures

    def rgb(color: str):
        return int(color[1:3], 16), int(color[3:5], 16), int(color[5:7], 16), 255

    image = [[rgb(BACKGROUND)] * SIZE for _ in range(SIZE)]
    for color, polygon in faces():
        xs = [x for x, _ in polygon]
        ys = [y for _, y in polygon]
        for y in range(max(0, int(min(ys))), min(SIZE, int(max(ys)) + 1)):
            for x in range(max(0, int(min(xs))), min(SIZE, int(max(xs)) + 1)):
                if inside(polygon, x + 0.5, y + 0.5):
                    image[y][x] = rgb(color)
    generate_textures.write_png(path, image)


def main() -> None:
    (ROOT / "docs" / "images" / "logo.svg").write_text(svg(), encoding="utf-8", newline="\n")
    if len(sys.argv) > 1:
        preview(Path(sys.argv[1]))


if __name__ == "__main__":
    main()
