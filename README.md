# DrBooking — Booking & Scheduling Platform

A SaaS booking platform that gives every service provider (doctor, coach, salon, consultant…)
a personal booking page, automatic confirmations/reminders, and conflict-free scheduling.

**Arabic-first**: the UI defaults to Arabic (RTL) with Egypt (`Africa/Cairo`) as the default
time zone, and visitors can switch to English at any time.

## Features (MVP)

| Problem | Solution in the app |
| --- | --- |
| Hours lost answering booking messages | Personal booking page at `/{slug}` (e.g. `/dr-ahmed`) showing only real free slots |
| No-shows | Automatic confirmation + reminders 24h and 2h before (e-mail / WhatsApp) with a one-click cancel link |
| Double bookings & human error | Slot grid computed from working hours, buffers, time-off blocks and existing bookings; server-side overlap check on save |
| Personal events colliding with work | Blocked-time list (the landing zone for Google Calendar / Outlook imported events) |
| Arabic-speaking market | Full Arabic UI with RTL layout, Arabic weekday names and Arabic notification messages |
| Monetisation | `FREE` (20 bookings/month), `BASIC` (unlimited + e-mail reminders), `PRO` (WhatsApp + payments) |

## Languages & time zones

- **Default language:** Arabic (`ar-EG`), rendered right-to-left.
- **Switching:** the header has a language toggle; it appends `?lang=ar` / `?lang=en` and
  stores the choice in a one-year `lang` cookie (`CookieLocaleResolver` in `WebConfig`).
- **Default time zone:** `Africa/Cairo`. The time zone picker lists Egypt first, followed by
  the rest of the Arab world, then international zones (`ZoneCatalog`).
- **Notification language:** each provider chooses `ar` or `en` in *Settings*; confirmations
  and reminders are rendered in that language, independently of the visitor's UI language.

### Editing the Arabic translations

`.properties` files are read as ISO-8859-1 by many IDEs, which silently destroys Arabic text.
To stay safe, `messages_ar.properties` stores Arabic as `\uXXXX` escapes and is **generated**:

1. Edit the human-readable source: `i18n-ar-source.txt` (UTF-8).
2. Regenerate the bundle:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\convert-ar.ps1
   ```

`LocalizationTest` guards this: it fails if the Arabic bundle loses a key, stops being Arabic,
or drops a `{0}` placeholder.


## Tech stack

- Java 21, Spring Boot 3.3
- Spring MVC + Thymeleaf (server-rendered UI)
- Spring Security (provider accounts, BCrypt)
- Spring Data JPA + H2 (file-based, swap the datasource for PostgreSQL in production)
- Spring Mail + WhatsApp Cloud API client
- Spring Scheduling for the reminder sweep

## Run it

```powershell
mvn spring-boot:run
```

Then open <http://localhost:8080>.

Demo account seeded on first start (`app.demo-data: true`):

- Booking page: <http://localhost:8080/dr-ahmed>
- Dashboard login: `dr.ahmed@example.com` / `password123`

Run the tests:

```powershell
mvn test
```

## URL map

| URL | Description |
| --- | --- |
| `/` | Marketing landing page |
| `/pricing` | Plans |
| `/register`, `/login` | Provider sign-up / sign-in |
| `/{slug}` | Public booking page |
| `/{slug}/book` | Booking submission |
| `/booking/{reference}` | Customer self-service (view / cancel) |
| `/api/public/{slug}/slots?date=YYYY-MM-DD` | JSON free slots (integrations) |
| `/dashboard` | Provider overview + upcoming appointments |
| `/dashboard/availability` | Weekly working hours + blocked time |
| `/dashboard/settings` | Profile, timezone, slot length, buffer, horizon |
| `/h2-console` | Database console (dev only) |

## Configuration

All settings live in `src/main/resources/application.yml` and can be overridden with env vars:

| Env var | Purpose |
| --- | --- |
| `APP_BASE_URL` | Public base URL used in booking links |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP server |
| `WHATSAPP_ENABLED` | `true` to actually call the WhatsApp Cloud API |
| `WHATSAPP_API_URL` | `https://graph.facebook.com/v20.0/{phone-number-id}/messages` |
| `WHATSAPP_TOKEN` | Cloud API access token |

When WhatsApp is disabled or SMTP is unreachable, messages are logged instead of sent, so the
whole flow can be demonstrated locally without credentials.

## Architecture

```
domain/         JPA entities: Provider, WorkingHour, TimeOff, Booking, Plan, BookingStatus
repository/     Spring Data repositories
service/        AvailabilityService (slot engine), BookingService, ProviderService, ReminderScheduler
notification/   NotificationService (localized) + EmailChannel / WhatsAppChannel
security/       Provider authentication (UserDetailsService, principal)
web/            Controllers (home, public booking, dashboard) and form DTOs
config/         WebConfig (locale resolver), ZoneCatalog (Egypt-first zones), AppProperties
resources/      messages.properties (en) + messages_ar.properties (ar, \uXXXX escaped)
```

The slot engine (`AvailabilityService`) is the core: it expands the weekly working hours into a
grid of `slotMinutes` blocks separated by `bufferMinutes`, then subtracts past times, active
bookings (plus buffer) and time-off periods.

## Next steps beyond the MVP

- Stripe/Paymob deposits (`Booking.depositAmount` / `paid` are already modelled)
- Google Calendar & Outlook OAuth sync writing into `TimeOff.externalEventId`
- Multi-staff and multi-branch support
- Subscription billing and the 1–2% transaction fee
