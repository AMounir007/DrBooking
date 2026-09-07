package com.mounir.learn.drbooking.config;

import java.util.List;

/**
 * Time zones offered in the UI. Egypt first, since it is the platform's home market.
 */
public final class ZoneCatalog {

    public static final String DEFAULT_ZONE_ID = "Africa/Cairo";

    private static final List<String> ZONES = List.of(
            "Africa/Cairo",
            "Asia/Riyadh",
            "Asia/Dubai",
            "Africa/Casablanca",
            "Asia/Amman",
            "Asia/Baghdad",
            "Asia/Kuwait",
            "Asia/Qatar",
            "Africa/Khartoum",
            "Africa/Tripoli",
            "Europe/London",
            "Europe/Paris",
            "Europe/Berlin",
            "Asia/Kolkata",
            "America/New_York",
            "America/Los_Angeles",
            "UTC");

    private ZoneCatalog() {
    }

    public static List<String> zones() {
        return ZONES;
    }
}
