$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$gradle = Get-Command gradle -ErrorAction SilentlyContinue
if (-not $gradle) {
  throw "Gradle is required for strict validation. Use the Docker or CI path when Gradle is not installed locally."
}

Push-Location $root
try {
  & $gradle.Source --no-daemon clean check
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
  Pop-Location
}
