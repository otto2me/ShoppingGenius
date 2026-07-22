# -*- coding: utf-8 -*-
import os

res = r"C:\Work\Privat\GroceryGenius-develop\app\src\main\res"

translations = {
    "values-de-rDE": ("Eintr\u00e4ge", "Kategorien", "Favoriten", "Favorit"),
    "values-af-rZA": ("Items", "Kategorie\u00ebr", "Gunstelinge", "Gunsteling"),
    "values-ar-rSA": ("\u0627\u0644\u0639\u0646\u0627\u0635\u0631", "\u0627\u0644\u0641\u0626\u0627\u062a", "\u0627\u0644\u0645\u0641\u0636\u0644\u0629", "\u0645\u0641\u0636\u0644"),
    "values-ca-rES": ("Elements", "Categories", "Preferits", "Preferit"),
    "values-cs-rCZ": ("Polo\u017eky", "Kategorie", "Obl\u00edben\u00e9", "Obl\u00edben\u00e9"),
    "values-da-rDK": ("Varer", "Kategorier", "Favoritter", "Favorit"),
    "values-el-rGR": ("\u03a3\u03c4\u03bf\u03b9\u03c7\u03b5\u03af\u03b1", "\u039a\u03b1\u03c4\u03b7\u03b3\u03bf\u03c1\u03af\u03b5\u03c2", "\u0391\u03b3\u03b1\u03c0\u03b7\u03bc\u03ad\u03bd\u03b1", "\u0391\u03b3\u03b1\u03c0\u03b7\u03bc\u03ad\u03bd\u03bf"),
    "values-es-rES": ("Art\u00edculos", "Categor\u00edas", "Favoritos", "Favorito"),
    "values-fi-rFI": ("Tuotteet", "Kategoriat", "Suosikit", "Suosikki"),
    "values-fr-rFR": ("Articles", "Cat\u00e9gories", "Favoris", "Favori"),
    "values-hu-rHU": ("Elemek", "Kateg\u00f3ri\u00e1k", "Kedvencek", "Kedvenc"),
    "values-it-rIT": ("Articoli", "Categorie", "Preferiti", "Preferito"),
    "values-iw-rIL": ("\u05e4\u05e8\u05d9\u05d8\u05d9\u05dd", "\u05e7\u05d8\u05d2\u05d5\u05e8\u05d9\u05d5\u05ea", "\u05de\u05d5\u05e2\u05d3\u05e4\u05d9\u05dd", "\u05de\u05d5\u05e2\u05d3\u05e3"),
    "values-ja-rJP": ("\u30a2\u30a4\u30c6\u30e0", "\u30ab\u30c6\u30b4\u30ea", "\u304a\u6c17\u306b\u5165\u308a", "\u304a\u6c17\u306b\u5165\u308a"),
    "values-ko-rKR": ("\ud56d\ubaa9", "\uce74\ud14c\uace0\ub9ac", "\uc990\uaca8\ucc3e\uae30", "\uc990\uaca8\ucc3e\uae30"),
    "values-nl-rNL": ("Items", "Categorie\u00ebn", "Favorieten", "Favoriet"),
    "values-no-rNO": ("Varer", "Kategorier", "Favoritter", "Favoritt"),
    "values-pl-rPL": ("Elementy", "Kategorie", "Ulubione", "Ulubiony"),
    "values-pt-rBR": ("Itens", "Categorias", "Favoritos", "Favorito"),
    "values-pt-rPT": ("Itens", "Categorias", "Favoritos", "Favorito"),
    "values-ro-rRO": ("Articole", "Categorii", "Favorite", "Favorit"),
    "values-ru-rRU": ("\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b", "\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u0438", "\u0418\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "\u0418\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435"),
    "values-sr-rSP": ("Stavke", "Kategorije", "Omiljeni", "Omiljeno"),
    "values-sv-rSE": ("Varor", "Kategorier", "Favoriter", "Favorit"),
    "values-tr-rTR": ("\u00d6\u011feler", "Kategoriler", "Favoriler", "Favori"),
    "values-uk-rUA": ("\u0415\u043b\u0435\u043c\u0435\u043d\u0442\u0438", "\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0456\u0457", "\u0412\u0438\u0431\u0440\u0430\u043d\u0435", "\u0412\u0438\u0431\u0440\u0430\u043d\u0435"),
    "values-vi-rVN": ("M\u1eb7t h\u00e0ng", "Danh m\u1ee5c", "Y\u00eau th\u00edch", "Y\u00eau th\u00edch"),
    "values-zh-rCN": ("\u9879\u76ee", "\u5206\u7c7b", "\u6536\u85cf", "\u6536\u85cf"),
    "values-zh-rTW": ("\u9805\u76ee", "\u5206\u985e", "\u6536\u85cf", "\u6536\u85cf"),
}

for folder, (items, cats, favs, fav) in translations.items():
    path = os.path.join(res, folder, "strings.xml")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    if "grocery_list_tab_items" in content:
        print("SKIP: " + folder)
        continue
    insert = (
        '    <string name="grocery_list_tab_items">' + items + '</string>\n'
        '    <string name="grocery_list_tab_categories">' + cats + '</string>\n'
        '    <string name="grocery_list_tab_favorites">' + favs + '</string>\n'
        '    <string name="edit_grocery_toggle_favorite_button_title">' + fav + '</string>\n'
    )
    content = content.replace("</resources>", insert + "</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("OK: " + folder)

print("Done.")

