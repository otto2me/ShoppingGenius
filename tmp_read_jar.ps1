param([string]$Jar,[string]$Entry)
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[System.IO.Compression.ZipFile]::OpenRead($Jar)
try {
  $item=$zip.Entries | Where-Object FullName -eq $Entry | Select-Object -First 1
  if (-not $item) { throw "Entry not found: $Entry" }
  $reader=New-Object System.IO.StreamReader($item.Open())
  try { $reader.ReadToEnd() } finally { $reader.Dispose() }
} finally { $zip.Dispose() }


