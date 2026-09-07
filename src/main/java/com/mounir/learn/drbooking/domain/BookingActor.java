package com.mounir.learn.drbooking.domain;

/**
 * Who performed a status change on a booking. Kept as an audit trail so a provider can
 * always answer "who confirmed this?", "who cancelled this?" and "who marked this a no-show?".
 */
public enum BookingActor {
    /** The service provider (or a staff member acting on their behalf) via the dashboard. */
    PROVIDER,
    /** The customer, e.g. self-service cancellation from their manage-booking page. */
    CUSTOMER,
    /** An automated process, e.g. the booking was created straight from the public page. */
    SYSTEM
}
