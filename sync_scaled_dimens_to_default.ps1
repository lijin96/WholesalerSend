$ErrorActionPreference = "Stop"

$defaultDimensXml = "app\src\main\res\values\dimens.xml"
$sourceDimensXml = "app\src\main\res\values-1280x752\dimens.xml"

if (-not (Test-Path $defaultDimensXml)) { throw "Missing default: $defaultDimensXml" }
if (-not (Test-Path $sourceDimensXml)) { throw "Missing source: $sourceDimensXml" }

$defaultText = [IO.File]::ReadAllText($defaultDimensXml)
$sourceText = [IO.File]::ReadAllText($sourceDimensXml)

if ($defaultText -notmatch "</resources>\s*$") { throw "Cannot find closing </resources> in default file" }

$nameRegex = [regex]::new("<dimen\s+name=""([^""]+)""\s*>([^<]*)</dimen>", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)

$defaultNames = @{}
foreach ($m in $nameRegex.Matches($defaultText)) {
    $defaultNames[$m.Groups[1].Value] = $true
}

$toAppend = New-Object System.Collections.Generic.List[string]

foreach ($m in $nameRegex.Matches($sourceText)) {
    $name = $m.Groups[1].Value
    $value = $m.Groups[2].Value

    if ($name -like "scaled_*") {
        if (-not $defaultNames.ContainsKey($name)) {
            $toAppend.Add("    <dimen name=""$name"">$value</dimen>")
        }
    }
}

if ($toAppend.Count -gt 0) {
    $appendText = ($toAppend -join "`r`n") + "`r`n"
    $newDefaultText = [regex]::Replace($defaultText, "</resources>\s*$", $appendText + "</resources>")
    [IO.File]::WriteAllText($defaultDimensXml, $newDefaultText, [Text.Encoding]::UTF8)
}

Write-Host "Appended scaled dimens to default: $($toAppend.Count)"

