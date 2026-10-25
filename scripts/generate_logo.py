#!/usr/bin/env python3
"""The logo of c0de (docs/images/logo.svg): the COMMENT block, two green slashes extruded from the floor, seen from
a slight angle, after the picture docs/images/logo.png. Each slash is a staircase of voxel columns, drawn from back
to front with a light top, a dark front and a darker side.

Writes docs/images/logo.svg and, to compare it with the picture, a rasterized preview to the path given as first
argument (optional). Only the standard library is used.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SIZE = 256

TOP = "#00A600"
FRONT = "#007E00"
SIDE = "#005B00"
BACKGROUND = "#000000"

STEPS = 6
WIDTH = 26.0  # the front of a column
HEIGHT = 48.0  # the height of a column
DEPTH = (8.0, -6.0)  # the top and the side recede to the upper right
STEP = (16.0, -16.0)  # from one column to the next, further back
SLASHES = ((32.0, 162.0), (140.0, 157.0))  # the front lower left corner of the first column of each slash


def columns() -> list[tuple[str, list[tuple[float, float]]]]:
    """The faces of all columns as (color, polygon), in drawing order."""
    faces = []
    for x0, y0 in SLASHES:
        # The columns further back are drawn first
        for k in reversed(range(STEPS)):
            x = x0 + k * STEP[0]
            y = y0 + k * STEP[1]
            dx, dy = DEPTH
            front = [(x, y), (x + WIDTH, y), (x + WIDTH, y + HEIGHT), (x, y + HEIGHT)]
            side = [(x + WIDTH, y), (x + WIDTH + dx, y + dy), (x + WIDTH + dx, y + HEIGHT + dy), (x + WIDTH, y + HEIGHT)]
            top = [(x, y), (x + dx, y + dy), (x + WIDTH + dx, y + dy), (x + WIDTH, y)]
            faces += [(SIDE, side), (FRONT, front), (TOP, top)]
    return faces


def svg() -> str:
    lines = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{SIZE}" height="{SIZE}" viewBox="0 0 {SIZE} {SIZE}">',
        "  <title>c0de</title>",
        f'  <rect width="{SIZE}" height="{SIZE}" fill="{BACKGROUND}"/>',
    ]
    for color, polygon in columns():
        points = " ".join(f"{x:g},{y:g}" for x, y in polygon)
        lines.append(f'  <polygon points="{points}" fill="{color}"/>')
    lines.append("</svg>")
    return "\n".join(lines) + "\n"


def inside(polygon, px, py) -> bool:
    result = False
    for i in range(len(polygon)):
        (x1, y1), (x2, y2) = polygon[i], polygon[(i + 1) % len(polygon)]
        if (y1 > py) != (y2 > py) and px < x1 + (py - y1) * (x2 - x1) / (y2 - y1):
            result = not result
    return result


def preview(path: Path) -> None:
    sys.path.insert(0, str(ROOT / "scripts"))
    import generate_textures

    def rgb(color: str):
        return int(color[1:3], 16), int(color[3:5], 16), int(color[5:7], 16), 255

    image = [[rgb(BACKGROUND)] * SIZE for _ in range(SIZE)]
    for color, polygon in columns():
        for y in range(SIZE):
            for x in range(SIZE):
                if inside(polygon, x + 0.5, y + 0.5):
                    image[y][x] = rgb(color)
    generate_textures.write_png(path, image)


def main() -> None:
    (ROOT / "docs" / "images" / "logo.svg").write_text(svg(), encoding="utf-8", newline="\n")
    if len(sys.argv) > 1:
        preview(Path(sys.argv[1]))


if __name__ == "__main__":
    main()
