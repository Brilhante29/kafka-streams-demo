param(
  [ValidateRange(1, 1000000)]
  [int]$Records = 1000,
  [ValidateRange(0, 100)]
  [int]$Warmups = 1,
  [ValidateRange(1, 100)]
  [int]$Repeats = 5,
  [string]$OutputPath = "benchmarks/results/broker-latest.json",
  [switch]$AllowDirty
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$resultsRoot = [IO.Path]::GetFullPath((Join-Path $root "benchmarks/results"))
$output = [IO.Path]::GetFullPath((Join-Path $root $OutputPath))

if (-not $output.StartsWith($resultsRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
  throw "OutputPath must stay under benchmarks/results"
}

function Invoke-Checked {
  param([string[]]$Arguments)
  & docker @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "docker $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
  }
}

$status = @(& git -C $root status --porcelain --untracked-files=normal)
if ($LASTEXITCODE -ne 0) { throw "Cannot inspect Git status" }
$cleanTree = $status.Count -eq 0
if (-not $AllowDirty -and -not $cleanTree) {
  throw "Release evidence requires a clean Git worktree. Use -AllowDirty only for smoke evidence."
}

$sourceCommit = (& git -C $root rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceCommit -notmatch "^[0-9a-f]{40}$") {
  throw "Cannot resolve source commit"
}

$lockPath = Join-Path $root "gradle.lockfile"
if (-not (Test-Path -LiteralPath $lockPath -PathType Leaf)) {
  throw "gradle.lockfile is required"
}
$lockDigest = "sha256:" + (Get-FileHash -LiteralPath $lockPath -Algorithm SHA256).Hash.ToLowerInvariant()
$relativeOutput = $output.Substring($resultsRoot.Length + 1).Replace([IO.Path]::DirectorySeparatorChar, [char]47)
$containerOutput = "/app/benchmarks/results/$relativeOutput"
$outputExisted = [IO.File]::Exists($output)
$benchmarkCompleted = $false
$runningOnWindows = [System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT

$previous = @{
  SOURCE_COMMIT = $env:SOURCE_COMMIT
  DEPENDENCY_LOCK_DIGEST = $env:DEPENDENCY_LOCK_DIGEST
  EVIDENCE_CLEAN_TREE = $env:EVIDENCE_CLEAN_TREE
  EVIDENCE_IMAGE_DIGEST = $env:EVIDENCE_IMAGE_DIGEST
  EVIDENCE_PRODUCER = $env:EVIDENCE_PRODUCER
  CI_RUN_URL = $env:CI_RUN_URL
}

try {
  [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($output)) | Out-Null
  if (-not $outputExisted) {
    [IO.File]::WriteAllText($output, "")
  }
  if (-not $runningOnWindows) {
    & chmod 0666 -- $output
    if ($LASTEXITCODE -ne 0) { throw "Cannot grant the container write access to $output" }
  }

  $env:SOURCE_COMMIT = $sourceCommit
  $env:DEPENDENCY_LOCK_DIGEST = $lockDigest
  $env:EVIDENCE_CLEAN_TREE = $cleanTree.ToString().ToLowerInvariant()
  $env:EVIDENCE_PRODUCER = if ($env:GITHUB_ACTIONS -eq "true") { "github-actions" } else { "local" }
  if ($env:GITHUB_ACTIONS -eq "true" -and $env:GITHUB_REPOSITORY -and $env:GITHUB_RUN_ID) {
    $env:CI_RUN_URL = "https://github.com/$($env:GITHUB_REPOSITORY)/actions/runs/$($env:GITHUB_RUN_ID)"
  }

  Push-Location -LiteralPath $root
  try {
    Invoke-Checked @("compose", "-f", "docker-compose.real.yml", "build", "streams-app")
    $imageDigest = (& docker image inspect "kafka-streams-demo:local" --format "{{.Id}}").Trim()
    if ($LASTEXITCODE -ne 0 -or $imageDigest -notmatch "^sha256:[0-9a-f]{64}$") {
      throw "Cannot resolve application image digest"
    }
    $env:EVIDENCE_IMAGE_DIGEST = $imageDigest

    Invoke-Checked @("compose", "-f", "docker-compose.real.yml", "up", "-d", "--wait", "kafka")
    Invoke-Checked @(
      "compose", "-f", "docker-compose.real.yml", "run", "--rm", "streams-app",
      "broker-benchmark", $Records.ToString(), $Warmups.ToString(), $Repeats.ToString(), $containerOutput
    )
    $benchmarkCompleted = $true
  } finally {
    & docker compose -f docker-compose.real.yml down --volumes --remove-orphans
    Pop-Location
  }
} finally {
  $env:SOURCE_COMMIT = $previous.SOURCE_COMMIT
  $env:DEPENDENCY_LOCK_DIGEST = $previous.DEPENDENCY_LOCK_DIGEST
  $env:EVIDENCE_CLEAN_TREE = $previous.EVIDENCE_CLEAN_TREE
  $env:EVIDENCE_IMAGE_DIGEST = $previous.EVIDENCE_IMAGE_DIGEST
  $env:EVIDENCE_PRODUCER = $previous.EVIDENCE_PRODUCER
  $env:CI_RUN_URL = $previous.CI_RUN_URL
  if (-not $runningOnWindows -and [IO.File]::Exists($output)) {
    & chmod 0644 -- $output | Out-Null
    $global:LASTEXITCODE = 0
  }
  if (-not $benchmarkCompleted -and -not $outputExisted -and [IO.File]::Exists($output)) {
    [IO.File]::Delete($output)
  }
}

Write-Host "broker_evidence=$output"
