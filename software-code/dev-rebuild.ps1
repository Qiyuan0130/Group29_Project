$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$buildDir = Join-Path $projectRoot "build"
$targetDir = Join-Path $projectRoot "target"

if (Test-Path $buildDir) {
  Remove-Item -Path $buildDir -Recurse -Force
}
if (Test-Path $targetDir) {
  Remove-Item -Path $targetDir -Recurse -Force
}

$tomcatHome = $env:TOMCAT_HOME
if ($tomcatHome -and (Test-Path $tomcatHome)) {
  $apps = @("java-web-json", "java-web-json.war")
  foreach ($app in $apps) {
    $webappsPath = Join-Path $tomcatHome ("webapps\" + $app)
    if (Test-Path $webappsPath) {
      Remove-Item -Path $webappsPath -Recurse -Force -ErrorAction SilentlyContinue
    }
  }

  $workPath = Join-Path $tomcatHome "work\Catalina"
  if (Test-Path $workPath) {
    Get-ChildItem -Path $workPath -Recurse -Directory -ErrorAction SilentlyContinue |
      Where-Object { $_.Name -eq "java-web-json" } |
      ForEach-Object {
        Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
      }
  }

  $tempPath = Join-Path $tomcatHome "temp"
  if (Test-Path $tempPath) {
    Get-ChildItem -Path $tempPath -Recurse -ErrorAction SilentlyContinue |
      Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
  }

  Write-Host "Tomcat cache cleaned under: $tomcatHome"
} else {
  Write-Host "TOMCAT_HOME not set or invalid. Skip Tomcat clean."
}

& (Join-Path $projectRoot "build-no-maven.ps1")
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

Write-Host "WAR built: $(Join-Path $buildDir "java-web-json.war")"
Write-Host "Next: In Community Server Connector click Publish or Restart. Then browser Ctrl+F5."
