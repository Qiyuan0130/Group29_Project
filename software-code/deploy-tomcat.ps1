#Requires -Version 5.1
# 在本目录执行: Maven 打包 -> 停 Tomcat -> 覆盖 webapps 中的 WAR -> 启动 Tomcat
# 用法:
#   .\deploy-tomcat.ps1
#   .\deploy-tomcat.ps1 -OpenBrowser
#   .\deploy-tomcat.ps1 -TomcatHome "D:\path\to\apache-tomcat-10.1.49"
#   $env:TOMCAT_HOME="D:\..." ; .\deploy-tomcat.ps1
#   .\deploy-tomcat.ps1 -NoShutdown   # 不执行 shutdown（Tomcat 未运行时）
param(
  [string]$TomcatHome = "",
  [switch]$NoShutdown,
  [switch]$NoStart,
  [switch]$OpenBrowser
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not $TomcatHome) {
  if ($env:TOMCAT_HOME -and (Test-Path $env:TOMCAT_HOME)) {
    $TomcatHome = $env:TOMCAT_HOME
  } else {
    $TomcatHome = "D:\software\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
  }
}

$mvnCmd = $null
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) { $mvnCmd = $mvn.Source }
elseif (Test-Path "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd") {
  $mvnCmd = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd"
}
if (-not $mvnCmd) {
  Write-Error "未找到 Maven，请将 mvn 加入 PATH 或安装 Maven。"
  exit 1
}

$webapps = Join-Path $TomcatHome "webapps"
if (-not (Test-Path $webapps)) {
  Write-Error "Tomcat webapps 目录不存在: $webapps （请检查 -TomcatHome 或环境变量 TOMCAT_HOME）"
  exit 1
}

Write-Host "==> mvn clean package -DskipTests"
& $mvnCmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$war = Join-Path $PSScriptRoot "target\java-web-json.war"
if (-not (Test-Path $war)) {
  Write-Error "未生成 WAR: $war"
  exit 1
}

if (-not $NoShutdown) {
  $shutdown = Join-Path $TomcatHome "bin\shutdown.bat"
  if (Test-Path $shutdown) {
    Write-Host "==> Tomcat shutdown（若未在运行可忽略下方提示）"
    & cmd /c "`"$shutdown`"" | Out-Null
    Start-Sleep -Seconds 2
  }
}

$destWar = Join-Path $webapps "java-web-json.war"
$destDir = Join-Path $webapps "java-web-json"
if (Test-Path $destWar) { Remove-Item $destWar -Force }
if (Test-Path $destDir) { Remove-Item $destDir -Recurse -Force }

Write-Host "==> 部署: $war -> $destWar"
Copy-Item -Path $war -Destination $destWar -Force

$url = "http://localhost:8080/java-web-json/register.html"
if (-not $NoStart) {
  $startup = Join-Path $TomcatHome "bin\startup.bat"
  if (-not (Test-Path $startup)) {
    Write-Error "未找到: $startup"
    exit 1
  }
  Write-Host "==> Tomcat startup"
  Start-Process -FilePath $startup -WorkingDirectory (Split-Path $startup)
  if ($OpenBrowser) {
    Start-Sleep -Seconds 4
    Start-Process $url
  }
} else {
  Write-Host "已跳过启动（-NoStart）。请手动运行 bin\startup.bat"
}

Write-Host ""
Write-Host "完成。浏览器打开: $url"
if (-not $OpenBrowser -and -not $NoStart) {
  Write-Host "（可加参数 -OpenBrowser 自动打开浏览器）"
}
