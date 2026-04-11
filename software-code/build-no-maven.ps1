# build-no-maven.ps1 - 支持 Java 编译的版本
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$buildDir = Join-Path $root "build"
$classesDir = Join-Path $buildDir "WEB-INF\classes"
$libDir = Join-Path $buildDir "WEB-INF\lib"
$warFile = Join-Path $buildDir "java-web-json.war"
$frontend = Join-Path $root "frontend"
$srcDir = Join-Path $root "src\main\java"
$webInfSrc = Join-Path $root "src\main\webapp\WEB-INF"

Write-Host "Building WAR with Java compilation..." -ForegroundColor Cyan

# 清理
if (Test-Path $buildDir) { 
    Remove-Item -Recurse -Force $buildDir 
}
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $libDir | Out-Null

# 复制前端文件
Copy-Item -Path "$frontend\*" -Destination $buildDir -Recurse -Force

# 复制 web.xml（从 src/main/webapp/WEB-INF 或创建默认）
$webInf = Join-Path $buildDir "WEB-INF"
$webXmlSrc = Join-Path $webInfSrc "web.xml"
$webXmlDest = Join-Path $webInf "web.xml"

if (Test-Path $webXmlSrc) {
    Copy-Item -Path $webXmlSrc -Destination $webXmlDest -Force
    Write-Host "Copied web.xml from src" -ForegroundColor Gray
} else {
    # 创建最小 web.xml（Jakarta EE 6）
    $webXml = @'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    <display-name>TA Recruitment System</display-name>
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
</web-app>
'@
    $webXml | Out-File -FilePath $webXmlDest -Encoding UTF8
    Write-Host "Created default web.xml" -ForegroundColor Yellow
}

# 编译 Java 文件（如果有）
if (Test-Path $srcDir) {
    $javaFiles = Get-ChildItem -Path $srcDir -Recurse -Filter "*.java"
    
    if ($javaFiles.Count -gt 0) {
        Write-Host "Found $($javaFiles.Count) Java files to compile..." -ForegroundColor Yellow
        
        # 下载必要的依赖
        $dependencies = @(
            @{ Name = "jakarta.servlet-api-6.0.0.jar"; Url = "https://repo.maven.apache.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar" },
            @{ Name = "jbcrypt-0.4.jar"; Url = "https://repo.maven.apache.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar" },
            @{ Name = "gson-2.10.1.jar"; Url = "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" }
        )
        
        foreach ($dep in $dependencies) {
            $jarPath = Join-Path $libDir $dep.Name
            if (-not (Test-Path $jarPath)) {
                Write-Host "Downloading $($dep.Name)..." -ForegroundColor Yellow
                try {
                    Invoke-WebRequest -Uri $dep.Url -OutFile $jarPath -TimeoutSec 30
                    Write-Host "Downloaded $($dep.Name)" -ForegroundColor Green
                } catch {
                    Write-Warning "Failed to download $($dep.Name)"
                }
            }
        }
        
        # 准备 classpath（包含所有下载的 JAR）
        $allJars = Get-ChildItem -Path $libDir -Filter "*.jar" | Select-Object -ExpandProperty FullName
        $classpath = ($allJars -join ";")
        if (Test-Path (Join-Path $root "target\classes")) {
            $classpath = "$classpath;$(Join-Path $root 'target\classes')"
        }
        
        # 编译所有 Java 文件
        Write-Host "Compiling Java files..." -ForegroundColor Yellow
        $javaFileList = $javaFiles.FullName -join " "
        
        # 使用 javac 编译（指定 UTF-8 编码）
        $compileCmd = "javac -encoding UTF-8 -cp `"$classpath`" -d `"$classesDir`" $javaFileList"
        Write-Host "Command: $compileCmd" -ForegroundColor Gray
        
        Invoke-Expression $compileCmd 2>&1 | ForEach-Object {
            Write-Host $_ -ForegroundColor $(if ($_ -match "error") { "Red" } else { "Gray" })
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Java compilation successful!" -ForegroundColor Green
        } else {
            Write-Warning "Java compilation had errors, continuing with static files only..."
        }
    } else {
        Write-Host "No Java files found in src/main/java" -ForegroundColor Gray
    }
}

# 打包 WAR
Write-Host "Packaging WAR file..." -ForegroundColor Cyan
Add-Type -AssemblyName System.IO.Compression.FileSystem
$files = Get-ChildItem -Recurse -Path $buildDir -File
$zip = [System.IO.Compression.ZipFile]::Open($warFile, "Create")

foreach ($file in $files) {
    $relativePath = $file.FullName.Substring($buildDir.Length + 1).Replace("\", "/")
    $entry = $zip.CreateEntry($relativePath)
    $entryStream = $entry.Open()
    $fileStream = [System.IO.File]::OpenRead($file.FullName)
    $fileStream.CopyTo($entryStream)
    $fileStream.Close()
    $entryStream.Close()
}

$zip.Dispose()

$warSize = [math]::Round((Get-Item $warFile).Length / 1KB, 2)
Write-Host "Built: $warFile" -ForegroundColor Green
Write-Host "Size: $warSize KB" -ForegroundColor Gray

# 验证 WAR 内容（使用 .NET 方法，避免外部命令）
Write-Host "`nWAR contents:" -ForegroundColor Cyan
try {
    $zipRead = [System.IO.Compression.ZipFile]::OpenRead($warFile)
    $entries = $zipRead.Entries | Select-Object -First 20
    foreach ($entry in $entries) {
        Write-Host "  $($entry.FullName)" -ForegroundColor Gray
    }
    $zipRead.Dispose()
} catch {
    Write-Host "  (Could not list WAR contents)" -ForegroundColor Yellow
}

Write-Host "`nBuild completed!" -ForegroundColor Green