$ErrorActionPreference = "Stop"
$layoutDir = Join-Path $PSScriptRoot "app\src\main\res\layout"
$files = Get-ChildItem -Path $layoutDir -Filter "*.xml" -File

# Avoid encoding issues with Chinese in .ps1: build label with [char] (works on Windows PowerShell 5+)
$scanLabel = 'android:text="' + [char]0x5F53 + [char]0x524D + [char]0x626B + [char]0x63CF + [char]0x6761 + [char]0x7801 + ' "'

$rxLabel = [regex]::new(
    '(' + [regex]::Escape($scanLabel) + '\r?\n)(\s*)(android:textSize="@dimen/(?:scaled_t_dp_30|show_title_size)")',
    [System.Text.RegularExpressions.RegexOptions]::Compiled
)

$rxTv = [regex]::new(
    'android:id="@\+id/tv_show_code"\r?\n(?:\s*android:[^\r\n]+\r?\n)*?\s*android:textSize="@dimen/(?:scaled_t_dp_30|show_title_size)"',
    [System.Text.RegularExpressions.RegexOptions]::Compiled
)

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$updated = 0

foreach ($f in $files) {
    $t = [System.IO.File]::ReadAllText($f.FullName)
    $orig = $t

    $t = $rxLabel.Replace($t, {
        param($m)
        return $m.Groups[1].Value + $m.Groups[2].Value + 'android:textSize="@dimen/scaled_t_dp_scan"'
    })

    $t = $rxTv.Replace($t, {
        param($m)
        $v = $m.Value
        $v = $v -replace 'android:textSize="@dimen/scaled_t_dp_30"', 'android:textSize="@dimen/scaled_t_dp_scan"'
        $v = $v -replace 'android:textSize="@dimen/show_title_size"', 'android:textSize="@dimen/scaled_t_dp_scan"'
        return $v
    })

    if ($t -ne $orig) {
        [System.IO.File]::WriteAllText($f.FullName, $t, $utf8NoBom)
        $updated++
        Write-Host "Updated: $($f.Name)"
    }
}

Write-Host "Files updated: $updated"
