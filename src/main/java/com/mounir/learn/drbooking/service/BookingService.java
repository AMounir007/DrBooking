package com.mounir.learn.drbooking.service;

import com.mounir.learn.drbooking.domain.*;
import com.mounir.learn.drbooking.notification.NotificationService;
import com.mounir.learn.drbooking.repository.BookingRepository;
import com.mounir.learn.drbooking.web.dto.BookingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final EnumSet<BookingStatus> ACTIVE =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilityService availabilityService,
                          NotificationService notificationService,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public Booking create(Provider provider, BookingRequest request) {
        Instant start = request.getStart();
        if (start == null) {
            throw new BookingException("Please choose a time slot.");
        }
        if (!start.isAfter(Instant.now(clock))) {
            throw new BookingException("This time slot is already in the past.");
        }
        enforcePlanLimit(provider);

        Instant end = start.plus(Duration.ofMinutes(provider.getSlotMinutes()));

        if (bookingRepository.existsByProviderAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                provider, ACTIVE, end, start)) {
            throw new BookingException("Sorry, this slot has just been taken. Please pick another one.");
        }
        if (!availabilityService.isSlotAvailable(provider, start)) {
            throw new BookingException("This slot is no longer available.");
        }

        Booking booking = new Booking();
        booking.setProvider(provider);
        booking.setCustomerName(request.getCustomerName().trim());
        booking.setCustomerEmail(request.getCustomerEmail() == null ? null : request.getCustomerEmail().trim());
        booking.setCustomerPhone(request.getCustomerPhone().trim());
        booking.setNotes(request.getNotes());
        booking.setStartAt(start);
        booking.setEndAt(end);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now(clock));

        Booking saved = bookingRepository.save(booking);
        notificationService.sendConfirmation(saved);
        return saved;
    }

    private void enforcePlanLimit(Provider provider) {
        int limit = provider.getPlan().getMonthlyBookingLimit();
        if (limit == Integer.MAX_VALUE) {
            return;
        }
        Instant since = Instant.now(clock).minus(30, ChronoUnit.DAYS);
        long used = bookingRepository.countByProviderAndCreatedAtAfter(provider, since);
        if (used >= limit) {
            throw new BookingException(
                    "This provider reached the monthly booking limit of the current plan. Please contact them directly.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findByReference(String reference) {
        return bookingRepository.findByReference(reference);
    }

    @Transactional(readOnly = true)
    public List<Booking> upcoming(Provider provider) {
        return bookingRepository.findByProviderAndStartAtGreaterThanEqualOrderByStartAtAsc(
                provider, Instant.now(clock).minus(Duration.ofHours(2)));
    }

    @Transactional(readOnly = true)
    public List<Booking> between(Provider provider, Instant from, Instant to) {
        return bookingRepository.findByProviderAndStartAtBetweenOrderByStartAtAsc(provider, from, to);
    }

    @Transactional
    public Booking cancel(String reference) {
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new BookingException("Booking not found."));
        if (!booking.isActive()) {
            return booking;
        }
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        notificationService.sendCancellation(saved);
        return saved;
    }

    @Transactional
    public Booking updateStatus(Long id, Provider provider, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found."));
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new BookingException("This booking belongs to another provider.");
        }
        booking.setStatus(status);
        Booking saved = bookingRepository.save(booking);
        if (status == BookingStatus.CANCELLED) {
            notificationService.sendCancellation(saved);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public long countLast30Days(Provider provider) {
        return bookingRepository.countByProviderAndCreatedAtAfter(provider, Instant.now(clock).minus(30, ChronoUnit.DAYS));
    }
}
