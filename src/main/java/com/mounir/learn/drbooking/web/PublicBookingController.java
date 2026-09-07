package com.mounir.learn.drbooking.web;

import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.service.AvailabilityService;
import com.mounir.learn.drbooking.service.BookingException;
import com.mounir.learn.drbooking.service.BookingService;
import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.service.Slot;
import com.mounir.learn.drbooking.web.dto.BookingRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The public, customer facing booking page: /{slug}
 */
@Controller
public class PublicBookingController {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final ProviderService providerService;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    public PublicBookingController(ProviderService providerService,
                                   AvailabilityService availabilityService,
                                   BookingService bookingService) {
        this.providerService = providerService;
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
    }

    @GetMapping("/{slug:[a-z0-9][a-z0-9-]{2,79}}")
    public String bookingPage(@PathVariable String slug,
                              @RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        Provider provider = providerService.requireBySlug(slug);
        ZoneId zone = provider.zone();
        LocalDate today = LocalDate.now(zone);
        LocalDate selected = date == null ? today : date;
        if (selected.isBefore(today)) {
            selected = today;
        }

        model.addAttribute("provider", provider);
        model.addAttribute("selectedDate", selected);
        model.addAttribute("days", nextDays(today, provider.getBookingHorizonDays()));
        model.addAttribute("slots", labelledSlots(provider, selected));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BookingRequest());
        }
        return "public/booking";
    }

    @PostMapping("/{slug:[a-z0-9][a-z0-9-]{2,79}}/book")
    public String book(@PathVariable String slug,
                       @Valid @ModelAttribute("form") BookingRequest form,
                       BindingResult binding,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {
        Provider provider = providerService.requireBySlug(slug);
        if (!binding.hasErrors()) {
            try {
                Booking booking = bookingService.create(provider, form);
                return "redirect:/booking/" + booking.getReference() + "?created";
            } catch (BookingException ex) {
                binding.reject("booking", ex.getMessage());
            }
        }
        LocalDate selected = date != null ? date
                : (form.getStart() != null ? LocalDate.ofInstant(form.getStart(), provider.zone())
                : LocalDate.now(provider.zone()));
        model.addAttribute("provider", provider);
        model.addAttribute("selectedDate", selected);
        model.addAttribute("days", nextDays(LocalDate.now(provider.zone()), provider.getBookingHorizonDays()));
        model.addAttribute("slots", labelledSlots(provider, selected));
        return "public/booking";
    }

    @GetMapping("/booking/{reference}")
    public String manageBooking(@PathVariable String reference, Model model) {
        Booking booking = bookingService.findByReference(reference)
                .orElseThrow(() -> new BookingException("Booking not found."));
        model.addAttribute("booking", booking);
        model.addAttribute("provider", booking.getProvider());
        model.addAttribute("localStart", booking.getStartAt().atZone(booking.getProvider().zone()));
        return "public/manage";
    }

    @PostMapping("/booking/{reference}/cancel")
    public String cancelBooking(@PathVariable String reference) {
        bookingService.cancel(reference);
        return "redirect:/booking/" + reference + "?cancelled";
    }

    /** JSON slots endpoint, handy for the front-end and for integrations. */
    @GetMapping("/api/public/{slug}/slots")
    @ResponseBody
    public Map<String, Object> slots(@PathVariable String slug,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Provider provider = providerService.requireBySlug(slug);
        List<Map<String, String>> slots = labelledSlots(provider, date).entrySet().stream()
                .map(e -> Map.of("start", e.getKey(), "label", e.getValue()))
                .toList();
        return Map.of(
                "provider", provider.getDisplayName(),
                "timezone", provider.getZoneId(),
                "date", date.toString(),
                "slotMinutes", provider.getSlotMinutes(),
                "slots", slots);
    }

    private Map<String, String> labelledSlots(Provider provider, LocalDate date) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Slot slot : availabilityService.freeSlots(provider, date)) {
            result.put(slot.start().toString(), TIME.format(slot.start().atZone(provider.zone())));
        }
        return result;
    }

    private List<LocalDate> nextDays(LocalDate from, int horizon) {
        return java.util.stream.IntStream.range(0, Math.min(horizon, 14))
                .mapToObj(from::plusDays)
                .toList();
    }

    @ModelAttribute("now")
    public Instant now() {
        return Instant.now();
    }
}
