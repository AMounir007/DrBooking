package com.mounir.learn.drbooking;

import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.service.*;
import com.mounir.learn.drbooking.web.dto.BookingRequest;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingFlowTest {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingService bookingService;

    private Provider provider;

    @BeforeEach
    void setUp() {
        String slug = "clinic-" + UUID.randomUUID().toString().substring(0, 6);
        RegistrationRequest request = new RegistrationRequest();
        request.setDisplayName("Dr. Test");
        request.setEmail(slug + "@example.com");
        request.setPassword("password123");
        request.setSlug(slug);
        request.setZoneId("UTC");
        provider = providerService.register(request);
    }

    private LocalDate nextWorkingDay() {
        return LocalDate.now(provider.zone()).plusDays(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    @Test
    void defaultWorkingHoursProduceSlots() {
        List<Slot> slots = availabilityService.freeSlots(provider, nextWorkingDay());
        assertThat(slots).isNotEmpty();
        assertThat(slots.get(0).end()).isEqualTo(slots.get(0).start().plusSeconds(30 * 60));
    }

    @Test
    void bookingRemovesTheSlotAndPreventsDoubleBooking() {
        LocalDate day = nextWorkingDay();
        Slot slot = availabilityService.freeSlots(provider, day).get(0);

        BookingRequest request = new BookingRequest();
        request.setCustomerName("Mona");
        request.setCustomerPhone("201234567890");
        request.setCustomerEmail("mona@example.com");
        request.setStart(slot.start());

        Booking booking = bookingService.create(provider, request);
        assertThat(booking.getReference()).isNotBlank();

        assertThat(availabilityService.freeSlots(provider, day))
                .noneMatch(s -> s.start().equals(slot.start()));

        BookingRequest duplicate = new BookingRequest();
        duplicate.setCustomerName("Ali");
        duplicate.setCustomerPhone("201234567891");
        duplicate.setStart(slot.start());

        assertThatThrownBy(() -> bookingService.create(provider, duplicate))
                .isInstanceOf(BookingException.class);
    }

    @Test
    void cancellingFreesTheSlotAgain() {
        LocalDate day = nextWorkingDay();
        Slot slot = availabilityService.freeSlots(provider, day).get(0);

        BookingRequest request = new BookingRequest();
        request.setCustomerName("Sara");
        request.setCustomerPhone("201234567892");
        request.setStart(slot.start());
        Booking booking = bookingService.create(provider, request);

        bookingService.cancel(booking.getReference());

        assertThat(availabilityService.freeSlots(provider, day))
                .anyMatch(s -> s.start().equals(slot.start()));
    }

    @Test
    void slugMustBeUnique() {
        RegistrationRequest duplicate = new RegistrationRequest();
        duplicate.setDisplayName("Copycat");
        duplicate.setEmail("other@example.com");
        duplicate.setPassword("password123");
        duplicate.setSlug(provider.getSlug());
        duplicate.setZoneId("UTC");

        assertThatThrownBy(() -> providerService.register(duplicate))
                .isInstanceOf(BookingException.class);
    }
}
