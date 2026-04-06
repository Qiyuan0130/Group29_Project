# Build WAR and copy to Tomcat webapps. Data paths are set at build time in WEB-INF/app-settings.properties
# (points to this repo's data/ and uploads/cv/).
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$tomcatRoot = if ($env:CATALINA_HOME) { $env:CATALINA_HOME.TrimEnd('\') } else {
    "D:\Tomcat10\apache-tomcat-10.1.49"
}
if (-not (Test-Path (Join-Path $tomcatRoot "webapps"))) {
    Write-Error "Tomcat not found at: $tomcatRoot — set CATALINA_HOME or edit deploy-tomcat.ps1"
}
& (Join-Path $root "build-no-maven.ps1")
$war = Join-Path $root "build\java-web-json.war"
$webapps = Join-Path $tomcatRoot "webapps"
$destWar = Join-Path $webapps "java-web-json.war"
$exploded = Join-Path $webapps "java-web-json"
# Old exploded dir often stays stale if only the .war is overwritten — remove so Tomcat unpacks fresh HTML.
if (Test-Path $exploded) {
    Remove-Item -Path $exploded -Recurse -Force
}
if (Test-Path $destWar) {
    Remove-Item -Path $destWar -Force
}
Copy-Item -Path $war -Destination $destWar -Force
Write-Host "Deployed: $destWar"
Write-Host "If you use IntelliJ/Eclipse Run (not this script), redeploy the artifact there — it may use target/, not this webapps path."
Write-Host "Open: http://localhost:8080/java-web-json/  (jobs/users data: $((Join-Path $root 'data')))"
