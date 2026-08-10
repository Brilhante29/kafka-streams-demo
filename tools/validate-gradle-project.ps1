param(
  [string]$RepoPath = ".",
  [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path -LiteralPath $RepoPath).Path
$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure {
  param([string]$Message)
  $script:failures.Add($Message)
}

function Require-File {
  param([string]$RelativePath)
  if (-not (Test-Path -LiteralPath (Join-Path $root $RelativePath) -PathType Leaf)) {
    Add-Failure "Missing Gradle file: $RelativePath"
  }
}

function Read-ManifestJvmScalar {
  param([string]$Content, [string]$Key)
  $section = [regex]::Match($Content, "(?ms)^jvm:\s*\r?\n(?<body>.*?)(?=^\S|\z)")
  if (-not $section.Success) { return "" }
  $value = [regex]::Match(
    $section.Groups["body"].Value,
    "(?m)^\s{2}" + [regex]::Escape($Key) + ":\s*([^\r\n#]+)"
  )
  if (-not $value.Success) { return "" }
  return $value.Groups[1].Value.Trim().Trim('"').Trim("'")
}

$buildKts = Join-Path $root "build.gradle.kts"
$buildGroovy = Join-Path $root "build.gradle"
if (-not (Test-Path -LiteralPath $buildKts -PathType Leaf) -and -not (Test-Path -LiteralPath $buildGroovy -PathType Leaf)) {
  Write-Host "gradle_project=false"
  exit 0
}
if (Test-Path -LiteralPath $buildGroovy -PathType Leaf) {
  Add-Failure "JVM projects must use Gradle Kotlin DSL; build.gradle is not allowed"
}

foreach ($required in @(
  "build.gradle.kts",
  "settings.gradle.kts",
  "gradlew",
  "gradlew.bat",
  "gradle/wrapper/gradle-wrapper.jar",
  "gradle/wrapper/gradle-wrapper.properties"
)) {
  Require-File $required
}

$propertiesPath = Join-Path $root "gradle/wrapper/gradle-wrapper.properties"
$wrapperVersion = ""
$distributionShaValue = ""
if (Test-Path -LiteralPath $propertiesPath -PathType Leaf) {
  $properties = Get-Content -Raw -LiteralPath $propertiesPath
  $urlMatch = [regex]::Match($properties, "(?m)^distributionUrl=.*gradle-([0-9]+(?:\.[0-9]+){1,2})-bin\.zip\s*$")
  if (-not $urlMatch.Success) {
    Add-Failure "Gradle distributionUrl must pin a released binary-only version"
  } else {
    $wrapperVersion = $urlMatch.Groups[1].Value
  }
  $distributionSha = [regex]::Match($properties, "(?m)^distributionSha256Sum=([a-fA-F0-9]{64})\s*$")
  if (-not $distributionSha.Success) {
    Add-Failure "gradle-wrapper.properties must pin distributionSha256Sum"
  } else {
    $distributionShaValue = $distributionSha.Groups[1].Value.ToLowerInvariant()
  }

  $knownWrappers = @{
    "8.10.2" = @{
      Distribution = "31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26"
      Jar = "2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046"
    }
    "8.12" = @{
      Distribution = "7a00d51fb93147819aab76024feece20b6b84e420694101f276be952e08bef03"
      Jar = "2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046"
    }
    "9.3.0" = @{
      Distribution = "0d585f69da091fc5b2beced877feab55a3064d43b8a1d46aeb07996b0915e0e0"
      Jar = "b3a875ddc1f044746e1b1a55f645584505f4a10438c1afea9f15e92a7c42ec13"
    }
  }
  if ($wrapperVersion -and -not $knownWrappers.ContainsKey($wrapperVersion)) {
    Add-Failure "Gradle $wrapperVersion is not in the reviewed wrapper checksum registry"
  } elseif ($wrapperVersion) {
    if ($distributionSha.Success -and $distributionSha.Groups[1].Value.ToLowerInvariant() -ne $knownWrappers[$wrapperVersion].Distribution) {
      Add-Failure "Gradle distribution checksum does not match the reviewed $wrapperVersion checksum"
    }
    $jarPath = Join-Path $root "gradle/wrapper/gradle-wrapper.jar"
    if (Test-Path -LiteralPath $jarPath -PathType Leaf) {
      $jarSha = (Get-FileHash -LiteralPath $jarPath -Algorithm SHA256).Hash.ToLowerInvariant()
      if ($jarSha -ne $knownWrappers[$wrapperVersion].Jar) {
        Add-Failure "Gradle wrapper JAR checksum does not match the reviewed $wrapperVersion checksum"
      }
    }
  }
}

$manifestVersion = ""
$manifestJvmLanguage = ""
$manifestToolchain = ""
$manifestWrapperVersion = ""
$manifestDistributionSha = ""
$manifestPath = Join-Path $root "project.yaml"
if (Test-Path -LiteralPath $manifestPath -PathType Leaf) {
  $manifest = Get-Content -Raw -LiteralPath $manifestPath
  $versionMatch = [regex]::Match($manifest, "(?m)^manifest_version:\s*(\d+)\s*$")
  if ($versionMatch.Success) { $manifestVersion = $versionMatch.Groups[1].Value }
  $manifestJvmLanguage = Read-ManifestJvmScalar -Content $manifest -Key "language"
  $manifestToolchain = Read-ManifestJvmScalar -Content $manifest -Key "toolchain_version"
  $manifestWrapperVersion = Read-ManifestJvmScalar -Content $manifest -Key "gradle_wrapper_version"
  $manifestDistributionSha = Read-ManifestJvmScalar -Content $manifest -Key "wrapper_distribution_sha256"

  if ($manifestVersion -eq "2" -and -not $manifestJvmLanguage) {
    Add-Failure "Manifest v2 Gradle projects must declare the jvm decision block"
  }
  if ($manifestWrapperVersion -and $wrapperVersion -and $manifestWrapperVersion -ne $wrapperVersion) {
    Add-Failure "project.yaml jvm.gradle_wrapper_version does not match gradle-wrapper.properties"
  }
  if ($manifestDistributionSha -and $distributionShaValue -and $manifestDistributionSha.ToLowerInvariant() -ne $distributionShaValue) {
    Add-Failure "project.yaml jvm.wrapper_distribution_sha256 does not match gradle-wrapper.properties"
  }
}

$javaToolchain = $null
$kotlinToolchain = $null
$kotlinTarget = $null
if (Test-Path -LiteralPath $buildKts -PathType Leaf) {
  $build = Get-Content -Raw -LiteralPath $buildKts
  $javaSources = @(Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java -File -ErrorAction SilentlyContinue).Count
  $kotlinSources = @(Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.kt -File -ErrorAction SilentlyContinue).Count

  $javaToolchain = [regex]::Match($build, "JavaLanguageVersion\.of\((\d+)\)")
  $kotlinToolchain = [regex]::Match($build, "jvmToolchain\((\d+)\)")
  $kotlinTarget = [regex]::Match($build, "JvmTarget\.JVM_(\d+)")

  if ($javaSources -gt 0 -and -not $javaToolchain.Success) {
    Add-Failure "Java sources require an explicit JavaLanguageVersion toolchain"
  }
  if ($kotlinSources -gt 0 -and -not $kotlinToolchain.Success) {
    Add-Failure "Kotlin sources require an explicit jvmToolchain"
  }
  if ($kotlinSources -gt 0 -and -not $kotlinTarget.Success) {
    Add-Failure "Kotlin sources require an explicit compiler JvmTarget"
  }
  if ($javaToolchain.Success -and $kotlinToolchain.Success -and $javaToolchain.Groups[1].Value -ne $kotlinToolchain.Groups[1].Value) {
    Add-Failure "Java and Kotlin toolchains must target the same JVM version"
  }
  if ($kotlinToolchain.Success -and $kotlinTarget.Success -and $kotlinToolchain.Groups[1].Value -ne $kotlinTarget.Groups[1].Value) {
    Add-Failure "Kotlin jvmToolchain and compiler JvmTarget must match"
  }

  if ($manifestToolchain) {
    $declaredToolchains = @(
      if ($javaToolchain.Success) { $javaToolchain.Groups[1].Value }
      if ($kotlinToolchain.Success) { $kotlinToolchain.Groups[1].Value }
      if ($kotlinTarget.Success) { $kotlinTarget.Groups[1].Value }
    ) | Select-Object -Unique
    if ($declaredToolchains.Count -eq 0) {
      Add-Failure "project.yaml declares JVM $manifestToolchain but build.gradle.kts has no explicit toolchain"
    } elseif (@($declaredToolchains | Where-Object { $_ -ne $manifestToolchain }).Count -gt 0) {
      Add-Failure "project.yaml jvm.toolchain_version does not match build.gradle.kts"
    }
  }
  if ($manifestJvmLanguage -eq "java" -and $kotlinSources -gt 0) {
    Add-Failure "project.yaml declares Java-only but Kotlin sources are present"
  }
  if ($manifestJvmLanguage -eq "kotlin" -and $javaSources -gt 0) {
    Add-Failure "project.yaml declares Kotlin-only but Java sources are present; declare mixed or remove the boundary"
  }
}

$git = Get-Command git -ErrorAction SilentlyContinue
if ($git) {
  try {
    $insideWorkTree = @(& git -C $root rev-parse --is-inside-work-tree 2>$null)
    $insideExit = $LASTEXITCODE
  } catch {
    $insideWorkTree = @()
    $insideExit = 1
  }
  $global:LASTEXITCODE = 0
  if ($insideExit -eq 0 -and $insideWorkTree -contains "true") {
    $modeLine = @(& git -C $root ls-files -s -- gradlew 2>$null)
    $global:LASTEXITCODE = 0
    if ($modeLine.Count -ne 1) {
      Add-Failure "gradlew must be tracked exactly once"
    } elseif ($modeLine[0] -notmatch "^100755\s") {
      Add-Failure "gradlew must be tracked with executable mode 100755"
    }
  }
}

$dockerPath = Join-Path $root "Dockerfile"
if (Test-Path -LiteralPath $dockerPath -PathType Leaf) {
  $dockerText = Get-Content -Raw -LiteralPath $dockerPath
  if ($dockerText -match "(?m)^\s*RUN\s+(?:sudo\s+)?gradle(?:\s|$)") {
    Add-Failure "Dockerfile invokes global Gradle; use the repository Wrapper"
  }
  if ($dockerText -match "(?m)^\s*RUN\s+(?:sudo\s+)?mvn(?:\s|$)") {
    Add-Failure "Dockerfile invokes Maven in a Gradle-governed JVM repository"
  }
  if ($dockerText -notmatch "\./gradlew\s") {
    Add-Failure "Dockerfile must build with ./gradlew"
  }
  if ($manifestToolchain) {
    $jvmArgs = @([regex]::Matches($dockerText, "(?m)^\s*ARG\s+JVM_VERSION=(\d+)\s*$"))
    if ($jvmArgs.Count -eq 0) {
      Add-Failure "Dockerfile must declare a static ARG JVM_VERSION matching project.yaml"
    }
    foreach ($jvmArg in $jvmArgs) {
      if ($jvmArg.Groups[1].Value -ne $manifestToolchain) {
        Add-Failure "Dockerfile JVM_VERSION does not match project.yaml jvm.toolchain_version"
      }
    }
    $fromImages = @([regex]::Matches($dockerText, "(?m)^\s*FROM\s+([^\s]+)"))
    if ($fromImages.Count -eq 0) {
      Add-Failure "Dockerfile must declare at least one base image"
    }
    foreach ($fromImage in $fromImages) {
      if ($fromImage.Groups[1].Value -notmatch "\$\{JVM_VERSION\}") {
        Add-Failure "Every Dockerfile base image must derive its JVM tag from JVM_VERSION"
      }
    }
  }
}

$workflowFiles = @(Get-ChildItem -Path (Join-Path $root ".github/workflows") -File -ErrorAction SilentlyContinue)
if ($workflowFiles.Count -gt 0) {
  $workflowText = ($workflowFiles | ForEach-Object { Get-Content -Raw -LiteralPath $_.FullName }) -join [Environment]::NewLine
  $globalGradle = $workflowText -match "(?m)^\s*(?:-\s*)?run:\s+(?:sudo\s+)?gradle(?:\s|$)" -or
    $workflowText -match "(?ms)^\s*(?:-\s*)?run:\s*\|\s*\r?\n\s*(?:sudo\s+)?gradle(?:\s|$)"
  if ($globalGradle) {
    Add-Failure "GitHub Actions invokes global Gradle; use the repository Wrapper"
  }
  $globalMaven = $workflowText -match "(?m)^\s*(?:-\s*)?run:\s+(?:sudo\s+)?mvn(?:\s|$)" -or
    $workflowText -match "(?ms)^\s*(?:-\s*)?run:\s*\|\s*\r?\n\s*(?:sudo\s+)?mvn(?:\s|$)"
  if ($globalMaven) {
    Add-Failure "GitHub Actions invokes Maven in a Gradle-governed JVM repository"
  }

  $dedicatedValidation = $workflowText -match "gradle/actions/wrapper-validation@(?:v[3-9]|[0-9a-f]{40}\s*#\s*v[3-9])"
  $setupValidation = $workflowText -match "gradle/actions/setup-gradle@(?:v(?:[4-9]|[1-9][0-9]+)|[0-9a-f]{40}\s*#\s*v(?:[4-9]|[1-9][0-9]+))"
  if (-not $dedicatedValidation -and -not $setupValidation) {
    Add-Failure "CI must validate Wrapper integrity with setup-gradle v4+ or wrapper-validation"
  }
  if ($manifestToolchain) {
    if ($workflowText -notmatch "actions/setup-java@(?:v(?:[4-9]|[1-9][0-9]+)|[0-9a-f]{40}\s*#\s*v(?:[4-9]|[1-9][0-9]+))") {
      Add-Failure "GitHub Actions must use setup-java v4+ for the declared JVM toolchain"
    }
    $ciDeclarations = @([regex]::Matches($workflowText, "(?m)^\s*java-version:\s*([^\r\n#]+)"))
    if ($ciDeclarations.Count -eq 0) {
      Add-Failure "GitHub Actions must declare a static java-version matching project.yaml"
    }
    foreach ($ciDeclaration in $ciDeclarations) {
      $ciVersion = $ciDeclaration.Groups[1].Value.Trim().Trim('"').Trim("'")
      if ($ciVersion -notmatch "^\d+$") {
        Add-Failure "GitHub Actions java-version must be static so it can be compared with project.yaml"
      } elseif ($ciVersion -ne $manifestToolchain) {
        Add-Failure "GitHub Actions JVM version does not match project.yaml jvm.toolchain_version"
      }
    }
  }
}
if (-not $SkipBuild -and $failures.Count -eq 0) {
  $java = Get-Command java -ErrorAction SilentlyContinue
  if (-not $java) {
    Add-Failure "Java is required to execute the Gradle Wrapper; use -SkipBuild only for structural audits"
  } else {
    $runningOnWindows = [System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT
    $wrapper = if ($runningOnWindows) { Join-Path $root "gradlew.bat" } else { Join-Path $root "gradlew" }
    Push-Location -LiteralPath $root
    try {
      & $wrapper --no-daemon clean check
      if ($LASTEXITCODE -ne 0) {
        Add-Failure "Gradle clean check failed with exit code $LASTEXITCODE"
      }
      $global:LASTEXITCODE = 0
    } finally {
      Pop-Location
    }
  }
}

if ($failures.Count -gt 0) {
  $failures | ForEach-Object { Write-Error $_ -ErrorAction Continue }
  exit 1
}

Write-Host "gradle_project=true"
Write-Host "gradle_wrapper_version=$wrapperVersion"
Write-Host "gradle_validation=passed"
