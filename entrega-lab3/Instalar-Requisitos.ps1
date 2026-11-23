[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('Central','Webapp')][string]$Rol,
    [switch]$SoloPlan,
    [string]$JavaRuta,
    [string]$PostgresRuta,
    [string]$TomcatRuta,
    [string]$TomcatDestino
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Requisitos-Comunes.ps1')
$datos = Get-RequisitosManifiesto
if (![Environment]::Is64BitOperatingSystem -or $datos.Architecture -ne 'x64') {
    throw 'Este paquete de instalación requiere Windows x64.'
}
if (!$TomcatDestino) { $TomcatDestino = Join-Path $env:LOCALAPPDATA ("EventosUyTools/" + $datos.Tomcat.DirectoryName) }
$estado = @(Get-RequisitosEstado -Rol $Rol -JavaRuta $JavaRuta -PostgresRuta $PostgresRuta -TomcatRuta $TomcatRuta -TomcatDestino $TomcatDestino)
$pendientes = @($estado | Where-Object { !$_.Compatible })
Write-Host "Requisitos para $Rol (Maven no es necesario):"
$estado | Format-Table Componente, Requerido, Version, Compatible, Ruta -AutoSize
if (!$pendientes.Count) {
    Write-Host 'Todos los requisitos están disponibles. No se descargó ni instaló nada.'
    return
}

if ($Rol -eq 'Central' -and @($pendientes | Where-Object Componente -EQ 'PostgreSQL').Count) {
    $otros = @(Get-Service -Name 'postgresql*' -ErrorAction SilentlyContinue)
    if ($otros.Count) {
        throw 'Hay un servicio PostgreSQL, pero no se localizó psql 17. Indicar -PostgresRuta; no se instalará otra instancia automáticamente.'
    }
}
if ($Rol -eq 'Webapp' -and @($pendientes | Where-Object Componente -EQ 'Tomcat').Count) {
    if ($TomcatRuta -and (Test-Path -LiteralPath $TomcatRuta)) {
        throw 'La instalación indicada en -TomcatRuta no es Tomcat 11 compatible. No se modifica.'
    }
    if (Test-Path -LiteralPath $TomcatDestino) {
        throw "El destino $TomcatDestino ya existe y no es Tomcat 11 válido. No se sobrescribe."
    }
    if (@(Get-Service -Name '*tomcat*' -ErrorAction SilentlyContinue).Count) {
        throw 'Se detectó un servicio Tomcat existente. Indicar -TomcatRuta para verificarlo antes de instalar otro.'
    }
}

Write-Host 'Plan de instalación:'
foreach ($item in $pendientes) {
    switch ($item.Componente) {
        'Java' { Write-Host "  Java 25: WinGet $($datos.Java.WingetId), sólo si no se detectó una versión 25." }
        'PostgreSQL' { Write-Host "  PostgreSQL 17: WinGet $($datos.PostgreSQL.WingetId), instalador interactivo de EnterpriseDB." }
        'Tomcat' { Write-Host "  Tomcat $($datos.Tomcat.Version): ZIP oficial Apache a $TomcatDestino, SHA-512 verificado." }
    }
}
Write-Host 'No se instalarán Maven, PostgreSQL en PC2 ni Tomcat en PC1.'
Write-Host 'No se crean bases, usuarios, certificados ni configuraciones de la aplicación.'
if ($SoloPlan) { Write-Host 'SoloPlan: ningún cambio realizado.'; return }
if (Test-ReinicioPendiente) { throw 'Windows indica un reinicio pendiente. Reiniciar antes de instalar y volver a ejecutar.' }

$wingetNecesario = @($pendientes | Where-Object { $_.Componente -in @('Java','PostgreSQL') })
if ($wingetNecesario.Count) {
    $winget = Get-Command winget.exe -ErrorAction SilentlyContinue
    if (!$winget) {
        throw 'Falta WinGet (App Installer). Instalarlo desde Microsoft Store o la versión oficial de Microsoft y volver a ejecutar.'
    }
    foreach ($item in $wingetNecesario) {
        $paquete = $datos[$item.Componente]
        Write-Host "Comprobando disponibilidad en la fuente winget: $($paquete.WingetId)"
        & $winget.Source show --id $paquete.WingetId --exact --source winget
        if ($LASTEXITCODE -ne 0) { throw "No se pudo verificar el paquete $($paquete.WingetId). No se instaló nada." }
    }
}
$respuesta = Read-Host 'Para instalar exactamente el plan mostrado, escribir INSTALAR'
if ($respuesta -cne 'INSTALAR') { Write-Host 'Instalación cancelada sin cambios.'; return }

foreach ($item in $pendientes) {
    switch ($item.Componente) {
        'Java' {
            & $winget.Source install --id $datos.Java.WingetId --exact --source winget --no-upgrade --interactive
            if ($LASTEXITCODE -ne 0) { throw 'Falló o se canceló la instalación de Java. Revisar WinGet.' }
        }
        'PostgreSQL' {
            & $winget.Source install --id $datos.PostgreSQL.WingetId --exact --source winget --no-upgrade --interactive
            if ($LASTEXITCODE -ne 0) { throw 'Falló o se canceló la instalación de PostgreSQL. Revisar WinGet.' }
        }
        'Tomcat' {
            $uri = [Uri]$datos.Tomcat.Url
            if ($uri.Scheme -ne 'https' -or $uri.Host -ne 'downloads.apache.org' -or $datos.Tomcat.Sha512 -notmatch '^[0-9a-fA-F]{128}$') {
                throw 'Origen o hash de Tomcat inválido en requirements.psd1.'
            }
            $padre = [IO.Path]::GetFullPath((Split-Path -Parent $TomcatDestino))
            $destino = [IO.Path]::GetFullPath($TomcatDestino)
            if (!$destino.StartsWith($padre + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
                throw 'Destino de Tomcat fuera de la carpeta prevista.'
            }
            if (Test-Path -LiteralPath $destino) { throw 'El destino de Tomcat apareció durante la instalación. No se sobrescribe.' }
            New-Item -ItemType Directory -Path $padre -Force | Out-Null
            $zip = Join-Path $env:TEMP ("eventosuy-tomcat-" + [guid]::NewGuid().ToString('N') + '.zip')
            $temporal = Join-Path $padre ('.eventosuy-tomcat-' + [guid]::NewGuid().ToString('N'))
            try {
                Write-Host "Descargando Tomcat desde $uri"
                Invoke-WebRequest -Uri $uri -OutFile $zip -UseBasicParsing
                $real = (Get-FileHash -Algorithm SHA512 -LiteralPath $zip).Hash
                if ($real -ine $datos.Tomcat.Sha512) { throw 'SHA-512 de Tomcat no coincide. El ZIP no se instalará.' }
                New-Item -ItemType Directory -Path $temporal | Out-Null
                Expand-Archive -LiteralPath $zip -DestinationPath $temporal
                $extraido = Join-Path $temporal $datos.Tomcat.DirectoryName
                $version = Get-VersionTomcat $extraido
                if ($version -ne "$($datos.Tomcat.Version).0") { throw "ZIP inesperado: Tomcat $version" }
                if (Test-Path -LiteralPath $destino) { throw 'El destino de Tomcat apareció durante la extracción. No se sobrescribe.' }
                Move-Item -LiteralPath $extraido -Destination $destino
                Write-Host "Tomcat $version instalado en $destino"
            } finally {
                if (Test-Path -LiteralPath $zip) { Remove-Item -LiteralPath $zip -Force }
                $temporalReal = [IO.Path]::GetFullPath($temporal)
                if ($temporalReal.StartsWith($padre + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase) -and
                    (Test-Path -LiteralPath $temporalReal)) {
                    Remove-Item -LiteralPath $temporalReal -Recurse -Force
                }
            }
        }
    }
    if (Test-ReinicioPendiente) { throw 'La instalación solicita reiniciar Windows. Reiniciar y volver a ejecutar el script.' }
}
$final = @(Get-RequisitosEstado -Rol $Rol -JavaRuta $JavaRuta -PostgresRuta $PostgresRuta -TomcatRuta $TomcatRuta -TomcatDestino $TomcatDestino)
$final | Format-Table Componente, Version, Compatible, Ruta -AutoSize
if (@($final | Where-Object { !$_.Compatible }).Count) {
    throw 'La instalación terminó, pero un requisito no pudo verificarse. Revisar rutas y el registro del instalador.'
}
Write-Host 'Requisitos completos. Continuar con la configuración privada y despliegue del manual.'
