param([string]$RepoPath = ".")

$ErrorActionPreference = "Stop"
$root = (Resolve-Path -LiteralPath $RepoPath).Path
$files = @(& git -C $root ls-files --cached --others --exclude-standard)
if ($LASTEXITCODE -ne 0) { throw 'Cannot enumerate repository files' }

$excluded = @(
  '.portfolio/contracts/fixtures/project.invalid.json',
  '.portfolio/contracts/fixtures/project.legacy.valid.json',
  '.portfolio/contracts/fixtures/project.non-jvm.valid.json',
  '.portfolio/contracts/fixtures/project.valid.json',
  '.portfolio/contracts/fixtures/project.jvm-profile-missing-jvm.invalid.json',
  '.portfolio/contracts/fixtures/project.kafka-mode-mismatch.invalid.json',
  '.portfolio/contracts/fixtures/project.kafka-selected-brain-none.invalid.json',
  '.portfolio/contracts/fixtures/project.kafka-streams-orphan.invalid.json',
  '.portfolio/contracts/fixtures/project.unknown-wrapper-checksum.invalid.json',
  '.portfolio/contracts/fixtures/project.wrapper-pair-mismatch.invalid.json'
)
$patterns = @(
  '(?i)github_pat_[A-Za-z0-9_]{20,}',
  '(?i)ghp_[A-Za-z0-9]{30,}',
  '(?i)AKIA[0-9A-Z]{16}',
  '(?i)-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----',
  '(?i)(password|passwd|secret|api[_-]?key|access[_-]?token)\s*[:=]\s*["''][^"'']{12,}["'']'
)
$findings = [System.Collections.Generic.List[string]]::new()
foreach ($relative in $files) {
  $normalized = $relative.Replace([IO.Path]::DirectorySeparatorChar, [char]47)
  if ($excluded -contains $normalized) { continue }
  $path = Join-Path $root $relative
  if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
  if ((Get-Item -LiteralPath $path).Length -gt 2MB) { continue }
  $content = Get-Content -Raw -LiteralPath $path -ErrorAction SilentlyContinue
  if ($null -eq $content) { continue }
  foreach ($pattern in $patterns) {
    if ($content -match $pattern) {
      $findings.Add("$normalized matched $pattern")
    }
  }
}
if ($findings.Count -gt 0) {
  $findings | ForEach-Object { Write-Error $_ -ErrorAction Continue }
  exit 1
}
Write-Host 'secret_scan=passed'
Write-Host "files_scanned=$($files.Count - $excluded.Count)"
