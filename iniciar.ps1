param([switch]$Compilar)
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME) { $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr' }
$jarAgro='target/agro-api-examen.jar'
$fuenteAgro=Get-ChildItem src/main -Recurse -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$recompilarAgro=$Compilar -or !(Test-Path $jarAgro)
if (!$recompilarAgro) { $recompilarAgro=$fuenteAgro.LastWriteTime -gt (Get-Item $jarAgro).LastWriteTime }
if ($recompilarAgro) {
    $mavenLocal=Join-Path $PSScriptRoot '../../work/maven/apache-maven-3.9.16/bin/mvn.cmd'
    $repoLocal=Join-Path $PSScriptRoot '../../work/maven/repository'
    $ErrorActionPreference='Continue'
    if(Test-Path $mavenLocal) { & $mavenLocal -B "-Dmaven.repo.local=$repoLocal" '-Dagro.jar=agro-api-examen' -DskipTests package }
    else { & .\mvnw.cmd -B '-Dagro.jar=agro-api-examen' -DskipTests package }
    $codigoAgro=$LASTEXITCODE
    $ErrorActionPreference='Stop'
    if($codigoAgro -ne 0){throw 'No se pudo compilar AgroAPI'}
}
& "$env:JAVA_HOME\bin\java.exe" -jar $jarAgro
