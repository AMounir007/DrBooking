package com.mounir.learn.drbooking.service;

import com.mounir.learn.drbooking.config.AppProperties;
import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.BookingStatus;
import com.mounir.learn.drbooking.notification.NotificationService;
import com.mounir.learn.drbooking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

/**
 * Sends the 24h and 2h reminders that dramatically reduce no-shows.
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final AppProperties properties;
    private final Clock clock;

    public ReminderScheduler(BookingRepository bookingRepository,
                             NotificationService notificationService,
                             AppProperties properties,
                             Clock clock) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.properties = properties;
        this.clock = clock;
    }

    /** Runs every 5 minutes. */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @Transactional
    public void dispatchReminders() {
        if (!properties.getReminders().isEnabled()) {
            return;
        }
        Instant now = Instant.now(clock);
        int firstHours = properties.getReminders().getFirstReminderHours();
        int secondHours = properties.getReminders().getSecondReminderHours();

        List<Booking> bookings = bookingRepository.findUpcoming(
                EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                now, now.plus(Duration.ofHours(firstHours)));

        for (Booking booking : bookings) {
            Duration untilStart = Duration.between(now, booking.getStartAt());
            if (!booking.isReminder24hSent() && untilStart.toHours() <= firstHours) {
                notificationService.sendReminder(booking, firstHours);
                booking.setReminder24hSent(true);
            }
            if (!booking.isReminder2hSent() && untilStart.toHours() <= secondHours) {
                notificationService.sendReminder(booking, secondHours);
                booking.setReminder2hSent(true);
            }
        }
        if (!bookings.isEmpty()) {
            bookingRepository.saveAll(bookings);
            log.debug("Reminder sweep processed {} bookings", bookings.size());
        }
    }
}
