package com.mounir.learn.drbooking.service;

import com.mounir.learn.drbooking.domain.*;
import com.mounir.learn.drbooking.repository.BookingRepository;
import com.mounir.learn.drbooking.repository.TimeOffRepository;
import com.mounir.learn.drbooking.repository.WorkingHourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Computes free slots from working hours minus bookings and time-off blocks.
 */
@Service
public class AvailabilityService {

    private static final EnumSet<BookingStatus> BLOCKING_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final WorkingHourRepository workingHourRepository;
    private final TimeOffRepository timeOffRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public AvailabilityService(WorkingHourRepository workingHourRepository,
                               TimeOffRepository timeOffRepository,
                               BookingRepository bookingRepository,
                               Clock clock) {
        this.workingHourRepository = workingHourRepository;
        this.timeOffRepository = timeOffRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    /**
     * @return free slots for the given local date of the provider.
     */
    @Transactional(readOnly = true)
    public List<Slot> freeSlots(Provider provider, LocalDate date) {
        ZoneId zone = provider.zone();
        List<Slot> candidates = candidateSlots(provider, date, zone);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        List<Booking> bookings = bookingRepository
                .findByProviderAndStatusInAndStartAtBetweenOrderByStartAtAsc(provider, BLOCKING_STATUSES,
                        dayStart.minus(Duration.ofDays(1)), dayEnd);
        List<TimeOff> timeOffs = timeOffRepository
                .findByProviderAndEndAtAfterAndStartAtBefore(provider, dayStart, dayEnd);

        Instant now = Instant.now(clock);
        List<Slot> free = new ArrayList<>();
        for (Slot slot : candidates) {
            if (!slot.start().isAfter(now)) {
                continue; // never offer a slot in the past
            }
            if (bookings.stream().anyMatch(b -> slot.overlaps(b.getStartAt(),
                    b.getEndAt().plusSeconds(60L * provider.getBufferMinutes())))) {
                continue;
            }
            if (timeOffs.stream().anyMatch(t -> slot.overlaps(t.getStartAt(), t.getEndAt()))) {
                continue;
            }
            free.add(slot);
        }
        return free;
    }

    /**
     * Raw slot grid derived from working hours only.
     */
    @Transactional(readOnly = true)
    public List<Slot> candidateSlots(Provider provider, LocalDate date, ZoneId zone) {
        List<WorkingHour> hours = workingHourRepository.findByProviderAndDayOfWeek(provider, date.getDayOfWeek());
        if (hours.isEmpty()) {
            return List.of();
        }
        Duration slotLength = Duration.ofMinutes(provider.getSlotMinutes());
        Duration step = slotLength.plusMinutes(provider.getBufferMinutes());

        List<Slot> slots = new ArrayList<>();
        for (WorkingHour hour : hours) {
            ZonedDateTime cursor = date.atTime(hour.getStartTime()).atZone(zone);
            ZonedDateTime windowEnd = date.atTime(hour.getEndTime()).atZone(zone);
            while (!cursor.plus(slotLength).isAfter(windowEnd)) {
                slots.add(new Slot(cursor.toInstant(), cursor.plus(slotLength).toInstant()));
                cursor = cursor.plus(step);
            }
        }
        slots.sort((a, b) -> a.start().compareTo(b.start()));
        return slots;
    }

    /**
     * @return true when the exact slot is still bookable.
     */
    @Transactional(readOnly = true)
    public boolean isSlotAvailable(Provider provider, Instant start) {
        LocalDate date = LocalDate.ofInstant(start, provider.zone());
        return freeSlots(provider, date).stream().anyMatch(s -> s.start().equals(start));
    }
}
