param([Parameter(Mandatory=$true)][string]$PlantUmlJar,
      [string]$Java='java')
$ErrorActionPreference='Stop'
$PlantUmlJar=(Resolve-Path -LiteralPath $PlantUmlJar).Path
$esperado=Get-Content (Join-Path $PSScriptRoot 'herramientas.json') -Raw | ConvertFrom-Json
if((Get-FileHash -LiteralPath $PlantUmlJar -Algorithm SHA256).Hash -ne $esperado.sha256_jar){throw 'Versión/hash de PlantUML diferente: revisar herramientas.json.'}
$fuentes=Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'fuentes') -Filter '*.puml'
foreach($fuente in $fuentes){
    & $Java '-Djava.awt.headless=true' -jar $PlantUmlJar -charset UTF-8 -failfast2 -tpng -o '../imagenes' $fuente.FullName
    if($LASTEXITCODE -ne 0){throw "Falló el diagrama $($fuente.Name)"}
}
Write-Output "$($fuentes.Count) diagramas exportados. El JAR de herramienta no forma parte del proyecto."
