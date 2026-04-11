# 在终端运行单元测试（不依赖 VS Code / Cursor 里的 Test Runner 面板）
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$mvnCmd = $null
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) {
    $mvnCmd = $mvn.Source
}
elseif (Test-Path "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd") {
    $mvnCmd = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd"
}

if (-not $mvnCmd) {
    Write-Host "未找到 Maven（mvn）。请先安装并加入 PATH，例如：" -ForegroundColor Yellow
    Write-Host "  winget install Apache.Maven --accept-package-agreements" -ForegroundColor Gray
    Write-Host "安装后重新打开终端，再在 software-code 目录执行本脚本或: mvn test" -ForegroundColor Gray
    exit 1
}

Write-Host "使用: $mvnCmd" -ForegroundColor Cyan
& $mvnCmd @args
exit $LASTEXITCODE
