# Deploy WAR to Tomcat webapps. Removes exploded dir so static HTML/JS updates apply.
#
# Usage:
#   .\deploy-tomcat.ps1 -Maven                    # default: VS Code RSP Tomcat (what usually runs on :8080)
#   .\deploy-tomcat.ps1 -Maven -Target Software  # D:\software\apache-tomcat-10.1.49-...
#
param(
    [switch]$Maven,
    [ValidateSet("Rsp", "Software")]
    [string]$Target = "Rsp"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$tomcatRoots = @{
    Rsp = "C:\Users\22001\.rsp\redhat-community-server-connector\runtimes\installations\tomcat-11.0.0-M6\apache-tomcat-11.0.0-M6"
    Software = "D:\software\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
}

if ($env:CATALINA_HOME -and (Test-Path (Join-Path $env:CATALINA_HOME "webapps"))) {
    $tomcatRoot = $env:CATALINA_HOME.TrimEnd('\')
    Write-Host "Using CATALINA_HOME: $tomcatRoot" -ForegroundColor Cyan
} else {
    $tomcatRoot = $tomcatRoots[$Target]
    if (-not (Test-Path (Join-Path $tomcatRoot "webapps"))) {
        Write-Error "Tomcat not found at: $tomcatRoot — use -Target Rsp|Software or set CATALINA_HOME"
    }
    Write-Host "Using Tomcat ($Target): $tomcatRoot" -ForegroundColor Cyan
}

if ($Maven) {
    $war = Join-Path $root "target\java-web-json.war"
    if (-not (Test-Path $war)) {
        Write-Error "Missing $war — run: cd $root; mvn clean package -DskipTests"
    }
} else {
    & (Join-Path $root "build-no-maven.ps1")
    $war = Join-Path $root "build\java-web-json.war"
}

$webapps = Join-Path $tomcatRoot "webapps"
$destWar = Join-Path $webapps "java-web-json.war"
$exploded = Join-Path $webapps "java-web-json"

if (Test-Path $exploded) {
    Remove-Item -Path $exploded -Recurse -Force
    Write-Host "Removed old exploded app: $exploded" -ForegroundColor Gray
}
if (Test-Path $destWar) {
    Remove-Item -Path $destWar -Force
}
Copy-Item -Path $war -Destination $destWar -Force
Write-Host "Deployed: $destWar"
Write-Host "Restart the Tomcat you use in VS Code (Server Connector), then Ctrl+F5."
Write-Host "Verify page source contains: admin-dashboard-v2"
Write-Host "Open: http://localhost:8080/java-web-json/admin/dashboard.html"
