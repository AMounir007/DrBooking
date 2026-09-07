package com.mounir.learn.drbooking.domain;

/**
 * Subscription plans used for monetisation.
 */
public enum Plan {

    FREE("Free", 20, false, false),
    BASIC("Basic", Integer.MAX_VALUE, true, false),
    PRO("Pro", Integer.MAX_VALUE, true, true);

    private final String label;
    private final int monthlyBookingLimit;
    private final boolean emailReminders;
    private final boolean whatsappReminders;

    Plan(String label, int monthlyBookingLimit, boolean emailReminders, boolean whatsappReminders) {
        this.label = label;
        this.monthlyBookingLimit = monthlyBookingLimit;
        this.emailReminders = emailReminders;
        this.whatsappReminders = whatsappReminders;
    }

    public String getLabel() { return label; }

    public int getMonthlyBookingLimit() { return monthlyBookingLimit; }

    public boolean isEmailReminders() { return emailReminders; }

    public boolean isWhatsappReminders() { return whatsappReminders; }
}
