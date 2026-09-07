Start-Sleep -Seconds 15
try {
    $r = Invoke-WebRequest -Uri 'http://localhost:8080/dr-ahmed' -UseBasicParsing -MaximumRedirection 0 -ErrorAction Stop
    "STATUS=$($r.StatusCode)" | Out-File live.txt -Encoding utf8
    $r.Content | Out-File live-body.txt -Encoding utf8
} catch {
    $resp = $_.Exception.Response
    if ($resp) {
        "STATUS=$([int]$resp.StatusCode)" | Out-File live.txt -Encoding utf8
        $reader = New-Object IO.StreamReader($resp.GetResponseStream())
        $reader.ReadToEnd() | Out-File live-body.txt -Encoding utf8
    } else {
        $_.Exception.Message | Out-File live.txt -Encoding utf8
    }
}
