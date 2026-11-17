$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$jdkBin = Join-Path $env:ProgramFiles 'Java\jdk-25.0.4.1\bin'
$jpackage = Join-Path $jdkBin 'jpackage.exe'
$jar = Join-Path $projectRoot 'target\SERSProject-1.0-SNAPSHOT.jar'
$inputDir = Join-Path $projectRoot 'packaging\input'
$outputDir = Join-Path $projectRoot 'dist'

if (-not (Test-Path -LiteralPath $jpackage)) {
    throw "Could not find jpackage at $jpackage. Install a JDK and update the jdkBin path in this script."
}

if (-not (Test-Path -LiteralPath $jar)) {
    throw 'The application JAR is missing. Build the project in NetBeans first, then run this script again.'
}

New-Item -ItemType Directory -Force -Path $inputDir, $outputDir | Out-Null
Copy-Item -LiteralPath $jar -Destination $inputDir -Force

$m2 = Join-Path $env:USERPROFILE '.m2\repository'
$dependencyJars = @(
    (Join-Path $m2 'org\mongodb\mongodb-driver-sync\5.11.1\mongodb-driver-sync-5.11.1.jar'),
    (Join-Path $m2 'org\mongodb\mongodb-driver-core\5.11.1\mongodb-driver-core-5.11.1.jar'),
    (Join-Path $m2 'org\mongodb\bson\5.11.1\bson-5.11.1.jar'),
    (Join-Path $m2 'org\mongodb\bson-record-codec\5.11.1\bson-record-codec-5.11.1.jar'),
    (Join-Path $m2 'org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar')
)

foreach ($dependency in $dependencyJars) {
    if (-not (Test-Path -LiteralPath $dependency)) {
        throw "Missing Maven dependency: $dependency. Build the project in NetBeans while online, then retry."
    }
    Copy-Item -LiteralPath $dependency -Destination $inputDir -Force
}

& $jpackage `
    --type app-image `
    --name 'SERS Event Registration System' `
    --input $inputDir `
    --main-jar 'SERSProject-1.0-SNAPSHOT.jar' `
    --main-class 'javamongo1.sersproject.SERSProject' `
    --dest $outputDir `
    --app-version '1.0.0' `
    --vendor 'Ramon Magsaysay Memorial Colleges' `
    --win-console

if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

Write-Host "App created at: $(Join-Path $outputDir 'SERS Event Registration System')"
Write-Host 'Open that folder and double-click SERS Event Registration System.exe.'
Write-Host 'To add a desktop shortcut, right-click the EXE and choose Show more options > Send to > Desktop (create shortcut).'
