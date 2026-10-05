# Testing Driver Distraction Restrictions in der Emulation

> **Wichtig:** Ein normaler Phone-Emulator wie ein Pixel-AVD reicht dafür **nicht** aus.
> Die Driver-Distraction-Regeln greifen nur in einem **Car Host**:
> - **Android Automotive OS Emulator (AAOS)** oder
> - **Android Auto Desktop Head Unit (DHU)** / echtes Android Auto.
>
> Wenn du nur `com.shoppinggenius.app/.MainActivity` auf einem Pixel-Emulator öffnest,
> testest du die normale Phone-App – **nicht** die Car-App-Screens aus `feature/carapp`.

## Voraussetzungen

1. **AAOS Emulator** oder **Android Auto DHU**
2. **GroceryGenius App** installiert
3. Start der **Car-App**, nicht nur der normalen Phone-UI

## Option 1: Automotive OS Emulator (Empfohlen)

### Schritt 1: Automotive OS Image erstellen

```bash
# Im Android Studio:
# 1. Tools → Device Manager
# 2. Create device
# 3. "Automotive OS" wählen
# 4. API Level 31+ wählen (z.B. Android 12)
# 5. Device erstellen und starten
```

### Schritt 2: App im Automotive Emulator starten

```bash
# Terminal/PowerShell:
cd C:\Work\Privat\GroceryGenius-develop

# Emulator starten (falls nicht automatisch)
emulator -avd Automotive_OS_Device_name

# App installieren und starten
gradlew.bat installDebug
adb shell am start -n com.shoppinggenius.app/.MainActivity
```

### Schritt 3: Android Auto öffnen

```bash
# Automotive OS hat Android Auto vorintegriert
# Im Emulator auf Android Auto App tippen oder:
adb shell am start -n com.android.car.media/.MediaActivity
```

## Option 2: Android Auto über ADB (Fortgeschrittene Methode)

### Schritt 1: GroceryGenius App starten

```bash
# App im regulären Emulator starten
adb shell am start -n com.shoppinggenius.app/.MainActivity
```

### Schritt 2: Car Service mit Distraction aktivieren

```bash
# Driver Distraction Mode simulieren
adb shell setprop persist.sys.car_mode 1

# Fahrt-Status aktivieren (Fahrer distracted)
adb shell setprop persist.sys.car_ui_mode 1
```

### Schritt 3: CarAppService direkt aufrufen

```bash
# Intent zum Starten des Car App Service
adb shell am startservice -n com.shoppinggenius.app/.feature.carapp.ShoppingGeniusCarAppService

# Oder über Explicit Intent mit Extra (Fahrt-Status)
adb shell am start-foreground-service \
  -n com.shoppinggenius.app/.feature.carapp.ShoppingGeniusCarAppService \
  --ez "com.android.car.uxr.UX_RESTRICTIONS_DRIVING" true
```

## Option 3: Debugging mit Android Studio

### Schritt 1: Emulator mit Debug starten

```bash
# PowerShell
cd C:\Work\Privat\GroceryGenius-develop
gradlew.bat installDebug
```

### Schritt 2: CarUxRestrictionsHelper Logger hinzufügen

Temporär kannst du in der App folgende Änderung machen zum Debugging:

**Datei: `CarUxRestrictionsHelper.kt`**

```kotlin
fun isDriverDistracted(): Boolean {
    val restrictions = restrictionsManager.currentRestrictions
    val isDistracted = restrictions.isRequiresDistractionOptimization
    
    // DEBUG: Log ausgeben
    android.util.Log.d("CarUxRestrictions", 
        "isDriverDistracted=$isDistracted, activeRestrictions=${restrictions.activeRestrictions}")
    
    return isDistracted
}
```

### Schritt 3: Logcat beobachten

```bash
# Terminal: Logcat filtern
adb logcat | findstr "CarUxRestrictions"
```

## Manuelle Test-Szenarien

### Szenario 1: Standmodus testen (Auto parkt) 🏁

1. App starten
2. Auf Liste tippen → **sollte funktionieren** ✅
3. Toggle-Schalter klicken → **sollte funktionieren** ✅
4. "Add Item" Button tippen → **sollte funktionieren** ✅
5. Produkt tippen → **sollte funktionieren** ✅

**Erwartetes Verhalten:**
- Alle UI-Elemente sind interaktiv
- Keine "🎤"-Symbole sichtbar

### Szenario 2: Fahrtmodus simulieren 🚗

```bash
# In Android Studio Console oder Terminal:
adb shell setprop persist.sys.car_ui_mode 1

# App neu starten oder Bildschirm aktualisieren
adb shell am force-stop com.shoppinggenius.app
adb shell am start -n com.shoppinggenius.app/.feature.carapp.ShoppingGeniusCarAppService
```

**Erwartetes Verhalten:**
- Listen-Tippen → **deaktiviert** ❌ 
- "🎤 Sprachbefehle möglich" Nachricht sichtbar
- Toggle-Schalter → **deaktiviert** ❌
- Add Item Button → **keine OnClickListener** ❌
- Sprachsuche (SearchTemplate) → **funktioniert** ✅

### Szenario 3: Voice Input testen 🎤

1. **Im Fahrtmodus** ProductPickerScreen öffnen
2. Mit Mikrofon-Button "Milch" sprechen
3. App sollte "Milch" zur Liste hinzufügen
4. **Tippen auf Result** → sollte nicht funktionieren (kein Listener)

```bash
# Simulieren von Voice Input (fortgeschritten):
adb shell input text "Milch"
adb shell input keyevent 66  # ENTER
```

## Debugging Tools

### CarUxRestrictionsManager State prüfen

```bash
# Dumpen des Car Service State
adb shell dumpsys car_service

# Spezifisch UX Restrictions
adb shell dumpsys car_uxr
```

### UI State mit Screenshot prüfen

```bash
# Screenshot machen
adb shell screencap -p > screenshot.png

# Emulator sollte zeigen:
# - Im Standmodus: volle Interaktivität
# - Im Fahrtmodus: deaktivierte Buttons mit "🎤" Symbolen
```

## Troubleshooting

### Problem: CarUxRestrictionsManager liefert immer `false`

**Lösung:**
- Sicherstellen, dass Automotive OS Image verwendet wird
- Oder: setprop Befehle mehrmals ausführen

```bash
adb shell setprop persist.sys.car_mode 1
adb shell setprop persist.sys.car_ui_mode 1
adb shell setprop persist.sys.usb.config adb
```

### Problem: App startet nicht mit CarAppService

**Lösung:**
- App im regulären Mode starten und Android Auto später aufrufen:

```bash
# Step 1: App starten
adb shell am start -n com.shoppinggenius.app/.MainActivity

# Step 2: Nach einigen Sekunden Android Auto starten
sleep 3
adb shell am start -n com.android.car.media/.MediaActivity
```

### Problem: Voice Input funktioniert nicht

**Lösung:**
- SearchTemplate benötigt ein funktionierendes Mikrofon
- Im Emulator: Virtuelle Microfon-Input über ADB simulieren oder
- Mit `adb shell input text` kombinieren:

```bash
# Typ "Milch"
adb shell input text "Milch"

# Drücke ENTER (Search absenden)
adb shell input keyevent 66
```

## Full Testing Script

Hier ist ein komplettes Test-Script zum Kopieren:

```bash
#!/bin/bash

echo "=== GroceryGenius Driver Distraction Testing ==="

# Schritt 1: App installieren
echo "1. App installieren..."
cd C:\Work\Privat\GroceryGenius-develop
gradlew.bat installDebug

# Schritt 2: App starten
echo "2. App starten..."
adb shell am start -n com.shoppinggenius.app/.MainActivity
sleep 2

# Schritt 3: Standmodus testen
echo "3. STANDMODUS TESTEN (Auto parkt)"
echo "   - Öffne die App und prüfe: Listen tappbar? Toggle aktiv? Add Button funktioniert?"
adb shell setprop persist.sys.car_mode 0
adb shell setprop persist.sys.car_ui_mode 0
sleep 2
adb shell am force-stop com.shoppinggenius.app

# Schritt 4: Fahrtmodus testen
echo "4. FAHRTMODUS TESTEN (Fahrer fährt)"
echo "   - Öffne die App und prüfe: Listen NICHT tappbar? Sprachbefehle nur? 🎤 Symbol sichtbar?"
adb shell setprop persist.sys.car_mode 1
adb shell setprop persist.sys.car_ui_mode 1
sleep 1
adb shell am start -n com.shoppinggenius.app/.MainActivity
sleep 2

echo "=== Testing Complete ==="
```

## Visuelle Indikatoren zum Testen

| Element | Standmodus 🏁 | Fahrtmodus 🚗 |
|---------|---------------|--------------|
| Listen | ✅ Tappbar | ❌ Nicht tappbar + "🎤" |
| Toggle | ✅ Funktioniert | ❌ Deaktiviert |
| Add Item | ✅ Tappbar | ❌ Keine Listener |
| Produkte | ✅ Tappbar | ❌ Nur Voice Input |
| Sprachsuche | ✅ Optional | ✅ **IMMER aktiv** |

## Real Device Testing

Wenn du auf einem echten Android Auto-Device testest:

```bash
# Connected Device auflisten
adb devices

# Auf specific device abzielen
adb -s DEVICE_ID install -r app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_ID shell am start -n com.shoppinggenius.app/.MainActivity
```

**Hinweis:** Echte Devices zeigen reale Fahrt-Status basierend auf GPS/Sensoren!

