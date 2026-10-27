param([switch]$Compilar)
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME) { $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr' }
$jarAgro='target/agro-api-validado.jar'
if ($Compilar -or !(Test-Path $jarAgro)) {
    $mavenLocal=Join-Path $PSScriptRoot '../../work/maven/apache-maven-3.9.16/bin/mvn.cmd'
    $repoLocal=Join-Path $PSScriptRoot '../../work/maven/repository'
    if(Test-Path $mavenLocal) { & $mavenLocal -B "-Dmaven.repo.local=$repoLocal" '-Dagro.jar=agro-api-validado' -DskipTests package }
    else { & .\mvnw.cmd -B '-Dagro.jar=agro-api-validado' -DskipTests package }
    if($LASTEXITCODE -ne 0){throw 'No se pudo compilar AgroAPI'}
}
& "$env:JAVA_HOME\bin\java.exe" -jar $jarAgro
