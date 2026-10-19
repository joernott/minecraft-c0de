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


def trigger_block() -> Image:
    """Provisional trigger block: glass with copper corners and a golden ball in its center."""
    image = [[rgb(0xD6E8EE) for _ in range(16)] for _ in range(16)]
    for i in range(16):
        for edge in (0, 15):
            image[i][edge] = rgb(0xA0B8C0)
            image[edge][i] = rgb(0xA0B8C0)
    for cx, cy in ((0, 0), (12, 0), (0, 12), (12, 12)):
        for y in range(4):
            for x in range(4):
                image[cy + y][cx + x] = rgb(0xC15A36 if (x + y) % 3 else 0xE77C56)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            distance = dx * dx + dy * dy
            if distance <= 9:
                image[y][x] = rgb(0xFDF55F if dx + dy < -1 else 0xE9B115)
            elif distance <= 12.5:
                image[y][x] = rgb(0xB26411)
    return image


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


def main() -> None:
    blocks = TEXTURES / "block"
    write_png(blocks / "white_c0de_bedrock.png", concrete(0xF5F7F3, "white"))
    write_png(blocks / "light_green_c0de_bedrock.png", concrete(0xD2EBD7, "green"))
    write_png(blocks / "black_c0de_bedrock.png", concrete(0x000000, "black", 0))
    write_png(blocks / "cursor_c0de_bedrock_top.png", cursor_top())
    write_png(blocks / "cursor_c0de_bedrock_side.png", concrete(0x7D7D73, "cursor_side"))
    write_png(blocks / "trigger_block.png", trigger_block())
    write_png(TEXTURES / "item" / "wrench.png", wrench())


if __name__ == "__main__":
    main()
