<h1 align="center" style="font-size:28px; line-height:1"><b>Shopping Genius</b></h1>

<div align="center">
  <img alt="Shopping Genius logo" src="images/app_logo_rounded_corners.svg" height="150px">
</div>

<br />

<div align="center">

[<img src="images/banners/banner_github.png"
      alt="Get it on GitHub"
      height="80">](https://github.com/DanielRendox/ShoppingGenius/releases)
[<img src="images/banners/banner_izzy_on_droid.png"
      alt="Get it on IzzyOnDroid"
      height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.rendox.ShoppingGenius)
[<img src="images/banners/banner_google_play.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=com.rendox.ShoppingGenius)

</div>

<br />
<br />

![Shopping Genius cover image](images/readme/readme_cover_image.png)

|                                                 |                                               |                                              |
|-------------------------------------------------|-----------------------------------------------|----------------------------------------------|
| ![](images/readme/feature_search_groceries.png) | ![](images/readme/feature_separate_lists.png) | ![](images/readme/feature_customization.png) |

Shopping Genius is a free, privacy-friendly shopping list app with offline-first data, fast product suggestions, and customizable lists.

## Project Origin

- This project is based on the original `GroceryGenius` repository: https://github.com/DanielRendox/GroceryGenius
- This fork is currently hosted here: https://github.com/otto2me/ShoppingGenius

## Features

- **Fast grocery input and autocomplete.** The app ships with bundled products, categories, and icons, and can auto-create custom products when a term is unknown.
- **Open vs completed organization.** Items are visually separated and can be managed quickly while shopping.
- **Modern Material You UI.** Grid-based layout, color themes, dark/light mode, and category-oriented grouping.
- **Multiple lists with drag and drop.** Reorder lists and categories efficiently.
- **Offline-first by design.** All core data is bundled and synced into local storage at startup.
- **Android Auto support.** Includes a car app service with product picking and grocery list interactions for compatible head units.
- **Privacy focused.** Data remains on-device; see [PRIVACY_POLICY.md](PRIVACY_POLICY.md).

## Build Configuration

Current Android config in `app/build.gradle.kts`:

- **compileSdk:** 35
- **targetSdk:** 35
- **minSdk:** 21
- **current app version:** `0.2.1` (`versionCode` 15)
- **JVM target:** 11

## Development Setup

1. Clone the repository and open it in Android Studio.
2. Ensure `local.properties` contains your local SDK path.
3. Add release signing and publishing credentials.

Example `local.properties` entries:

```ini
sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# Release signing
RELEASE_STORE_FILE=C:\\Work\\keys\\shoppinggenius-release.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=shoppinggenius
RELEASE_KEY_PASSWORD=***

# Google Play Publisher (required for Play upload)
PLAY_SERVICE_ACCOUNT_JSON=C:\\Work\\keys\\play-service-account.json

# Firebase App Distribution (optional)
FIREBASE_SERVICE_ACCOUNT_JSON=C:\\Work\\keys\\shoppinggenius-9df9c-firebase.json
FIREBASE_ARTIFACT_TYPE=APK
```

## Release and Publishing

### Publish to Play Console (internal track)

Use `publish_to_play.bat` from the project root. It:

1. Checks required files and config.
2. Prompts for `versionCode` and `versionName`.
3. Updates `app/build.gradle.kts` using `tools/update_version.ps1`.
4. Builds release AAB (`:app:bundleRelease`).
5. Uploads to Play (`:app:publishReleaseBundle`).

Run:

```bat
cd C:\Work\Privat\GroceryGenius-develop
publish_to_play.bat
```

Logs are written to `publish_play_log.txt`.

### Firebase App Distribution (optional)

If configured, distribution tasks can be used for tester delivery. Artifact type can be controlled with `FIREBASE_ARTIFACT_TYPE` in `local.properties`.

## Android Auto

- Car app metadata is declared in `app/src/main/AndroidManifest.xml`.
- Automotive description is in `app/src/main/res/xml/automotive_app_desc.xml`.
- The project includes `androidx.car.app` integration and a dedicated car app service.

## Data and Localization Pipeline

### Bundled assets

- `assets/product/`: product catalogs (including localized variants)
- `assets/category/`: category catalogs (including localized variants)
- `assets/icons/`: icon pack and generated artifacts

### Tooling scripts

- `tools/import_myshopi_catalog.py`: imports/normalizes product and category data from MyShopi exports.
- `tools/process_new_icons.py`: image cleanup pipeline (background removal, rounded corners, sync, ZIP regeneration).
- `tools/export_products_to_excel.py`: exports products to `product_export.xlsx`.
- `tools/embed_cc0_images_into_product_excel.py`: enriches exported product workbook with CC0/Public-Domain image sources and embeds thumbnails.

## Tech Stack

- Kotlin + Jetpack Compose
- Room
- DataStore Preferences
- WorkManager
- Moshi
- Coil
- Hilt
- Coroutines/Flow
- RecyclerView + ItemTouchHelper (for drag-and-drop scenarios)

## Roadmap

Potential long-term ideas:

- Sharing grocery lists with other people
- Location reminders


## Contributing

Contributions are welcome (bug fixes, features, translations, design, tooling). Please open an issue or PR.

For source references:

- Original upstream: https://github.com/DanielRendox/GroceryGenius
- Current fork: https://github.com/otto2me/ShoppingGenius

### Translations

Configured sync sources include:

- `app/src/main/res/values/strings.xml`
- `assets/category/categories_en.json`
- `assets/product/default_products_en.json`

## License

The project code is licensed under GPL v3. See `LICENSE`.

Icon assets in `assets/icons` (and copies in app assets) are excluded from GPL code licensing terms as documented by the project and may carry separate usage restrictions.

