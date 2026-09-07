package com.mounir.learn.drbooking.service;

import java.time.Instant;

/**
 * A bookable slot expressed as an absolute instant range.
 */
public record Slot(Instant start, Instant end) {

    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }
}
