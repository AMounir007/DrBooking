package com.mounir.learn.drbooking.config;

import java.util.List;

/**
 * Time zones offered in the UI. The platform currently only serves Egypt.
 */
public final class ZoneCatalog {

    public static final String DEFAULT_ZONE_ID = "Africa/Cairo";

    private static final List<String> ZONES = List.of(
            "Africa/Cairo");

    private ZoneCatalog() {
    }

    public static List<String> zones() {
        return ZONES;
    }
}
