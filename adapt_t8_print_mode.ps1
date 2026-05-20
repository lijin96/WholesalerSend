$ErrorActionPreference = "Stop"

$javaDir = "d:\AndroidStudioProjects\WholesalerSend\app\src\main\java"
$files = Get-ChildItem $javaDir -Recurse -Filter "*.java" -File

$updated = 0

foreach ($f in $files) {
    if ($f.Name -eq "AboutActivity.java") {
        continue
    }

    $text = [System.IO.File]::ReadAllText($f.FullName)
    $orig = $text

    if ($text -notmatch "printbill\.print\(") {
        continue
    }

    $versionVar = $null
    if ($text -match "\bsysUserInfo\b") {
        $versionVar = "sysUserInfo"
    } elseif ($text -match "\bsysinfo\b") {
        $versionVar = "sysinfo"
    } else {
        # Unknown version holder in this file; skip safely.
        continue
    }

    $pattern = "(?m)^(?<indent>\s*)printbill\.print\(\s*[^,]+,\s*(?<args>.+?)\);\s*$"
    $newText = [regex]::Replace($text, $pattern, {
        param($m)
        $indent = $m.Groups["indent"].Value
        $args = $m.Groups["args"].Value
        return $indent + "if (" + $versionVar + ".getOldVersion().equals(""T8"")) {" + "`r`n" +
               $indent + "    printbill.prints(" + $args + ");" + "`r`n" +
               $indent + "} else {" + "`r`n" +
               $indent + "    " + $m.Value.Trim() + "`r`n" +
               $indent + "}"
    })
    if ($newText -ne $orig) {
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($f.FullName, $newText, $utf8NoBom)
        $updated++
        Write-Host "Updated: $($f.FullName)"
    }
}

Write-Host "Files updated: $updated"

