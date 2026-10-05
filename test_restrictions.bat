@echo off
REM Quick Test Script für Driver Distraction Restrictions
REM Speichere als: test_restrictions.bat im GroceryGenius-develop Ordner
REM Starten mit: .\test_restrictions.bat

setlocal enabledelayedexpansion

echo ============================================
echo  GroceryGenius - Driver Distraction Test
echo ============================================
echo.

REM Check if emulator is running
adb devices | findstr "emulator" > nul
if errorlevel 1 (
    echo ERROR: Kein Emulator gefunden!
    echo Bitte starten Sie den Android Emulator zuerst.
    pause
    exit /b 1
)

echo [1/5] App installieren...
call gradlew.bat installDebug
if errorlevel 1 (
    echo ERROR: Installation fehlgeschlagen
    pause
    exit /b 1
)

echo.
echo [2/5] Standmodus aktivieren (Auto parkt)...
adb shell setprop persist.sys.car_mode 0
adb shell setprop persist.sys.car_ui_mode 0
adb shell setprop persist.sys.usb.config adb

echo.
echo [3/5] App im Standmodus starten...
adb shell am force-stop com.shoppinggenius.app
adb shell am start -n com.shoppinggenius.app/.MainActivity
echo.
echo STANDMODUS AKTIV ===========================
echo Teste folgende Funktionen (sollten alle funktionieren):
echo   - Tippe auf eine Einkaufsliste
echo   - Tippe auf ein Produkt/Toggle
echo   - Tippe auf "Add Item" Button
echo   - Produkt hinzufügen
echo.
echo Drücke ENTER wenn Test fertig...
pause

echo.
echo [4/5] Fahrtmodus aktivieren (Fahrer fährt)...
adb shell setprop persist.sys.car_mode 1
adb shell setprop persist.sys.car_ui_mode 1
adb shell am force-stop com.shoppinggenius.app
adb shell am start -n com.shoppinggenius.app/.MainActivity

echo.
echo FAHRTMODUS AKTIV ============================
echo Teste folgende Funktionen:
echo   X Tippen auf Liste sollte NICHT funktionieren
echo   X Toggle sollte NICHT funktionieren
echo   X "Add Item" Tippen sollte NICHT funktionieren
echo   ✓ Sprachsuche sollte funktionieren ("🎤" Symbol sichtbar)
echo.
echo Drücke ENTER wenn Test fertig...
pause

echo.
echo [5/5] Logcat anzeigen (Restrictions-Debug)...
echo Filtert automatisch nach "CarUxRestrictions" Debug-Ausgaben
echo Drücke CTRL+C zum Beenden
adb logcat | findstr "CarUxRestrictions"

echo.
echo Test abgeschlossen!
pause

