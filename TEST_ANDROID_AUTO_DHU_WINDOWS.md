# Android Auto Test mit Telefon + DHU unter Windows

Diese Anleitung beschreibt den schnellsten Weg, die Android-Auto-Car-App von `GroceryGenius` mit einem **echten Telefon** und der **Desktop Head Unit (DHU)** unter Windows zu testen.

## Ziel

Folgende Fälle sollen überprüft werden:

1. **Liste antippen** → soll während der Fahrt **nicht** funktionieren
2. **Eintrag als erledigt/offen ändern** → soll während der Fahrt **nicht** funktionieren
3. **`Add Item` antippen** → soll während der Fahrt **nicht** funktionieren
4. **Sprachsuche** → soll weiter funktionieren

## Was hier wirklich getestet wird

- Die Car-App-Screens aus `app/src/main/java/com/shoppinggenius/app/feature/carapp/`
- Die hostseitige Sperre durch `ParkedOnlyOnClickListener`
- Android Auto als **Car Host**

## Voraussetzungen

- Windows-PC
- Android-Smartphone
- USB-Kabel
- `adb.exe` vorhanden
- `desktop-head-unit.exe` vorhanden
- Android Auto auf dem Telefon installiert
- Entwickleroptionen auf dem Telefon aktiviert

## Bereits lokal vorhanden

In dieser Umgebung wurden folgende Tools gefunden:

- `C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- `C:\Users\fixczpt\AppData\Local\Android\Sdk\extras\google\auto\desktop-head-unit.exe`

## 1. Telefon vorbereiten

### A. Entwickleroptionen im Telefon aktivieren
- USB-Debugging aktivieren
- Telefon per USB verbinden
- Zugriffsanfrage für USB-Debugging bestätigen

### B. Android Auto Entwicklermodus aktivieren
Je nach Version leicht unterschiedlich, typischer Ablauf:

1. Android Auto auf dem Telefon öffnen
2. Einstellungen öffnen
3. Mehrfach auf die Versionsnummer tippen, bis der Entwicklermodus aktiv ist
4. Im Menü die **Entwicklereinstellungen** öffnen
5. **Unbekannte Quellen** erlauben

> Falls die App im Host nicht erscheint, ist dieser Schritt meistens die Ursache.

## 2. Verbindung prüfen

```powershell
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

Erwartung:
- dein Telefon wird als `device` angezeigt
- nicht `unauthorized`

## 3. App installieren

```powershell
cd C:\Work\Privat\GroceryGenius-develop
.\gradlew.bat installDebug
```

## 4. Port-Forwarding für Android Auto setzen

```powershell
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" forward tcp:5277 tcp:5277
```

## 5. DHU starten

```powershell
& "C:\Users\fixczpt\AppData\Local\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
```

Danach sollte sich das Android-Auto-Fenster auf dem PC öffnen.

## 6. GroceryGenius im Host öffnen

Im DHU-Fenster:
- in die App-Übersicht wechseln
- `GroceryGenius` auswählen

> Wichtig: Nicht die normale Phone-App testen, sondern die App im Android-Auto-Host.

## 7. Testfälle systematisch durchführen

## Test A: Parkzustand

Erwartung im Parkzustand:
- Listen öffnen funktioniert
- `Add Item` funktioniert
- Produkte antippen funktioniert
- Einträge als erledigt/offen markieren funktioniert

### Ablauf
1. App im DHU öffnen
2. Eine Liste öffnen
3. `Add Item` antippen
4. Ein Produkt auswählen
5. Zur Liste zurückgehen
6. Einen Eintrag als erledigt/offen markieren

## Test B: Fahrzustand

Jetzt in den Fahrmodus wechseln.

### Hinweis
Je nach DHU-Version befindet sich der Fahr-/Parkstatus an unterschiedlicher Stelle, z. B.:
- Toolbar/Statusbereich
- Menüpunkt für Fahrzustand
- Host-spezifische Simulation

Wenn es einen Schalter für **Driving / Parked** gibt, auf **Driving** stellen.

### Erwartung im Fahrzustand
- **Liste antippen** → darf nicht auslösen
- **`Add Item` antippen** → darf nicht auslösen
- **Produkt antippen** → darf nicht auslösen
- **Eintrag erledigt/offen markieren** → darf nicht auslösen
- der Host sollte eine Park-/Sicherheitsmeldung anzeigen oder die Aktion still blockieren

## Test C: Spracheingabe

Die Spracheingabe testest du am besten so:

1. **im Parkzustand** die Zielliste öffnen
2. **im Parkzustand** `Add Item` öffnen
3. dann erst auf **Driving** wechseln
4. im `SearchTemplate` die Spracheingabe verwenden

### Erwartung
- Touch auf Listenelemente bleibt blockiert
- Sprachsuche bzw. Sucheingabe soll weiter nutzbar sein

## Log-Ausgabe prüfen

```powershell
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat | findstr CarUxRestrictions
```

### Mögliche Interpretation
- Auf Android Auto projected / DHU kann es sein, dass keine echte AAOS-Restrictions-API verfügbar ist
- Das ist **nicht automatisch falsch**
- Entscheidend ist hier vor allem, dass der Host `ParkedOnlyOnClickListener` korrekt durchsetzt

## Wenn die App im DHU nicht erscheint

Prüfen:
- Android Auto Entwicklermodus aktiv?
- Unbekannte Quellen erlaubt?
- Debug-Build installiert?
- Telefon in `adb devices` sichtbar?

## Wenn Klicks trotz Fahrmodus noch funktionieren

Prüfen:
- Testest du wirklich im DHU-Fenster?
- Oder testest du versehentlich die normale App auf dem Telefon?
- Wurde der Fahrmodus im Host wirklich umgeschaltet?
- Wurde nach einem Neuinstallieren die App im Host neu geöffnet?

## Nützliche Befehle

### App neu installieren

```powershell
cd C:\Work\Privat\GroceryGenius-develop
.\gradlew.bat installDebug
```

### App auf dem Telefon beenden

```powershell
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.shoppinggenius.app
```

### Weiterleitung neu setzen

```powershell
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" forward --remove tcp:5277
"C:\Users\fixczpt\AppData\Local\Android\Sdk\platform-tools\adb.exe" forward tcp:5277 tcp:5277
```

## Kurzfazit

Für deinen Fall ist **Telefon + DHU** der schnellste realistische Test:

- normale Phone-UI alleine reicht nicht
- der Android-Auto-Host muss aktiv sein
- die eigentliche Sperre erfolgt hostseitig über `ParkedOnlyOnClickListener`

