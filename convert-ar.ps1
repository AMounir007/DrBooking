# Converts the UTF-8 Arabic source into an ASCII-safe .properties file using \uXXXX escapes.
# Java's Properties loader decodes these natively, so the result is immune to the
# ISO-8859-1 encoding that IDEs traditionally apply to .properties files.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$src = 'i18n-ar-source.txt'
$dst = 'src\main\resources\messages_ar.properties'

$text = [IO.File]::ReadAllText($src, [Text.Encoding]::UTF8)

$sb = New-Object System.Text.StringBuilder
foreach ($ch in $text.ToCharArray()) {
    $code = [int]$ch
    if ($code -gt 126) {
        [void]$sb.Append(('\u{0:x4}' -f $code))
    } else {
        [void]$sb.Append($ch)
    }
}

[IO.File]::WriteAllText($dst, $sb.ToString(), (New-Object Text.UTF8Encoding($false)))

# Report
$written = [IO.File]::ReadAllText($dst, [Text.Encoding]::UTF8)
$escapes = ([regex]::Matches($written, '\\u[0-9a-f]{4}')).Count
$nonAscii = 0
foreach ($ch in $written.ToCharArray()) { if ([int]$ch -gt 126) { $nonAscii++ } }
"escapes=$escapes nonAsciiLeft=$nonAscii bytes=$((Get-Item $dst).Length)" | Out-File convert-report.txt -Encoding utf8
