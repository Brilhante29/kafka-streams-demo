param(
  [Parameter(Mandatory = $true)]
  [string]$Path,
  [string]$ExpectedBenchmarkId = "",
  [switch]$RequireClean
)

$ErrorActionPreference = "Stop"

function Fail([string]$Message) {
  throw "Benchmark evidence invalid: $Message"
}

function Require-Property($Object, [string]$Name, [string]$Context) {
  if ($null -eq $Object -or $Object.PSObject.Properties.Name -notcontains $Name) {
    Fail "$Context is missing $Name"
  }
}

function Require-Digest([string]$Value, [string]$Context) {
  if ($Value -notmatch '^sha256:[0-9a-f]{64}$') { Fail "$Context must be a sha256 digest" }
}

$resolved = (Resolve-Path -LiteralPath $Path).Path
try {
  $evidence = Get-Content -Raw -LiteralPath $resolved | ConvertFrom-Json
} catch {
  Fail "JSON parse failed: $($_.Exception.Message)"
}

foreach ($name in @('schema_version','run_id','project','benchmark_id','workload','metrics','execution','environment','provenance','comparability_key')) {
  Require-Property $evidence $name 'root'
}
if ([int]$evidence.schema_version -ne 2) { Fail 'schema_version must be 2' }
$uuid = [Guid]::Empty
if (-not [Guid]::TryParse([string]$evidence.run_id, [ref]$uuid)) { Fail 'run_id must be a UUID' }
if ([string]$evidence.project -ne 'kafka-streams-demo') { Fail 'project must be kafka-streams-demo' }
if ([string]$evidence.benchmark_id -notmatch '^[a-z0-9][a-z0-9._-]*$') { Fail 'benchmark_id is malformed' }
if ($ExpectedBenchmarkId -and [string]$evidence.benchmark_id -ne $ExpectedBenchmarkId) {
  Fail "benchmark_id must be $ExpectedBenchmarkId"
}

foreach ($name in @('version','fixture_digest','config_digest','warmup_iterations','measured_iterations','concurrency')) {
  Require-Property $evidence.workload $name 'workload'
}
Require-Digest ([string]$evidence.workload.fixture_digest) 'workload.fixture_digest'
Require-Digest ([string]$evidence.workload.config_digest) 'workload.config_digest'
if ([int]$evidence.workload.warmup_iterations -lt 0) { Fail 'warmup_iterations must be non-negative' }
if ([int]$evidence.workload.measured_iterations -lt 1) { Fail 'measured_iterations must be positive' }
if ([int]$evidence.workload.concurrency -lt 1) { Fail 'concurrency must be positive' }

$metrics = @($evidence.metrics)
if ($metrics.Count -lt 1) { Fail 'metrics must not be empty' }
foreach ($metric in $metrics) {
  foreach ($name in @('name','value','unit','direction','samples','failures','summary')) {
    Require-Property $metric $name "metric $($metric.name)"
  }
  if ([string]::IsNullOrWhiteSpace([string]$metric.name)) { Fail 'metric name must not be blank' }
  if ([string]::IsNullOrWhiteSpace([string]$metric.unit)) { Fail "metric $($metric.name) unit must not be blank" }
  if (@('higher_is_better','lower_is_better','target') -notcontains [string]$metric.direction) {
    Fail "metric $($metric.name) direction is invalid"
  }
  if (@($metric.samples).Count -lt 1) { Fail "metric $($metric.name) has no samples" }
  if ([int]$metric.failures -lt 0) { Fail "metric $($metric.name) failures must be non-negative" }
}
if (@($metrics[0].samples).Count -ne [int]$evidence.workload.measured_iterations) {
  Fail 'primary metric sample count must match measured_iterations'
}
if ([double]$metrics[0].value -le 0) { Fail 'primary metric must be positive' }
$invariant = @($metrics | Where-Object { $_.name -eq 'output_invariant_ratio' })
if ($invariant.Count -ne 1 -or [double]$invariant[0].value -ne 1.0 -or [int]$invariant[0].failures -ne 0) {
  Fail 'output_invariant_ratio must exist once with value 1.0 and zero failures'
}

foreach ($name in @('command','started_at','duration_seconds','exit_code','repeat')) {
  Require-Property $evidence.execution $name 'execution'
}
if ([int]$evidence.execution.exit_code -ne 0) { Fail 'execution.exit_code must be zero' }
if ([int]$evidence.execution.repeat -ne [int]$evidence.workload.measured_iterations) {
  Fail 'execution.repeat must match measured_iterations'
}
if ([double]$evidence.execution.duration_seconds -lt 0) { Fail 'duration_seconds must be non-negative' }
$startedAt = [DateTimeOffset]::MinValue
if (-not [DateTimeOffset]::TryParse([string]$evidence.execution.started_at, [ref]$startedAt)) {
  Fail 'execution.started_at must be an ISO-8601 timestamp'
}

foreach ($name in @('runtime','architecture','hardware_class')) {
  Require-Property $evidence.environment $name 'environment'
  if ([string]::IsNullOrWhiteSpace([string]$evidence.environment.$name)) { Fail "environment.$name must not be blank" }
}

foreach ($name in @('source_commit','clean_tree','image_ref','image_digest','dependency_lock_digest','producer','artifact_digest')) {
  Require-Property $evidence.provenance $name 'provenance'
}
if ([string]$evidence.provenance.source_commit -notmatch '^[0-9a-f]{40}$') { Fail 'source_commit must be a full Git SHA' }
Require-Digest ([string]$evidence.provenance.image_digest) 'provenance.image_digest'
Require-Digest ([string]$evidence.provenance.dependency_lock_digest) 'provenance.dependency_lock_digest'
Require-Digest ([string]$evidence.provenance.artifact_digest) 'provenance.artifact_digest'
if (@('local','github-actions','other-ci') -notcontains [string]$evidence.provenance.producer) { Fail 'producer is invalid' }
if ($RequireClean) {
  if (-not [bool]$evidence.provenance.clean_tree) { Fail 'clean_tree must be true for release evidence' }
  if ([string]$evidence.provenance.source_commit -eq ('0' * 40)) { Fail 'release evidence cannot use an unknown source commit' }
  if ([string]$evidence.provenance.image_digest -eq ('sha256:' + ('0' * 64))) { Fail 'release evidence cannot use an unknown image digest' }
}
if ([string]$evidence.comparability_key -notmatch '^[a-z0-9][a-z0-9._:-]*$') { Fail 'comparability_key is malformed' }

Write-Host "benchmark_validation=passed"
Write-Host "benchmark_id=$($evidence.benchmark_id)"
Write-Host "primary_metric=$($metrics[0].name)"
Write-Host "primary_value=$($metrics[0].value)"
Write-Host "samples=$(@($metrics[0].samples).Count)"
