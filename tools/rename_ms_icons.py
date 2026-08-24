#!/usr/bin/env python3
"""
Benennt alle neuen myshopi-Icons um: <name>.png -> ms_<name>.png
Aktualisiert:
- icons_change_list.json
- Alle product/*.json Dateien (iconId Feld)
- assets/icons/ und app/src/main/assets/icons/
- all_icons.zip
"""
import json
import shutil
import zipfile
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
ASSET_ROOTS = [
    BASE_DIR / "assets" / "icons",
    BASE_DIR / "app" / "src" / "main" / "assets" / "icons",
]
PRODUCT_ASSET_ROOTS = [
    BASE_DIR / "assets" / "product",
    BASE_DIR / "app" / "src" / "main" / "assets" / "product",
]
NEW_ICON_MIN_VERSION = 3  # changeListVersion >= this = myshopi icons


def read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def rebuild_zip(icons_dir: Path):
    zip_path = icons_dir / "all_icons.zip"
    png_files = sorted(
        [p for p in icons_dir.glob("*.png") if p.is_file()],
        key=lambda p: p.name.lower(),
    )
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for png in png_files:
            zf.write(png, png.name)
    print(f"  Rebuilt all_icons.zip with {len(png_files)} icons -> {zip_path}")


def main():
    # Load icons change list
    cl_path = BASE_DIR / "assets" / "icons" / "icons_change_list.json"
    change_list = read_json(cl_path)

    # Find icons that need renaming (new myshopi icons without ms_ prefix already)
    to_rename = {}  # old_name -> new_name
    for item in change_list:
        icon_id = item["id"]
        if (
            item.get("changeListVersion", 0) >= NEW_ICON_MIN_VERSION
            and not item.get("isDeleted", False)
            and not icon_id.startswith("ms_")
        ):
            new_name = f"ms_{icon_id}"
            to_rename[icon_id] = new_name

    print(f"Icons to rename: {len(to_rename)}")

    # ----------------------------------------------------------------
    # 1. Rename PNG files in both asset directories
    # ----------------------------------------------------------------
    for icons_dir in ASSET_ROOTS:
        renamed = 0
        for old_name, new_name in to_rename.items():
            old_path = icons_dir / old_name
            new_path = icons_dir / new_name
            if old_path.exists():
                shutil.move(str(old_path), str(new_path))
                renamed += 1
            elif new_path.exists():
                pass  # already renamed
            else:
                print(f"  [MISSING] {old_name} in {icons_dir}")
        print(f"  Renamed {renamed} icons in {icons_dir}")

    # ----------------------------------------------------------------
    # 2. Update icons_change_list.json
    # ----------------------------------------------------------------
    # Get max version
    max_version = max(item.get("changeListVersion", 0) for item in change_list)
    delete_version = max_version + 1  # version for "deleted old name" entries

    new_cl = []
    for item in change_list:
        icon_id = item["id"]
        if icon_id in to_rename:
            # Mark old entry as deleted
            new_cl.append({"id": icon_id, "changeListVersion": delete_version, "isDeleted": True})
            # Add new entry with ms_ prefix
            new_cl.append({"id": to_rename[icon_id], "changeListVersion": item["changeListVersion"], "isDeleted": False})
        else:
            new_cl.append(item)

    write_json(cl_path, new_cl)
    # Sync change list to app assets
    app_cl_path = BASE_DIR / "app" / "src" / "main" / "assets" / "icons" / "icons_change_list.json"
    if app_cl_path.parent.exists():
        shutil.copy2(cl_path, app_cl_path)
    print(f"  Updated icons_change_list.json ({len(new_cl)} entries, delete_version={delete_version})")

    # ----------------------------------------------------------------
    # 3. Update all product JSON files (iconId field)
    # ----------------------------------------------------------------
    updated_files = 0
    for product_root in PRODUCT_ASSET_ROOTS:
        if not product_root.exists():
            continue
        for json_file in sorted(product_root.rglob("*.json")):
            if "change_list" in json_file.name:
                continue
            try:
                products = read_json(json_file)
                changed = False
                for product in products:
                    icon_id = product.get("iconId")
                    if icon_id and icon_id in to_rename:
                        product["iconId"] = to_rename[icon_id]
                        changed = True
                if changed:
                    write_json(json_file, products)
                    updated_files += 1
            except Exception as e:
                print(f"  [ERROR] {json_file}: {e}")

    print(f"  Updated {updated_files} product JSON files")

    # ----------------------------------------------------------------
    # 4. Rebuild all_icons.zip in both asset directories
    # ----------------------------------------------------------------
    for icons_dir in ASSET_ROOTS:
        rebuild_zip(icons_dir)

    print(f"\nDone! Renamed {len(to_rename)} icons to ms_ prefix.")
    print("Next: Update GroceryIcon.kt to not tint ms_ icons.")


if __name__ == "__main__":
    main()

