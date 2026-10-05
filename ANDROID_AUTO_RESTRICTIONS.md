## Driver Distraction Restrictions für Android Auto

Diese Dokumentation beschreibt, wie die **Driver Distraction Restrictions** nach Google Android for Cars App Library Standards implementiert wurden.

### Übersicht

Die Android Auto App "GroceryGenius" implementiert die Driver Distraction Restrictions mit folgenden Zielen:

1. **Sichere Verwendung während der Fahrt**: Nur sichere Operationen sind während der Fahrt möglich
2. **Sprachsteuerung priorisieren**: Im Fahrtmodus können neue Listenelemente nur über Spracheingaben hinzugefügt werden
3. **Blockierung von komplexen Operationen**: Tippen auf UI-Elemente wird im Fahrtmodus deaktiviert
4. **Unrestricted Mode**: Im Standmodus (Auto parkt) sind alle Funktionen verfügbar

### Kategorisierung

Die App ist unter der Kategorie **"messenger"** registriert (nicht "IOT" oder "POI"), da die Hauptfunktion das Teilen und Verwenden von Einkaufslisten ist.

**AndroidManifest.xml:**
```xml
<intent-filter>
    <action android:name="androidx.car.app.CarAppService" />
    <category android:name="androidx.car.app.category.MESSAGING" />
</intent-filter>
```

### Implementation

#### 1. CarUxRestrictionsHelper Klasse
Datei: `CarUxRestrictionsHelper.kt`

Diese Hilfsklasse verwaltet den Status der Driver Distraction Restrictions:

```kotlin
fun isDriverDistracted(): Boolean
```
- Gibt `true` zurück wenn der Fahrer fährt (Restrictions aktiv)
- Gibt `false` zurück wenn das Auto steht

```kotlin
fun canPerformClickActions(): Boolean
```
- Gibt `true` zurück wenn Tippen auf UI-Elemente möglich ist
- Im Fahrtmodus: `false` (nur Sprachsteuerung)

#### 2. GroceryListsScreen
Datei: `GroceryListsScreen.kt`

Angepasst für Driver Distraction Restrictions:

**Im Standmodus:**
- Normalles Tippen auf Einkaufslisten ermöglicht Navigation zu Items

**Im Fahrtmodus:**
- Tippen auf Listen ist deaktiviert
- Nur Sprachbefehle möglich
- Visueller Hinweis: "🎤 Sprachbefehle möglich"

#### 3. GroceryItemsScreen
Datei: `GroceryItemsScreen.kt`

**Schalter-Button (Toggle) Verhalten:**
- Im Standmodus: Normales Tippen zum Abhaken/Abhaken-Rückgängigmachen von Items
- Im Fahrtmodus: Toggle ist deaktiviert, nur Spracheingaben möglich

**Add Item Button:**
- Im Standmodus: Button ist aktiv und tappbar
- Im Fahrtmodus: Button ohne OnClickListener (nur über Sprachsteuerung erreichbar)

#### 4. ProductPickerScreen
Datei: `ProductPickerScreen.kt`

Diese Screen wird verwendet um Produkte zur Liste hinzuzufügen.

**Tippen auf Produkte:**
- Im Standmodus: Produkte können durch Tippen hinzugefügt werden
- Im Fahrtmodus: Tippen ist deaktiviert

**Sprachsuche (Voice Input):**
- IMMER aktiv (auch im Fahrtmodus)
- Benutzer kann nach Produkten sprechen und diese werden zur Liste hinzugefügt
- SearchTemplate.SearchCallback.onSearchSubmitted() ist nicht von Restrictions betroffen

### Restrictions-Level

Der Fahrer wird als "distracted" betrachtet wenn:
- Das Auto sich bewegt
- CarUxRestrictionsManager.currentRestrictions.isRequiresDistractionOptimization == true

### Testing

Zum Testen der Driver Distraction Restrictions in Android Studio:

1. **Emulator konfigurieren:**
   ```bash
   # Simuliert Fahrt-Status
   adb shell setprop persist.sys.usb.config adb,acc_aoa
   ```

2. **Android Auto-Session mit Restrictions:**
   - Android Auto lädt die App
   - CarUxRestrictionsManager wird bei Fahrt aktiviert
   - Tipp-Gesten sollten nicht funktionieren
   - Sprachbefehle sollten funktionieren

### Best Practices

1. **Voice First**: Im Fahrtmodus sollten alle Operationen über Sprache möglich sein
2. **Visual Feedback**: Benutzer sollte sehen, dass sie sich im Fahrtmodus befinden
3. **Kein Übersteuern**: Sprachbefehle sollten immer funktionieren, unabhängig vom Fahrt-Status
4. **Graceful Degradation**: UI sollte sich elegant an Restrictions anpassen

### Zukünftige Improvements

1. **Text-to-Speech für Bestätigungen**: "Artikel hinzugefügt: Milch"
2. **Voice Command Hinweise**: "Sage 'Milch hinzufügen' um ein Produkt hinzuzufügen"
3. **Custom Voice Commands**: Integrieren mit VoiceInteractionSession
4. **Distraction Optimizer**: Weitere Unify der UI für verschiedene Restrictions-Level

