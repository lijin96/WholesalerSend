$ErrorActionPreference = "Stop"

$base = "d:\AndroidStudioProjects\WholesalerSend\app\src\main\res"
$defaultDir = Join-Path $base "values"
$allDirs = Get-ChildItem $base -Directory | Where-Object { $_.Name -like "values*" }

function Get-ResNames([string]$dirPath) {
    $set = @{}
    Get-ChildItem $dirPath -Filter "*.xml" -File | ForEach-Object {
        try {
            [xml]$x = Get-Content $_.FullName -Raw -Encoding UTF8
            if ($null -eq $x.resources) { return }
            foreach ($n in $x.resources.ChildNodes) {
                if ($n.NodeType -ne [System.Xml.XmlNodeType]::Element) { continue }
                $nameAttr = $n.Attributes["name"]
                if ($null -ne $nameAttr -and $nameAttr.Value) {
                    $key = "$($n.Name):$($nameAttr.Value)"
                    $set[$key] = $true
                } elseif ($n.Name -eq "item") {
                    $typeAttr = $n.Attributes["type"]
                    $name2 = $n.Attributes["name"]
                    if ($null -ne $typeAttr -and $null -ne $name2) {
                        $key = "item-$($typeAttr.Value):$($name2.Value)"
                        $set[$key] = $true
                    }
                }
            }
        } catch {
            # ignore parse errors per file
        }
    }
    return $set
}

$defaultSet = Get-ResNames $defaultDir

foreach ($d in $allDirs) {
    if ($d.Name -eq "values") { continue }
    $curr = Get-ResNames $d.FullName
    $missing = @()
    foreach ($k in $curr.Keys) {
        if (-not $defaultSet.ContainsKey($k)) {
            $missing += $k
        }
    }
    if ($missing.Count -gt 0) {
        Write-Output "[$($d.Name)] missing in values: $($missing.Count)"
        $missing | Sort-Object | ForEach-Object { Write-Output "  $_" }
    } else {
        Write-Output "[$($d.Name)] missing in values: 0"
    }
}

