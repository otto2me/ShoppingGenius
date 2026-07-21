#!/usr/bin/env python3
"""
Skript zum Verarbeiten von DuckDuckGo Icons und automatischem Entfernen von weißen Hintergründen.
"""

from collections import deque
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw


def _is_near_white(r, g, b, tolerance=40, gray_tolerance=40):
    return (
        r >= 255 - tolerance
        and g >= 255 - tolerance
        and b >= 255 - tolerance
        and max(r, g, b) - min(r, g, b) <= gray_tolerance
    )


def _remove_edge_connected_light_background(img, tolerance=40):
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

def convert_white_to_transparent(image_path, tolerance=40, corner_radius_ratio=0.18):
    """
    Konvertiert weiße und hellgraue Pixel in transparent.

    Args:
        image_path: Pfad zur Bilddatei
        tolerance: Toleranzbereich für "weiß" Erkennung (0-255)

    Returns:
        True wenn erfolgreich, False sonst
    """
    try:
        # Bild öffnen
        img = Image.open(image_path)

        # Stelle sicher, dass das Bild RGBA hat (für Transparenz)
        if img.mode != 'RGBA':
            img = img.convert('RGBA')

        _remove_edge_connected_light_background(img, tolerance=tolerance)
        _apply_rounded_corners(img, radius_ratio=corner_radius_ratio)

        # Speichere Bild
        img.save(image_path, 'PNG')
        return True
    except Exception as e:
        print(f"[FEHLER] {image_path}: {e}")
        return False

def process_existing_icons():
    """
    Verarbeitet alle Icons und macht die weißen Hintergründe transparent.
    """
    icons_dir = Path(__file__).parent / "assets" / "icons"

    if not icons_dir.exists():
        print(f"[FEHLER] Ordner nicht gefunden: {icons_dir}")
        return

    # Finde alle PNG-Dateien
    png_files = [f for f in icons_dir.glob("*.png") if f.name not in ["all_icons.zip", "icons_change_list.json"]]

    if not png_files:
        print(f"[FEHLER] Keine PNG-Dateien gefunden")
        return

    print(f"\n{'='*60}")
    print(f"Verarbeite {len(png_files)} Icon-Bilder...")
    print(f"Entferne weisse Hintergruende und mache sie transparent...")
    print(f"{'='*60}\n")

    success_count = 0

    for png_file in sorted(png_files):
        if convert_white_to_transparent(str(png_file)):
            success_count += 1
            print(f"  [OK] {png_file.name}")
        else:
            print(f"  [FEHLER] {png_file.name}")

    print(f"\n{'='*60}")
    print(f"Fertig! {success_count}/{len(png_files)} Bilder verarbeitet.")
    print(f"{'='*60}\n")

if __name__ == "__main__":
    print("Icon Background Remover - DuckDuckGo Icons")
    print("=" * 60)
    process_existing_icons()

