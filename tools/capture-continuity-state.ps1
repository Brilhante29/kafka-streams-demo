param(
  [string[]]$RepoPaths = @("."),
  [string]$OutputPath = ".portfolio-control/CONTINUITY_STATE.md",
  [string[]]$NextActions = @()
)

$ErrorActionPreference = "Stop"

function Invoke-GitText {
  param([string]$Repo, [string[]]$Arguments)
  try {
    $output = @(& git -C $Repo @Arguments 2>$null)
    $exitCode = $LASTEXITCODE
  } catch {
    $output = @()
    $exitCode = 1
  }
  $global:LASTEXITCODE = 0
  if ($exitCode -ne 0) { return @() }
  return $output
}

$localPaths = [System.Collections.Generic.List[string]]::new()

function Register-LocalPath {
  param([string]$Value)
  if ($Value -and -not $script:localPaths.Contains($Value)) {
    $script:localPaths.Add($Value)
  }
}

function Sanitize-Line {
  param([string]$Value)
  if ($null -eq $Value) { return "" }
  $clean = $Value -replace 'https://[^/@\s]+@github\.com/', 'https://github.com/'
  $clean = $clean -replace '(ghp_|github_pat_)[A-Za-z0-9_]+', '[REDACTED]'
  foreach ($localPath in @($script:localPaths | Sort-Object Length -Descending)) {
    $clean = $clean -replace [regex]::Escape($localPath), '<local-path>'
    $clean = $clean -replace [regex]::Escape($localPath.Replace([char]92, [char]47)), '<local-path>'
  }
  $clean = $clean -replace '(?i)[A-Z]:\\Users\\[^\\\s]+\\[^\r\n]*', '<local-path>'
  $clean = $clean -replace '(?i)/(Users|home)/[^/\s]+/[^\r\n]*', '<local-path>'
  return $clean
}

$sections = [System.Collections.Generic.List[string]]::new()
$sections.Add("# Continuity State")
$sections.Add("")
$sections.Add("Generated: $([DateTimeOffset]::Now.ToString('o'))")
$sections.Add("Purpose: mechanical Git and worktree state for continuation. Read CURRENT_HANDOFF.md for engineering decisions.")
$sections.Add("")

foreach ($candidate in $RepoPaths) {
  $resolved = (Resolve-Path -LiteralPath $candidate).Path
  Register-LocalPath $resolved
  $topLevel = @(Invoke-GitText -Repo $resolved -Arguments @("rev-parse", "--show-toplevel"))
  if ($topLevel.Count -gt 0) { Register-LocalPath $topLevel[0] }
  $repoAlias = Split-Path -Leaf $resolved
  if (-not $repoAlias) { $repoAlias = "repository" }
  $sections.Add("## $repoAlias")
  $sections.Add("")
  if ($topLevel.Count -eq 0) {
    $sections.Add("- Git repository: no")
    $sections.Add("")
    continue
  }

  $branch = @(Invoke-GitText -Repo $resolved -Arguments @("branch", "--show-current"))
  $head = @(Invoke-GitText -Repo $resolved -Arguments @("rev-parse", "HEAD"))
  $origin = @(Invoke-GitText -Repo $resolved -Arguments @("remote", "get-url", "origin"))
  $status = @(Invoke-GitText -Repo $resolved -Arguments @("status", "--short"))
  $recent = @(Invoke-GitText -Repo $resolved -Arguments @("log", "-5", "--pretty=format:%h %s"))
  $worktrees = @(Invoke-GitText -Repo $resolved -Arguments @("worktree", "list", "--porcelain"))

  $sections.Add("- Git repository: yes")
  $sections.Add("- Repository alias: $repoAlias")
  $sections.Add("- Branch: $(if ($branch.Count) { Sanitize-Line $branch[0] } else { 'detached' })")
  $sections.Add("- Head: $(if ($head.Count) { Sanitize-Line $head[0] } else { 'unknown' })")
  $sections.Add("- Origin: $(if ($origin.Count) { Sanitize-Line $origin[0] } else { 'none' })")
  $sections.Add("- Dirty entries at capture: $($status.Count)")
  $sections.Add("")
  $sections.Add("### Working Tree")
  $sections.Add("")
  if ($status.Count -eq 0) {
    $sections.Add("- clean")
  } else {
    foreach ($line in @($status | Select-Object -First 20)) { $sections.Add("    $(Sanitize-Line $line)") }
    if ($status.Count -gt 20) { $sections.Add("    ... $($status.Count - 20) additional entries omitted; run git status --short in this worktree.") }
  }
  $sections.Add("")
  $sections.Add("### Recent Commits")
  $sections.Add("")
  if ($recent.Count -eq 0) {
    $sections.Add("- none")
  } else {
    foreach ($line in $recent) { $sections.Add("    $(Sanitize-Line $line)") }
  }
  $sections.Add("")
  $sections.Add("### Worktrees")
  $sections.Add("")
  if ($worktrees.Count -eq 0) {
    $sections.Add("- unavailable")
  } else {
    $worktreeIndex = 0
    foreach ($line in $worktrees) {
      if (-not $line) { continue }
      if ($line -match "^worktree\s+(.+)$") {
        $worktreeIndex++
        Register-LocalPath $Matches[1]
        $sections.Add("    worktree <local-worktree-$worktreeIndex>")
      } else {
        $sections.Add("    $(Sanitize-Line $line)")
      }
    }
  }
  $sections.Add("")
}

$sections.Add("## Next Actions")
$sections.Add("")
if ($NextActions.Count -eq 0) {
  $sections.Add("- Read .portfolio-control/CURRENT_HANDOFF.md and preserve its strict continuation order.")
} else {
  foreach ($action in $NextActions) { $sections.Add("- $(Sanitize-Line $action)") }
}

$target = if ([IO.Path]::IsPathRooted($OutputPath)) {
  $OutputPath
} else {
  Join-Path (Get-Location) $OutputPath
}
$parent = Split-Path -Parent $target
if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
[IO.File]::WriteAllLines($target, $sections, [Text.UTF8Encoding]::new($false))
Write-Host "continuity_state=$target"
