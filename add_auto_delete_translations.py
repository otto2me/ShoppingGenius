#!/usr/bin/env python3
"""
Fügt die fehlenden Auto-Delete Übersetzungen zu allen Sprachdateien hinzu.
"""

import os
import re

BASE_DIR = r"C:\Work\Privat\GroceryGenius-develop\app\src\main\res"

# Übersetzungen: lang_code -> (title, description, hours)
TRANSLATIONS = {
    "af-rZA": (
        "Verwyder voltooide items outomaties",
        "Verwyder voltooide items outomaties na %1$d uur.",
        "%1$d uur"
    ),
    "ar-rSA": (
        "الحذف التلقائي للعناصر المكتملة",
        "إزالة العناصر المكتملة تلقائيًا بعد %1$d ساعة.",
        "%1$d ساعة"
    ),
    "ca-rES": (
        "Suprimeix automàticament els elements completats",
        "Elimina automàticament els elements completats després de %1$d hores.",
        "%1$d hores"
    ),
    "cs-rCZ": (
        "Automatické mazání dokončených položek",
        "Automaticky odstraňovat dokončené položky po %1$d hodinách.",
        "%1$d hodin"
    ),
    "da-rDK": (
        "Slet gennemførte varer automatisk",
        "Fjern automatisk gennemførte varer efter %1$d timer.",
        "%1$d timer"
    ),
    "el-rGR": (
        "Αυτόματη διαγραφή ολοκληρωμένων στοιχείων",
        "Αυτόματη κατάργηση ολοκληρωμένων στοιχείων μετά από %1$d ώρες.",
        "%1$d ώρες"
    ),
    "es-rES": (
        "Eliminar automáticamente los artículos completados",
        "Eliminar automáticamente los artículos completados después de %1$d horas.",
        "%1$d horas"
    ),
    "fi-rFI": (
        "Poista valmistuneet tuotteet automaattisesti",
        "Poista valmistuneet tuotteet automaattisesti %1$d tunnin kuluttua.",
        "%1$d tuntia"
    ),
    "fr-rFR": (
        "Suppression automatique des articles complétés",
        "Supprimer automatiquement les articles complétés après %1$d heures.",
        "%1$d heures"
    ),
    "hu-rHU": (
        "Befejezett elemek automatikus törlése",
        "Befejezett elemek automatikus eltávolítása %1$d óra után.",
        "%1$d óra"
    ),
    "it-rIT": (
        "Elimina automaticamente gli articoli completati",
        "Rimuovi automaticamente gli articoli completati dopo %1$d ore.",
        "%1$d ore"
    ),
    "iw-rIL": (
        "מחק פריטים שהושלמו אוטומטית",
        "הסר פריטים שהושלמו אוטומטית לאחר %1$d שעות.",
        "%1$d שעות"
    ),
    "ja-rJP": (
        "完了済みアイテムの自動削除",
        "%1$d 時間後に完了済みアイテムを自動的に削除します。",
        "%1$d 時間"
    ),
    "ko-rKR": (
        "완료된 항목 자동 삭제",
        "%1$d시간 후 완료된 항목을 자동으로 제거합니다.",
        "%1$d시간"
    ),
    "nl-rNL": (
        "Afgeronde items automatisch verwijderen",
        "Verwijder afgeronde items automatisch na %1$d uur.",
        "%1$d uur"
    ),
    "no-rNO": (
        "Slett fullførte varer automatisk",
        "Fjern fullførte varer automatisk etter %1$d timer.",
        "%1$d timer"
    ),
    "pl-rPL": (
        "Automatyczne usuwanie ukończonych pozycji",
        "Automatycznie usuń ukończone pozycje po %1$d godzinach.",
        "%1$d godz."
    ),
    "pt-rBR": (
        "Excluir itens concluídos automaticamente",
        "Remover automaticamente os itens concluídos após %1$d horas.",
        "%1$d horas"
    ),
    "pt-rPT": (
        "Eliminação automática de itens concluídos",
        "Remover automaticamente os itens concluídos após %1$d horas.",
        "%1$d horas"
    ),
    "ro-rRO": (
        "Ștergere automată articole finalizate",
        "Elimină automat articolele finalizate după %1$d ore.",
        "%1$d ore"
    ),
    "ru-rRU": (
        "Автоудаление выполненных товаров",
        "Автоматически удалять выполненные товары через %1$d ч.",
        "%1$d ч"
    ),
    "sr-rSP": (
        "Automatsko brisanje završenih stavki",
        "Automatski ukloni završene stavke nakon %1$d sati.",
        "%1$d sati"
    ),
    "sv-rSE": (
        "Radera slutförda varor automatiskt",
        "Ta bort slutförda varor automatiskt efter %1$d timmar.",
        "%1$d timmar"
    ),
    "tr-rTR": (
        "Tamamlanan Öğeleri Otomatik Sil",
        "Tamamlanan öğeleri %1$d saat sonra otomatik olarak kaldır.",
        "%1$d saat"
    ),
    "uk-rUA": (
        "Автовидалення виконаних товарів",
        "Автоматично видаляти виконані товари через %1$d год.",
        "%1$d год"
    ),
    "vi-rVN": (
        "Tự động xóa các mục đã hoàn thành",
        "Tự động xóa các mục đã hoàn thành sau %1$d giờ.",
        "%1$d giờ"
    ),
    "zh-rCN": (
        "自动删除已完成项目",
        "在 %1$d 小时后自动移除已完成的项目。",
        "%1$d 小时"
    ),
    "zh-rTW": (
        "自動刪除已完成項目",
        "在 %1$d 小時後自動移除已完成的項目。",
        "%1$d 小時"
    ),
}

INSERT_BEFORE = '    <string name="settings_default_list">'

def process_file(lang_code, title, description, hours):
    file_path = os.path.join(BASE_DIR, f"values-{lang_code}", "strings.xml")
    if not os.path.exists(file_path):
        print(f"  FEHLER: Datei nicht gefunden: {file_path}")
        return False

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Check if already inserted
    if "settings_auto_delete_completed_title" in content:
        print(f"  ÜBERSPRUNGEN: {lang_code} (bereits vorhanden)")
        return True

    new_strings = (
        f'    <string name="settings_auto_delete_completed_title">{title}</string>\n'
        f'    <string name="settings_auto_delete_completed_description">{description}</string>\n'
        f'    <string name="settings_auto_delete_completed_hours">{hours}</string>\n'
        f'    <string name="settings_default_list">'
    )

    if INSERT_BEFORE not in content:
        print(f"  FEHLER: Einfügepunkt nicht gefunden in {lang_code}")
        return False

    updated = content.replace(INSERT_BEFORE, new_strings, 1)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(updated)

    print(f"  OK: {lang_code}")
    return True


def main():
    print("Füge Auto-Delete Übersetzungen hinzu...\n")
    success = 0
    failed = 0
    for lang_code, (title, description, hours) in TRANSLATIONS.items():
        if process_file(lang_code, title, description, hours):
            success += 1
        else:
            failed += 1

    print(f"\nFertig: {success} erfolgreich, {failed} Fehler")


if __name__ == "__main__":
    main()

