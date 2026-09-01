param(
    [Parameter(Mandatory)][string]$GradleFile,
    [Parameter(Mandatory)][string]$VersionCode,
    [Parameter(Mandatory)][string]$VersionName
)

if (-not (Test-Path $GradleFile)) {
    Write-Error "Datei nicht gefunden: $GradleFile"
    exit 1
}

$q       = [char]34
$content = Get-Content $GradleFile -Raw

$content = $content -replace 'versionCode\s*=\s*\d+', "versionCode = $VersionCode"
$content = $content -replace "versionName\s*=\s*$q[^$q]*$q", "versionName = $q$VersionName$q"

[System.IO.File]::WriteAllText($GradleFile, $content)
Write-Host "[OK] versionCode = $VersionCode  |  versionName = $VersionName"

