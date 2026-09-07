package com.mounir.learn.drbooking;

import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.service.AvailabilityService;
import com.mounir.learn.drbooking.service.BookingService;
import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.service.Slot;
import com.mounir.learn.drbooking.web.dto.BookingRequest;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebPagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingService bookingService;

    @Test
    void publicPagesAreReachable() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/pricing")).andExpect(status().isOk());
    }

    @Test
    void dashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    void providerBookingPageIsPublic() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setDisplayName("Dr. Public");
        request.setEmail("public@example.com");
        request.setPassword("password123");
        request.setSlug("dr-public");
        request.setZoneId("UTC");
        providerService.register(request);

        mockMvc.perform(get("/dr-public"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dr. Public")));
    }

    /**
     * Regression test for the "See a live example" 500 error: viewing and cancelling a
     * booking through the actual servlet stack must not throw LazyInitializationException
     * when Booking.provider is accessed outside the transaction that loaded the booking.
     */
    @Test
    void bookingManagePageRendersAndCancelsWithoutLazyInitializationException() throws Exception {
        String slug = "e2e-web-" + UUID.randomUUID().toString().substring(0, 6);
        RegistrationRequest request = new RegistrationRequest();
        request.setDisplayName("Dr. Lazy");
        request.setEmail(slug + "@example.com");
        request.setPassword("password123");
        request.setSlug(slug);
        request.setZoneId("Africa/Cairo");
        var provider = providerService.register(request);

        LocalDate day = LocalDate.now(provider.zone()).plusDays(1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        List<Slot> slots = availabilityService.freeSlots(provider, day);
        org.assertj.core.api.Assertions.assertThat(slots).isNotEmpty();

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCustomerName("Web Customer");
        bookingRequest.setCustomerPhone("201000000099");
        bookingRequest.setStart(slots.get(0).start());
        Booking booking = bookingService.create(provider, bookingRequest);

        // This GET is served by a fresh HTTP request/transaction, exactly like the
        // failure reported for "See a live example" -> book -> view confirmation.
        mockMvc.perform(get("/booking/" + booking.getReference()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Web Customer")));

        mockMvc.perform(post("/booking/" + booking.getReference() + "/cancel").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/booking/" + booking.getReference()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Web Customer")));
    }
}

