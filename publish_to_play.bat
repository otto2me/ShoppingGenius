@echo off
setlocal EnableDelayedExpansion

:: ============================================================
::  GroceryGenius – Release-Build + Upload zur Play Console
::  Track: internal  |  Artifact: AAB
::  Voraussetzung: PLAY_SERVICE_ACCOUNT_JSON in local.properties
:: ============================================================

set "ROOT=%~dp0"
set "GRADLEW=%ROOT%gradlew.bat"
set "LOGFILE=%ROOT%publish_play_log.txt"
set "BUILD_GRADLE=%ROOT%app\build.gradle.kts"

echo.
echo ============================================================
echo  GroceryGenius ^– Play Console Upload
echo ============================================================
echo  Startzeit: %date% %time%
echo  Arbeitsverzeichnis: %ROOT%
echo.

:: ----------------------------------------------------------
:: Voraussetzungen pruefen
:: ----------------------------------------------------------
if not exist "%GRADLEW%" (
    echo [FEHLER] gradlew.bat nicht gefunden in %ROOT%
    exit /b 1
)
if not exist "%BUILD_GRADLE%" (
    echo [FEHLER] app\build.gradle.kts nicht gefunden.
    exit /b 1
)

set "LOCAL_PROPS=%ROOT%local.properties"
if not exist "%LOCAL_PROPS%" (
    echo [FEHLER] local.properties nicht gefunden.
    exit /b 1
)

:: PLAY_SERVICE_ACCOUNT_JSON pruefen
set "SA_FOUND=0"
for /f "usebackq tokens=1,* delims==" %%A in ("%LOCAL_PROPS%") do (
    if "%%A"=="PLAY_SERVICE_ACCOUNT_JSON" set "SA_FOUND=1"
)
if "%SA_FOUND%"=="0" (
    echo [FEHLER] PLAY_SERVICE_ACCOUNT_JSON fehlt in local.properties.
    exit /b 1
)
echo [OK] Service-Account-Konfiguration gefunden.

:: ----------------------------------------------------------
:: Aktuelle Version aus build.gradle.kts lesen
:: ----------------------------------------------------------
set "CUR_CODE="
set "CUR_NAME="
for /f "usebackq tokens=*" %%L in ("%BUILD_GRADLE%") do (
    set "LINE=%%L"
    set "TRIM=!LINE: =!"
    if "!TRIM:~0,12!"=="versionCode=" (
        set "CUR_CODE=!TRIM:~12!"
    )
    if "!TRIM:~0,12!"=="versionName=" (
        set "CUR_NAME=!TRIM:~12!"
        :: Gaensefuesschen entfernen
        set "CUR_NAME=!CUR_NAME:"=!"
    )
)

if not defined CUR_CODE (
    echo [FEHLER] versionCode konnte nicht gelesen werden.
    exit /b 1
)
if not defined CUR_NAME (
    echo [FEHLER] versionName konnte nicht gelesen werden.
    exit /b 1
)

:: Vorschlag: versionCode + 1
set /a SUGGESTED_CODE=CUR_CODE+1

echo.
echo ============================================================
echo  Aktuelle Version: %CUR_NAME%  (versionCode %CUR_CODE%)
echo ============================================================
echo.

:: ----------------------------------------------------------
:: versionCode abfragen
:: ----------------------------------------------------------
set "NEW_CODE="
set /p "NEW_CODE=  Neuer versionCode   [Vorschlag: %SUGGESTED_CODE%, Enter = unveraendert %CUR_CODE%]: "
if "!NEW_CODE!"=="" set "NEW_CODE=%CUR_CODE%"

:: Nur Zahlen erlauben
set "CODE_CHECK=!NEW_CODE!"
for /f "delims=0123456789" %%X in ("!CODE_CHECK!") do (
    echo [FEHLER] versionCode muss eine ganze Zahl sein.
    exit /b 1
)

:: ----------------------------------------------------------
:: versionName abfragen
:: ----------------------------------------------------------
set "NEW_NAME="
set /p "NEW_NAME=  Neuer versionName   [aktuell: %CUR_NAME%, Enter = unveraendert]: "
if "!NEW_NAME!"=="" set "NEW_NAME=%CUR_NAME%"

echo.
echo  Neue Version wird gesetzt: !NEW_NAME!  (versionCode !NEW_CODE!)
echo.
set "CONFIRM="
set /p "CONFIRM=  Aenderungen speichern und Build starten? [J/n]: "
if /i "!CONFIRM!"=="n" (
    echo  Abgebrochen.
    exit /b 0
)

:: ----------------------------------------------------------
:: build.gradle.kts aktualisieren (via tools\update_version.ps1)
:: ----------------------------------------------------------
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%tools\update_version.ps1" -GradleFile "!BUILD_GRADLE!" -VersionCode "!NEW_CODE!" -VersionName "!NEW_NAME!"
if ERRORLEVEL 1 (
    echo [FEHLER] Konnte build.gradle.kts nicht aktualisieren.
    exit /b 1
)
echo [OK] build.gradle.kts aktualisiert: versionName = !NEW_NAME! / versionCode = !NEW_CODE!

:: Log-Datei neu starten
echo Release-Build gestartet am %date% %time% > "%LOGFILE%"
echo versionCode: !NEW_CODE!  versionName: !NEW_NAME! >> "%LOGFILE%"

:: ----------------------------------------------------------
:: SCHRITT 1: Release-AAB bauen
:: ----------------------------------------------------------
echo.
echo [SCHRITT 1/2] Baue Release-AAB ...
echo Vollstaendige Ausgabe wird in publish_play_log.txt gespeichert.
echo.

powershell -NoProfile -Command "& '%GRADLEW%' :app:bundleRelease --stacktrace 2>&1 | Tee-Object -FilePath '%LOGFILE%' -Append"
if ERRORLEVEL 1 (
    echo.
    echo [FEHLER] Release-Build fehlgeschlagen.
    echo Siehe %LOGFILE% fuer Details.
    exit /b 1
)
echo.
echo [OK] AAB erfolgreich gebaut.

:: ----------------------------------------------------------
:: SCHRITT 2: In Play Console hochladen
:: ----------------------------------------------------------
echo.
echo [SCHRITT 2/2] Lade AAB in Play Console hoch (Internal Track) ...
echo.

powershell -NoProfile -Command "& '%GRADLEW%' :app:publishReleaseBundle --stacktrace 2>&1 | Tee-Object -FilePath '%LOGFILE%' -Append"
if ERRORLEVEL 1 (
    echo.
    echo [FEHLER] Upload zur Play Console fehlgeschlagen.
    echo Siehe %LOGFILE% fuer Details.
    exit /b 1
)

echo.
echo ============================================================
echo  [ERFOLG] App v!NEW_NAME! (Code !NEW_CODE!) erfolgreich
echo  im Internal Track der Play Console veroeffentlicht.
echo  Endzeit: %date% %time%
echo  Log: %LOGFILE%
echo ============================================================
echo.
endlocal
