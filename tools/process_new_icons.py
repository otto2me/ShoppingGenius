#!/usr/bin/env python3
"""
Verarbeitet neue Icons:
- Direkt aus Original-JPGs (myshopi/named_images/) konvertieren
- Weißen Hintergrund entfernen (edge-connected flood fill, niedrige Toleranz)
- Abgerundete Ecken anwenden
- In beide Asset-Verzeichnisse kopieren
- all_icons.zip neu erstellen
"""
import json
import re
import shutil
import unicodedata
import zipfile
from collections import deque
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw

BASE_DIR = Path(__file__).resolve().parents[1]
ASSET_ROOTS = [
    BASE_DIR / "assets" / "icons",
    BASE_DIR / "app" / "src" / "main" / "assets" / "icons",
]
SOURCE_DIR = BASE_DIR / "myshopi" / "named_images"
NEW_ICON_MIN_VERSION = 3  # changeListVersion >= this means "newly added by myshopi import"

# Niedrigere Toleranz für Produktfotos: nur nahezu reines Weiß entfernen
TOLERANCE = 10


def icon_slug(file_name: str) -> str:
    stem = Path(file_name).stem.strip().lower()
    stem = (
        stem.replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
    )
    stem = "".join(c for c in unicodedata.normalize("NFKD", stem) if not unicodedata.combining(c))
    stem = re.sub(r"[^a-z0-9]+", "_", stem).strip("_")
    stem = re.sub(r"_+", "_", stem)
    return f"{stem}.png"


def _is_near_white(r, g, b, tolerance=10, gray_tolerance=15):
    return (
        r >= 255 - tolerance
        and g >= 255 - tolerance
        and b >= 255 - tolerance
        and max(r, g, b) - min(r, g, b) <= gray_tolerance
    )


def _remove_edge_connected_light_background(img, tolerance=10):
    pixels = img.load()
    width, height = img.size
    queue = deque()
    visited = set()

    def try_enqueue(x, y):
        if (x, y) in visited:
            return
        r, g, b, a = pixels[x, y]
        if a == 0:
            visited.add((x, y))
            return
        if _is_near_white(r, g, b, tolerance=tolerance):
            visited.add((x, y))
            queue.append((x, y))

    for x in range(width):
        try_enqueue(x, 0)
        try_enqueue(x, height - 1)
    for y in range(height):
        try_enqueue(0, y)
        try_enqueue(width - 1, y)

    neighbors = ((1, 0), (-1, 0), (0, 1), (0, -1))
    while queue:
        x, y = queue.popleft()
        r, g, b, _ = pixels[x, y]
        pixels[x, y] = (r, g, b, 0)
        for dx, dy in neighbors:
            nx, ny = x + dx, y + dy
            if nx < 0 or ny < 0 or nx >= width or ny >= height:
                continue
            if (nx, ny) in visited:
                continue
            nr, ng, nb, na = pixels[nx, ny]
            if na > 0 and _is_near_white(nr, ng, nb, tolerance=tolerance):
                visited.add((nx, ny))
                queue.append((nx, ny))


def _apply_rounded_corners(img, radius_ratio=0.18):
    width, height = img.size
    radius = max(4, int(min(width, height) * radius_ratio))
    rounded_mask = Image.new("L", (width, height), 0)
    draw = ImageDraw.Draw(rounded_mask)
    draw.rounded_rectangle((0, 0, width - 1, height - 1), radius=radius, fill=255)
    alpha = img.getchannel("A")
    img.putalpha(ImageChops.multiply(alpha, rounded_mask))


def process_icon_from_source(source_path: Path, dest_path: Path, tolerance: int = TOLERANCE) -> bool:
    try:
        img = Image.open(source_path)
        if img.mode != "RGBA":
            img = img.convert("RGBA")
        _remove_edge_connected_light_background(img, tolerance=tolerance)
        _apply_rounded_corners(img)
        img.save(dest_path, "PNG")
        return True
    except Exception as e:
        print(f"  [FEHLER] {source_path.name}: {e}")
        return False


def rebuild_zip(icons_dir: Path):
    zip_path = icons_dir / "all_icons.zip"
    png_files = sorted(
        [p for p in icons_dir.glob("*.png") if p.is_file()],
        key=lambda p: p.name.lower(),
    )
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for png in png_files:
            zf.write(png, png.name)
    print(f"  Rebuilt all_icons.zip with {len(png_files)} icons → {zip_path}")


def main():
    # Find new icon IDs from change list
    cl_path = BASE_DIR / "assets" / "icons" / "icons_change_list.json"
    change_list = json.loads(cl_path.read_text(encoding="utf-8"))
    new_icons = {
        item["id"]
        for item in change_list
        if item.get("changeListVersion", 0) >= NEW_ICON_MIN_VERSION
        and not item.get("isDeleted", False)
    }
    print(f"Found {len(new_icons)} new icons to process (changeListVersion >= {NEW_ICON_MIN_VERSION})")
    print(f"Using tolerance={TOLERANCE} (conservative, für Produktfotos)")

    # Build reverse mapping: target PNG name → source JPG
    source_map: dict[str, Path] = {}
    for src in SOURCE_DIR.glob("*"):
        if src.suffix.lower() in (".jpg", ".jpeg", ".png"):
            target_name = icon_slug(src.name)
            source_map[target_name] = src

    primary_icons_dir = ASSET_ROOTS[0]

    ok, fail, missing_src = 0, 0, 0
    for icon_name in sorted(new_icons):
        dest_path = primary_icons_dir / icon_name
        src_path = source_map.get(icon_name)

        if src_path is None:
            # Kein Original gefunden – vorhandenes PNG mit neuer Toleranz neu verarbeiten
            if dest_path.exists():
                try:
                    img = Image.open(dest_path)
                    if img.mode != "RGBA":
                        img = img.convert("RGBA")
                    # Bild bereits verarbeitet – nur Rounded Corners neu anwenden
                    _apply_rounded_corners(img)
                    img.save(dest_path, "PNG")
                    ok += 1
                    print(f"  [OK-reapply] {icon_name} (kein Original, nur Corners)")
                except Exception as e:
                    print(f"  [FEHLER] {icon_name}: {e}")
                    fail += 1
            else:
                print(f"  [MISSING-SRC] {icon_name}")
                missing_src += 1
            continue

        if process_icon_from_source(src_path, dest_path, tolerance=TOLERANCE):
            ok += 1
            print(f"  [OK] {icon_name}  <- {src_path.name}")
        else:
            fail += 1

    print(f"\nProcessed: {ok} OK, {fail} failed, {missing_src} missing source")

    # Sync to secondary asset directories and rebuild zips
    for icons_dir in ASSET_ROOTS:
        if icons_dir == primary_icons_dir:
            rebuild_zip(icons_dir)
            continue
        for icon_name in sorted(new_icons):
            src = primary_icons_dir / icon_name
            dst = icons_dir / icon_name
            if src.exists():
                shutil.copy2(src, dst)
        rebuild_zip(icons_dir)
        print(f"  Synced {len(new_icons)} icons to {icons_dir}")

    print("\nDone!")


if __name__ == "__main__":
    main()

