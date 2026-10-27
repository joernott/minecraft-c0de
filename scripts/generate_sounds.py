"""Generates the sounds of the mod (task 13.3): the disk drive reading or writing a disk, like a 3.5" floppy drive.

The sound is synthesized: the spindle motor spins up and hums, the head seeks with a fast series of stepper clicks
(which buzz like the famous floppy drive music), steps from track to track while it reads, and seeks back. Every
variant uses its own random seed, so that Minecraft can choose between slightly different sounds.

The WAV files are written to build/sounds; the Ogg Vorbis files that Minecraft needs are encoded with ffmpeg or
oggenc, whichever is installed (on the VM: apt install ffmpeg).

Usage: python scripts/generate_sounds.py
"""

import math
import random
import shutil
import struct
import subprocess
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WAV_DIR = ROOT / "build" / "sounds"
OGG_DIR = ROOT / "src" / "main" / "resources" / "assets" / "c0de" / "sounds"
RATE = 44_100
VARIANTS = 2


def click(samples, start, strength, rng):
    """A step of the head: a short noise burst and the ringing of the drive frame, decaying within a few ms."""
    modes = [(rng.uniform(1700, 1900), 0.0016, 0.5), (rng.uniform(2900, 3200), 0.0010, 0.3), (160, 0.004, 0.6)]
    begin = int(start * RATE)
    for i in range(int(0.012 * RATE)):
        if begin + i >= len(samples):
            break
        t = i / RATE
        value = sum(a * math.exp(-t / tau) * math.sin(2 * math.pi * f * t) for f, tau, a in modes)
        if t < 0.0008:
            value += rng.uniform(-0.6, 0.6)
        samples[begin + i] += strength * value


def seek(samples, start, steps, interval, strength, rng):
    """Moves the head over several tracks; returns the time after the last step."""
    for step in range(steps):
        click(samples, start + step * interval, strength * rng.uniform(0.85, 1.0), rng)
    return start + steps * interval


def motor(samples, start, end, rng):
    """The hum of the spindle motor and the rubbing of the disk, with a short spin-up and spin-down."""
    phase = 0.0
    noise = 0.0
    for i in range(int(start * RATE), min(len(samples), int(end * RATE))):
        t = i / RATE
        ramp = min(1.0, (t - start) / 0.12, (end - t) / 0.1)
        # The motor speeds up during the spin-up, then turns at a steady speed
        frequency = 95 * (0.6 + 0.4 * min(1.0, (t - start) / 0.12))
        phase += 2 * math.pi * frequency / RATE
        noise = 0.97 * noise + 0.03 * rng.uniform(-1, 1)
        hum = 0.5 * math.sin(phase) + 0.25 * math.sin(2 * phase) + 0.15 * math.sin(3 * phase)
        samples[i] += 0.12 * ramp * hum + 0.35 * ramp * noise


def disk_access(seed):
    """One access of the drive: spin-up, seek, reading track by track, seek back, spin-down."""
    rng = random.Random(seed)
    length = 1.25
    samples = [0.0] * int(length * RATE)
    motor(samples, 0.0, length, rng)
    # The disk is clamped when the motor starts
    click(samples, 0.01, 1.2, rng)
    t = seek(samples, 0.08, rng.randint(14, 22), 0.0035, 0.6, rng)
    for _ in range(rng.randint(3, 4)):
        t += rng.uniform(0.12, 0.18)
        click(samples, t, 0.8, rng)
    seek(samples, t + 0.12, rng.randint(8, 14), 0.003, 0.6, rng)
    peak = max(abs(s) for s in samples)
    return [0.8 * s / peak for s in samples]


def write_wav(path, samples):
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b"".join(struct.pack("<h", int(s * 32767)) for s in samples))


def encode(wav, ogg):
    """Encodes a mono Ogg Vorbis file, which Minecraft plays positioned at the drive."""
    ogg.parent.mkdir(parents=True, exist_ok=True)
    if shutil.which("ffmpeg"):
        command = ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libvorbis", "-q:a", "5", str(ogg)]
    elif shutil.which("oggenc"):
        command = ["oggenc", "-Q", "-q", "5", "-o", str(ogg), str(wav)]
    else:
        raise SystemExit("Neither ffmpeg nor oggenc is installed; the WAV files are in " + str(WAV_DIR))
    subprocess.run(command, check=True)


def main():
    for variant in range(1, VARIANTS + 1):
        wav = WAV_DIR / f"disk_drive{variant}.wav"
        write_wav(wav, disk_access(variant))
        encode(wav, OGG_DIR / f"disk_drive{variant}.ogg")
        print(OGG_DIR / f"disk_drive{variant}.ogg")


if __name__ == "__main__":
    main()
