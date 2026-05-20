$ErrorActionPreference = "Stop"

$APP_RES_DIR = "app\src\main\res"
$LAYOUT_DIR = Join-Path $APP_RES_DIR "layout"

$BASE_DIMENS_XML = Join-Path $APP_RES_DIR "values-1280x752\dimens.xml"
$TARGET_DIMENS_XML = Join-Path $APP_RES_DIR "values-800x480\dimens.xml"

$WIDTH_FACTOR = [decimal]800 / [decimal]1280     # 0.625
$HEIGHT_FACTOR = [decimal]480 / [decimal]752   # ~0.63829787234
$TEXT_FACTOR = ($WIDTH_FACTOR + $HEIGHT_FACTOR) / [decimal]2
$OTHER_FACTOR = $TEXT_FACTOR

# Matches attributes like: android:layout_width="150dp" / android:textSize='24dp'
# Groups: attr, q (quote), val, unit (dp|sp)
$pattern = "(?<attr>[a-zA-Z0-9_:\-]+)\s*=\s*(?<q>[""'])(?<val>-?\d+(?:\.\d+)?)(?<unit>dp|sp)\k<q>"
$regex = [regex]::new($pattern, [System.Text.RegularExpressions.RegexOptions]::Compiled)

function NormalizeNumForName([string]$s) {
    if ($s.StartsWith("-")) {
        $s = "neg" + $s.Substring(1)
    }
    $s = $s -replace "\.","_"
    $s = $s -replace "_0+$",""
    $s = $s -replace "_+$",""
    if ([string]::IsNullOrWhiteSpace($s)) { return "0" }
    return $s
}

function FormatScaled([decimal]$value) {
    # Round to 2 decimals (half up), then trim trailing zeros
    $rounded = [Math]::Round([double]$value, 2, [MidpointRounding]::AwayFromZero)
    $s = $rounded.ToString("0.##", [System.Globalization.CultureInfo]::InvariantCulture)
    return $s
}

function RoleForAttr([string]$attrName) {
    $local = ($attrName -split ":")[-1]
    $localLower = $local.ToLowerInvariant()

    if ($localLower.EndsWith("textsize")) {
        return @("t", $TEXT_FACTOR)
    }

    # Width-related
    if ($localLower -in @("layout_width","minwidth","maxwidth","width")) {
        return @("w", $WIDTH_FACTOR)
    }
    if ($localLower -in @("layout_height","minheight","maxheight","height")) {
        return @("h", $HEIGHT_FACTOR)
    }

    # margin/padding suffix heuristics
    if ($localLower.Contains("margin") -or $localLower.Contains("padding")) {
        if ($localLower.EndsWith("left") -or $localLower.EndsWith("right") -or $localLower.EndsWith("start") -or $localLower.EndsWith("end")) {
            return @("w", $WIDTH_FACTOR)
        }
        if ($localLower.EndsWith("top") -or $localLower.EndsWith("bottom")) {
            return @("h", $HEIGHT_FACTOR)
        }
    }

    return @("o", $OTHER_FACTOR)
}

function GetExistingDimenNames([string]$dimensXmlPath) {
    if (-not (Test-Path $dimensXmlPath)) { return @{} }
    $text = [System.IO.File]::ReadAllText($dimensXmlPath)
    $matches = [regex]::Matches($text, "<dimen\s+name=""([^""]+)""")
    $set = @{}
    foreach ($m in $matches) {
        $name = $m.Groups[1].Value
        $set[$name] = $true
    }
    return $set
}

function AppendDimensBeforeResourcesClose([string]$dimensXmlPath, [hashtable]$newDimensOrderedNameToValue) {
    if (-not (Test-Path $dimensXmlPath)) {
        throw "Dimens file not found: $dimensXmlPath"
    }
    $content = [System.IO.File]::ReadAllText($dimensXmlPath)
    if ($content -notmatch "</resources>\s*$") {
        throw "Cannot find closing </resources> in $dimensXmlPath"
    }
    $newLines = ""
    foreach ($name in ($newDimensOrderedNameToValue.Keys | Sort-Object)) {
        $val = $newDimensOrderedNameToValue[$name]
        $newLines += "    <dimen name=""$name"">$val</dimen>`r`n"
    }
    $content = [regex]::Replace($content, "</resources>\s*$", $newLines + "</resources>")
    [System.IO.File]::WriteAllText($dimensXmlPath, $content, [System.Text.Encoding]::UTF8)
}

if (-not (Test-Path $LAYOUT_DIR)) { throw "LAYOUT_DIR not found: $LAYOUT_DIR" }
if (-not (Test-Path $BASE_DIMENS_XML)) { throw "BASE_DIMENS_XML not found: $BASE_DIMENS_XML" }
if (-not (Test-Path $TARGET_DIMENS_XML)) { throw "TARGET_DIMENS_XML not found: $TARGET_DIMENS_XML" }

$generated = @{} # name -> @{ base="..dp"; target="..dp" }

$layoutFiles = Get-ChildItem -Path $LAYOUT_DIR -Recurse -Filter "*.xml"
Write-Host "Found layout xml files: $($layoutFiles.Count)"

foreach ($file in $layoutFiles) {
    $path = $file.FullName
    $content = [System.IO.File]::ReadAllText($path)

    $newContent = $regex.Replace($content, {
        param($m)
        $attr = $m.Groups["attr"].Value
        $q = $m.Groups["q"].Value
        $valStr = $m.Groups["val"].Value
        $unit = $m.Groups["unit"].Value

        $roleInfo = RoleForAttr $attr
        $role = $roleInfo[0]
        $factor = $roleInfo[1]

        $origDec = [decimal]$valStr
        $baseDec = $origDec * [decimal]1
        $targetDec = $origDec * $factor

        $nameNorm = NormalizeNumForName $valStr
        $name = "scaled_${role}_${unit}_$nameNorm"

        if (-not $generated.ContainsKey($name)) {
            $baseValStr = "$(FormatScaled $baseDec)$unit"
            $targetValStr = "$(FormatScaled $targetDec)$unit"
            $generated[$name] = @{ base = $baseValStr; target = $targetValStr }
        }

        return "$attr=$q`@dimen/$name$q"
    })

    if ($newContent -ne $content) {
        [System.IO.File]::WriteAllText($path, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Updated: $($file.FullName)"
    }
}

$baseExisting = GetExistingDimenNames $BASE_DIMENS_XML
$targetExisting = GetExistingDimenNames $TARGET_DIMENS_XML

$baseToAdd = @{}
$targetToAdd = @{}

foreach ($name in $generated.Keys) {
    if (-not $baseExisting.ContainsKey($name)) {
        $baseToAdd[$name] = $generated[$name].base
    }
    if (-not $targetExisting.ContainsKey($name)) {
        $targetToAdd[$name] = $generated[$name].target
    }
}

if ($baseToAdd.Count -gt 0) {
    AppendDimensBeforeResourcesClose $BASE_DIMENS_XML $baseToAdd
    Write-Host "Added to base dimen: $($baseToAdd.Count)"
}
if ($targetToAdd.Count -gt 0) {
    AppendDimensBeforeResourcesClose $TARGET_DIMENS_XML $targetToAdd
    Write-Host "Added to target dimen: $($targetToAdd.Count)"
}

Write-Host "Done. Generated dimen count: $($generated.Count)"

