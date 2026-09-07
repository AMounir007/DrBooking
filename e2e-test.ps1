﻿$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$report = New-Object System.Collections.Generic.List[string]
$slug = 'e2e-' + (Get-Random -Maximum 999999)
$email = "$slug@example.com"
$password = 'password123'

function Step([string]$name, [scriptblock]$block) {
    try {
        & $block
        $report.Add("PASS: $name")
    } catch {
        $report.Add("FAIL: $name -- $($_.Exception.Message)")
    }
}

function Csrf([string]$html) {
    return [regex]::Match($html, 'name="_csrf" value="([^"]+)"').Groups[1].Value
}

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 1. Home page loads in Arabic by default
Step "Home page (ar default)" {
    $r = Invoke-WebRequest -Uri "$base/" -WebSession $session -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'dir="rtl"') { throw "not RTL by default" }
}

# 2. Registration page loads, grab CSRF
$regPage = Invoke-WebRequest -Uri "$base/register" -WebSession $session -UseBasicParsing
Step "Registration page loads" {
    if ($regPage.StatusCode -ne 200) { throw "status $($regPage.StatusCode)" }
}
$csrf = Csrf $regPage.Content
if (-not $csrf) { $report.Add("FAIL: could not extract CSRF token from /register") }

# 3. Register a brand-new provider (follow redirects; final page should be /login?registered)
Step "Register new provider ($slug)" {
    $body = @{
        displayName  = 'E2E Test Provider'
        businessName = 'E2E Clinic'
        slug         = $slug
        zoneId       = 'Africa/Cairo'
        language     = 'ar'
        email        = $email
        password     = $password
        _csrf        = $csrf
    }
    $r = Invoke-WebRequest -Uri "$base/register" -Method Post -Body $body -WebSession $session -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'registered' -and $r.BaseResponse.ResponseUri.AbsolutePath -ne '/login') {
        throw "registration did not land on /login?registered (landed on $($r.BaseResponse.ResponseUri))"
    }
}

# 4. Login page + login (follow redirect to /dashboard)
$loginPage = Invoke-WebRequest -Uri "$base/login" -WebSession $session -UseBasicParsing
$csrfLogin = Csrf $loginPage.Content

$script:loginResult = $null
Step "Login as new provider" {
    $body = @{ username = $email; password = $password; _csrf = $csrfLogin }
    $script:loginResult = Invoke-WebRequest -Uri "$base/login" -Method Post -Body $body -WebSession $session -UseBasicParsing
    if ($script:loginResult.StatusCode -ne 200) { throw "status $($script:loginResult.StatusCode)" }
    if ($script:loginResult.BaseResponse.ResponseUri.AbsolutePath -ne '/dashboard') {
        throw "login did not land on /dashboard (landed on $($script:loginResult.BaseResponse.ResponseUri))"
    }
}

# 5. Dashboard shows the booking link
Step "Dashboard shows booking link after login" {
    $content = if ($script:loginResult) { $script:loginResult.Content } else {
        (Invoke-WebRequest -Uri "$base/dashboard" -WebSession $session -UseBasicParsing).Content
    }
    if ($content -notmatch [regex]::Escape("/$slug")) { throw "booking link not shown on dashboard" }
}

# 6. Public booking page for the new provider (same code path as "See a live example")
Step "Public booking page for new provider" {
    $r = Invoke-WebRequest -Uri "$base/$slug" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'E2E Test Provider') { throw "provider name missing" }
}

# 7. "See a live example" -> the seeded demo provider
Step "See a live example (/dr-ahmed)" {
    $r = Invoke-WebRequest -Uri "$base/dr-ahmed" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'dir="rtl"') { throw "not RTL" }
}

# 8. JSON slots API for the new provider, tomorrow's date
$tomorrow = (Get-Date).AddDays(1).ToString('yyyy-MM-dd')
$script:slotsJson = $null
Step "Slots JSON API returns slots" {
    $r = Invoke-WebRequest -Uri "$base/api/public/$slug/slots?date=$tomorrow" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    $script:slotsJson = $r.Content | ConvertFrom-Json
    if ($script:slotsJson.slots.Count -lt 1) { throw "no slots returned for $tomorrow" }
}

# The anonymous customer flow needs one shared session so the CSRF cookie
# handed out on GET is sent back with the following POST.
$customerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 9. Submit a real booking through the public form (follow redirect to /booking/{ref}?created)
$script:bookingRef = $null
Step "Submit a booking as a customer" {
    $bookPage = Invoke-WebRequest -Uri "$base/$slug`?date=$tomorrow" -WebSession $customerSession -UseBasicParsing
    $csrfBook = Csrf $bookPage.Content
    $startValue = $script:slotsJson.slots[0].start

    $body = @{
        start         = $startValue
        customerName  = 'Test Customer'
        customerPhone = '201000000001'
        customerEmail = 'customer@example.com'
        notes         = 'E2E automated booking'
        _csrf         = $csrfBook
    }
    $r = Invoke-WebRequest -Uri "$base/$slug/book?date=$tomorrow" -Method Post -Body $body -WebSession $customerSession -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    $finalPath = $r.BaseResponse.ResponseUri.AbsolutePath
    if ($finalPath -notmatch '^/booking/([a-z0-9]+)$') { throw "did not land on /booking/{ref}, landed on $finalPath" }
    $script:bookingRef = [regex]::Match($finalPath, '^/booking/([a-z0-9]+)$').Groups[1].Value
    if ($r.Content -notmatch 'Test Customer') { throw "customer name missing from confirmation page" }
}

# 10. View the booking confirmation page directly (idempotent re-fetch)
Step "View booking confirmation page" {
    $r = Invoke-WebRequest -Uri "$base/booking/$($script:bookingRef)" -WebSession $customerSession -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'Test Customer') { throw "customer name missing" }
}

# 11. Verify the slot disappeared from availability (no double booking)
Step "Booked slot no longer available" {
    $r = Invoke-WebRequest -Uri "$base/api/public/$slug/slots?date=$tomorrow" -UseBasicParsing
    $slots = ($r.Content | ConvertFrom-Json).slots
    $stillThere = $slots | Where-Object { $_.start -eq $script:slotsJson.slots[0].start }
    if ($stillThere) { throw "slot still shows as available after booking" }
}

# 12. Attempt to double-book the same slot directly -> must be rejected
Step "Double-booking the same slot is rejected" {
    $bookPage2 = Invoke-WebRequest -Uri "$base/$slug`?date=$tomorrow" -WebSession $customerSession -UseBasicParsing
    $csrfBook2 = Csrf $bookPage2.Content
    $body = @{
        start         = $script:slotsJson.slots[0].start
        customerName  = 'Another Customer'
        customerPhone = '201000000002'
        _csrf         = $csrfBook2
    }
    $r = Invoke-WebRequest -Uri "$base/$slug/book?date=$tomorrow" -Method Post -Body $body -WebSession $customerSession -UseBasicParsing
    if ($r.Content -notmatch 'no longer available|taken|already') {
        throw "expected a conflict/unavailable message, got a different page"
    }
}

# 13. Cancel the original booking
Step "Cancel the booking" {
    $managePage = Invoke-WebRequest -Uri "$base/booking/$($script:bookingRef)" -WebSession $customerSession -UseBasicParsing
    $csrfCancel = Csrf $managePage.Content
    $body = @{ _csrf = $csrfCancel }
    $r = Invoke-WebRequest -Uri "$base/booking/$($script:bookingRef)/cancel" -Method Post -Body $body -WebSession $customerSession -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
    if ($r.Content -notmatch 'cancelled|إلغاء') { throw "cancellation confirmation not shown" }
}

# 14. Slot is available again after cancellation
Step "Slot available again after cancellation" {
    $r = Invoke-WebRequest -Uri "$base/api/public/$slug/slots?date=$tomorrow" -UseBasicParsing
    $slots = ($r.Content | ConvertFrom-Json).slots
    $backAgain = $slots | Where-Object { $_.start -eq $script:slotsJson.slots[0].start }
    if (-not $backAgain) { throw "slot did not reopen after cancellation" }
}

# 15. Language switch works and persists via cookie
Step "Language switch to English persists via cookie" {
    $sw = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $r = Invoke-WebRequest -Uri "$base/$slug`?lang=en" -WebSession $sw -UseBasicParsing
    if ($r.Content -notmatch 'dir="ltr"') { throw "did not switch to LTR" }
    $r2 = Invoke-WebRequest -Uri "$base/$slug" -WebSession $sw -UseBasicParsing
    if ($r2.Content -notmatch 'dir="ltr"') { throw "language choice was not persisted via cookie" }
}

# 16. Unauthenticated dashboard access redirects to login
Step "Dashboard requires authentication for anonymous visitors" {
    $anon = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $r = Invoke-WebRequest -Uri "$base/dashboard" -WebSession $anon -UseBasicParsing
    if ($r.BaseResponse.ResponseUri.AbsolutePath -ne '/login') {
        throw "anonymous /dashboard did not redirect to /login (landed on $($r.BaseResponse.ResponseUri))"
    }
}

$report | Out-File e2e-report.txt -Encoding utf8
$failures = $report | Where-Object { $_ -like 'FAIL*' }
if ($failures) { "OVERALL=FAIL" | Add-Content e2e-report.txt } else { "OVERALL=PASS" | Add-Content e2e-report.txt }
