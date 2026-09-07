package com.mounir.learn.drbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application level settings (see application.yml, prefix "app").
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Public base URL used to build booking links. */
    private String baseUrl = "http://localhost:8080";

    /** From address used for notification e-mails. */
    private String mailFrom = "no-reply@drbooking.local";

    private final Whatsapp whatsapp = new Whatsapp();

    private final Reminders reminders = new Reminders();

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String mailFrom) { this.mailFrom = mailFrom; }

    public Whatsapp getWhatsapp() { return whatsapp; }

    public Reminders getReminders() { return reminders; }

    public static class Whatsapp {
        /** When false messages are only logged (useful for local development). */
        private boolean enabled = false;
        /** Cloud API endpoint, e.g. https://graph.facebook.com/v20.0/{phone-number-id}/messages */
        private String apiUrl = "";
        private String accessToken = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    }

    public static class Reminders {
        private boolean enabled = true;
        /** First reminder, hours before the appointment. */
        private int firstReminderHours = 24;
        /** Second reminder, hours before the appointment. */
        private int secondReminderHours = 2;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getFirstReminderHours() { return firstReminderHours; }
        public void setFirstReminderHours(int firstReminderHours) { this.firstReminderHours = firstReminderHours; }

        public int getSecondReminderHours() { return secondReminderHours; }
        public void setSecondReminderHours(int secondReminderHours) { this.secondReminderHours = secondReminderHours; }
    }
}
