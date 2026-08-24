#!/usr/bin/env python3
import json
import re
import unicodedata
import zipfile
from dataclasses import dataclass
from urllib.parse import quote
from urllib.request import urlopen
from pathlib import Path
from typing import Dict, List, Tuple

import pandas as pd
from PIL import Image


BASE_DIR = Path(__file__).resolve().parents[1]
EXCEL_PATH = BASE_DIR / "myshopi" / "named_images_listing_cleanup.xlsx"
IMAGE_SOURCE_DIR = BASE_DIR / "myshopi" / "named_images"
ASSET_ROOTS = [
    BASE_DIR / "assets",
    BASE_DIR / "app" / "src" / "main" / "assets",
]


@dataclass
class ProductSeed:
    german_name: str
    category_label: str
    file_name: str


@dataclass
class ProductEntry:
    product_id: str
    german_name: str
    category_id: str
    icon_id: str
    source_file_name: str


def read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def normalize(text: str) -> str:
    text = str(text).strip().lower()
    text = (
        text.replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replace("&", " und ")
    )
    text = "".join(c for c in unicodedata.normalize("NFKD", text) if not unicodedata.combining(c))
    return re.sub(r"[^a-z0-9]+", "", text)


def slugify(text: str) -> str:
    text = str(text).strip().lower()
    text = (
        text.replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replace("&", " and ")
    )
    text = "".join(c for c in unicodedata.normalize("NFKD", text) if not unicodedata.combining(c))
    text = re.sub(r"[^a-z0-9]+", "-", text).strip("-")
    return re.sub(r"-+", "-", text)


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


def chunked(items: List[str], size: int) -> List[List[str]]:
    return [items[i : i + size] for i in range(0, len(items), size)]


def locale_to_lang(locale: str) -> str:
    locale = locale.strip()
    if locale == "zh_CN":
        return "zh-CN"
    if locale == "zh_TW":
        return "zh-TW"
    if locale.startswith("pt_"):
        return "pt"
    return locale.split("_")[0].lower()


def detect_target(path: Path, kind: str) -> str:
    if kind == "category":
        if path.name == "categories_en.json":
            return "en"
        if path.name == "categories.json" and path.parent.name == "category":
            return "en"
    if kind == "product":
        if path.name == "default_products_en.json":
            return "en"
        if path.name == "default_products.json" and path.parent.name == "product":
            return "en"

    locale = path.parent.name
    if locale.startswith("de"):
        return "de"
    if locale.startswith("en"):
        return "en"
    return locale_to_lang(locale)


class Translator:
    def __init__(self):
        self.cache: Dict[Tuple[str, str], str] = {}

    def _google_batch(self, texts: List[str], target: str) -> List[str]:
        joined = "\n".join(texts)
        url = (
            "https://translate.googleapis.com/translate_a/single"
            f"?client=gtx&sl=de&tl={quote(target)}&dt=t&q={quote(joined)}"
        )
        with urlopen(url, timeout=12) as response:
            payload = json.loads(response.read().decode("utf-8"))

        translated = "".join(part[0] for part in payload[0])
        parts = translated.split("\n")
        if len(parts) != len(texts):
            # If line splitting fails, keep one-to-one fallback behavior.
            return [translated if i == 0 else texts[i] for i in range(len(texts))]
        return parts

    def _translate_single(self, text: str, target: str) -> str:
        result = self.translate_many([text], target)
        return result.get(text, text)

    def translate_many(self, texts: List[str], target: str) -> Dict[str, str]:
        unique_texts = list(dict.fromkeys(texts))
        if target == "de":
            return {t: t for t in unique_texts}

        result: Dict[str, str] = {}
        if not unique_texts:
            return result

        # English batch first, used as fallback for any target errors.
        en_map: Dict[str, str] = {}
        if target != "en":
            en_map = self.translate_many(unique_texts, "en")

        for part in chunked(unique_texts, 40):
            uncached = [t for t in part if (target, t) not in self.cache]
            for src in part:
                if (target, src) in self.cache:
                    result[src] = self.cache[(target, src)]

            if not uncached:
                continue

            try:
                translated = self._google_batch(uncached, target)
                for src, dst in zip(uncached, translated):
                    value = (dst or "").strip() or en_map.get(src, src)
                    self.cache[(target, src)] = value
                    result[src] = value
            except Exception:
                for src in uncached:
                    value = en_map.get(src, src)
                    self.cache[(target, src)] = value
                    result[src] = value
        return result


def collect_excel_data() -> Tuple[List[str], Dict[str, ProductSeed]]:
    df = pd.read_excel(EXCEL_PATH)
    rows = df[["screenshot_category", "screenshot_name", "file_name"]].dropna()

    categories: List[str] = []
    product_by_norm: Dict[str, ProductSeed] = {}

    for _, row in rows.iterrows():
        category = str(row["screenshot_category"]).strip()
        name = str(row["screenshot_name"]).strip()
        file_name = str(row["file_name"]).strip()
        if not category or not name or not file_name:
            continue

        categories.append(category)

        key = normalize(name)
        candidate = ProductSeed(name, category, file_name)
        existing = product_by_norm.get(key)
        if existing is None:
            product_by_norm[key] = candidate
        else:
            # Prefer a cleaner base file name without numeric suffixes.
            old_score = ("_" in Path(existing.file_name).stem, len(existing.file_name))
            new_score = ("_" in Path(candidate.file_name).stem, len(candidate.file_name))
            if new_score < old_score:
                product_by_norm[key] = candidate

    return sorted(set(categories)), product_by_norm


def sync_catalog(asset_root: Path, all_categories: List[str], product_seeds: Dict[str, ProductSeed], translator: Translator):
    category_root = asset_root / "category"
    product_root = asset_root / "product"
    icons_root = asset_root / "icons"

    de_category_file = category_root / "de_DE" / "categories.json"
    de_product_file = product_root / "de_DE" / "default_products.json"

    if not de_category_file.exists() or not de_product_file.exists() or not icons_root.exists():
        return {
            "asset_root": str(asset_root),
            "added_categories": 0,
            "added_products": 0,
            "added_icons": 0,
        }

    de_categories = read_json(de_category_file)
    de_products = read_json(de_product_file)

    category_id_by_norm = {normalize(item["name"]): item["id"] for item in de_categories}
    existing_category_ids = {item["id"] for item in de_categories}
    max_sort = max(item.get("sortingPriority", 0) for item in de_categories)

    new_categories: List[dict] = []
    category_id_for_label: Dict[str, str] = {}

    for label in all_categories:
        key = normalize(label)
        if key in category_id_by_norm:
            category_id_for_label[label] = category_id_by_norm[key]
            continue

        category_id = f"default-category-{slugify(label)}"
        suffix = 2
        while category_id in existing_category_ids:
            category_id = f"default-category-{slugify(label)}-{suffix}"
            suffix += 1

        max_sort += 1
        item = {
            "id": category_id,
            "name": label,
            "sortingPriority": max_sort,
        }
        new_categories.append(item)
        existing_category_ids.add(category_id)
        category_id_by_norm[key] = category_id
        category_id_for_label[label] = category_id

    for label in all_categories:
        if label not in category_id_for_label:
            category_id_for_label[label] = category_id_by_norm[normalize(label)]

    existing_product_name_norm = {normalize(item["name"]) for item in de_products}
    existing_product_ids = {item["id"] for item in de_products}

    new_products: List[ProductEntry] = []
    all_icon_ids = {p.get("iconId", "") for p in de_products}

    for key, seed in sorted(product_seeds.items(), key=lambda kv: kv[1].german_name.lower()):
        if key in existing_product_name_norm:
            continue

        category_id = category_id_for_label.get(seed.category_label)
        if not category_id:
            continue

        product_id = f"default-product-{slugify(seed.german_name)}"
        suffix = 2
        while product_id in existing_product_ids:
            product_id = f"default-product-{slugify(seed.german_name)}-{suffix}"
            suffix += 1

        icon_id = icon_slug(seed.file_name)
        if not icon_id:
            continue

        new_products.append(
            ProductEntry(
                product_id=product_id,
                german_name=seed.german_name,
                category_id=category_id,
                icon_id=icon_id,
                source_file_name=seed.file_name,
            )
        )
        existing_product_ids.add(product_id)
        all_icon_ids.add(icon_id)

    category_targets = [
        category_root / "categories.json",
        category_root / "categories_en.json",
    ] + sorted(category_root.glob("*/categories.json"))

    product_targets = [
        product_root / "default_products.json",
        product_root / "default_products_en.json",
    ] + sorted(product_root.glob("*/default_products.json"))

    category_texts = [c["name"] for c in new_categories]
    product_texts = [p.german_name for p in new_products]

    for path in category_targets:
        if not path.exists():
            continue
        target = detect_target(path, "category")
        names = translator.translate_many(category_texts, target)

        items = read_json(path)
        id_set = {item["id"] for item in items}
        for c in new_categories:
            if c["id"] in id_set:
                continue
            items.append(
                {
                    "id": c["id"],
                    "name": names.get(c["name"], c["name"]),
                    "sortingPriority": c["sortingPriority"],
                }
            )
            id_set.add(c["id"])
        items.sort(key=lambda x: x["id"])
        write_json(path, items)

    for path in product_targets:
        if not path.exists():
            continue
        target = detect_target(path, "product")
        names = translator.translate_many(product_texts, target)

        items = read_json(path)
        id_set = {item["id"] for item in items}
        for p in new_products:
            if p.product_id in id_set:
                continue
            items.append(
                {
                    "id": p.product_id,
                    "name": names.get(p.german_name, p.german_name),
                    "iconId": p.icon_id,
                    "categoryId": p.category_id,
                    "isDefault": True,
                }
            )
            id_set.add(p.product_id)
        items.sort(key=lambda x: x["id"])
        write_json(path, items)

    added_icons = 0
    for p in new_products:
        src = IMAGE_SOURCE_DIR / p.source_file_name
        dst = icons_root / p.icon_id
        if not src.exists() or dst.exists():
            continue
        with Image.open(src) as img:
            if img.mode not in ("RGB", "RGBA"):
                img = img.convert("RGBA" if "A" in img.getbands() else "RGB")
            img.save(dst, format="PNG")
        added_icons += 1

    icons_change_path = icons_root / "icons_change_list.json"
    if icons_change_path.exists():
        icons_change = read_json(icons_change_path)
        existing_ids = {item["id"] for item in icons_change}
        current_version = max((item.get("changeListVersion", 0) for item in icons_change), default=0)
        next_version = current_version + 1
        for p in new_products:
            if p.icon_id not in existing_ids:
                icons_change.append(
                    {
                        "id": p.icon_id,
                        "changeListVersion": next_version,
                        "isDeleted": False,
                    }
                )
                existing_ids.add(p.icon_id)
        icons_change.sort(key=lambda x: x["id"])
        write_json(icons_change_path, icons_change)

    products_change_path = product_root / "default_products_change_list.json"
    if products_change_path.exists():
        changes = read_json(products_change_path)
        existing_ids = {item["id"] for item in changes}
        current_version = max((item.get("changeListVersion", 0) for item in changes), default=0)
        next_version = current_version + 1
        for p in new_products:
            if p.product_id not in existing_ids:
                changes.append(
                    {
                        "id": p.product_id,
                        "changeListVersion": next_version,
                        "isDeleted": False,
                    }
                )
                existing_ids.add(p.product_id)
        changes.sort(key=lambda x: x["id"])
        write_json(products_change_path, changes)

    categories_change_path = category_root / "categories_change_list.json"
    if categories_change_path.exists() and new_categories:
        changes = read_json(categories_change_path)
        existing_ids = {item["id"] for item in changes}
        current_version = max((item.get("changeListVersion", 0) for item in changes), default=0)
        next_version = current_version + 1
        for c in new_categories:
            if c["id"] not in existing_ids:
                changes.append(
                    {
                        "id": c["id"],
                        "changeListVersion": next_version,
                        "isDeleted": False,
                    }
                )
                existing_ids.add(c["id"])
        changes.sort(key=lambda x: x["id"])
        write_json(categories_change_path, changes)

    zip_path = icons_root / "all_icons.zip"
    png_files = sorted([p for p in icons_root.glob("*.png") if p.is_file()], key=lambda p: p.name.lower())
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for png in png_files:
            zf.write(png, png.name)

    return {
        "asset_root": str(asset_root),
        "added_categories": len(new_categories),
        "added_products": len(new_products),
        "added_icons": added_icons,
    }


def main():
    categories, product_seeds = collect_excel_data()
    translator = Translator()

    results = []
    for asset_root in ASSET_ROOTS:
        results.append(sync_catalog(asset_root, categories, product_seeds, translator))

    for result in results:
        print(
            f"[{result['asset_root']}] "
            f"categories+={result['added_categories']} "
            f"products+={result['added_products']} "
            f"icons+={result['added_icons']}"
        )


if __name__ == "__main__":
    main()


