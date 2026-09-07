package com.mounir.learn.drbooking.web;

import com.mounir.learn.drbooking.config.AppProperties;
import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.BookingStatus;
import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.security.ProviderPrincipal;
import com.mounir.learn.drbooking.service.BookingService;
import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.web.dto.ProfileForm;
import com.mounir.learn.drbooking.web.dto.WorkingHoursForm;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ProviderService providerService;
    private final BookingService bookingService;
    private final AppProperties properties;

    public DashboardController(ProviderService providerService,
                               BookingService bookingService,
                               AppProperties properties) {
        this.providerService = providerService;
        this.bookingService = bookingService;
        this.properties = properties;
    }

    @ModelAttribute("provider")
    public Provider provider(@AuthenticationPrincipal ProviderPrincipal principal) {
        return providerService.requireByEmail(principal.getUsername());
    }

    @ModelAttribute("bookingLink")
    public String bookingLink(@AuthenticationPrincipal ProviderPrincipal principal) {
        return properties.getBaseUrl() + "/" + principal.getSlug();
    }

    @GetMapping
    public String overview(@ModelAttribute("provider") Provider provider, Model model) {
        List<Booking> upcoming = bookingService.upcoming(provider);
        ZoneId zone = provider.zone();
        Instant weekStart = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant weekEnd = LocalDate.now(zone).plusDays(7).atStartOfDay(zone).toInstant();

        Map<BookingStatus, Long> byStatus = bookingService.between(provider, weekStart, weekEnd).stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

        model.addAttribute("upcoming", upcoming);
        model.addAttribute("zone", zone);
        model.addAttribute("statusCounts", byStatus);
        model.addAttribute("last30", bookingService.countLast30Days(provider));
        model.addAttribute("plan", provider.getPlan());
        return "dashboard/index";
    }

    @PostMapping("/bookings/{id}/status")
    public String changeStatus(@ModelAttribute("provider") Provider provider,
                               @PathVariable Long id,
                               @RequestParam BookingStatus status) {
        bookingService.updateStatus(id, provider, status);
        return "redirect:/dashboard";
    }

    @GetMapping("/availability")
    public String availability(@ModelAttribute("provider") Provider provider, Model model) {
        if (!model.containsAttribute("hoursForm")) {
            model.addAttribute("hoursForm", providerService.workingHoursForm(provider));
        }
        model.addAttribute("timeOffs", providerService.timeOffs(provider));
        return "dashboard/availability";
    }

    @PostMapping("/availability")
    public String saveAvailability(@ModelAttribute("provider") Provider provider,
                                   @ModelAttribute("hoursForm") WorkingHoursForm form) {
        providerService.saveWorkingHours(provider, form);
        return "redirect:/dashboard/availability?saved";
    }

    @PostMapping("/time-off")
    public String addTimeOff(@ModelAttribute("provider") Provider provider,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                             @RequestParam(required = false) String reason) {
        ZoneId zone = provider.zone();
        providerService.addTimeOff(provider, start.atZone(zone).toInstant(), end.atZone(zone).toInstant(), reason);
        return "redirect:/dashboard/availability?blocked";
    }

    @PostMapping("/time-off/{id}/delete")
    public String deleteTimeOff(@ModelAttribute("provider") Provider provider, @PathVariable Long id) {
        providerService.deleteTimeOff(provider, id);
        return "redirect:/dashboard/availability";
    }

    @GetMapping("/settings")
    public String settings(@ModelAttribute("provider") Provider provider, Model model) {
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setDisplayName(provider.getDisplayName());
            form.setBusinessName(provider.getBusinessName());
            form.setWhatsappNumber(provider.getWhatsappNumber());
            form.setZoneId(provider.getZoneId());
            form.setSlotMinutes(provider.getSlotMinutes());
            form.setBufferMinutes(provider.getBufferMinutes());
            form.setBookingHorizonDays(provider.getBookingHorizonDays());
            form.setDescription(provider.getDescription());
            model.addAttribute("profileForm", form);
        }
        model.addAttribute("zones", List.of("UTC", "Africa/Cairo", "Europe/London", "Europe/Paris",
                "Europe/Berlin", "Asia/Dubai", "Asia/Riyadh", "Asia/Kolkata",
                "America/New_York", "America/Los_Angeles"));
        return "dashboard/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute("provider") Provider provider,
                               @Valid @ModelAttribute("profileForm") ProfileForm form,
                               BindingResult binding,
                               Model model) {
        if (binding.hasErrors()) {
            return settings(provider, model);
        }
        providerService.updateProfile(provider, form);
        return "redirect:/dashboard/settings?saved";
    }
}
