package com.mounir.learn.drbooking.service;

import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.domain.TimeOff;
import com.mounir.learn.drbooking.domain.WorkingHour;
import com.mounir.learn.drbooking.repository.ProviderRepository;
import com.mounir.learn.drbooking.repository.TimeOffRepository;
import com.mounir.learn.drbooking.repository.WorkingHourRepository;
import com.mounir.learn.drbooking.web.dto.ProfileForm;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import com.mounir.learn.drbooking.web.dto.WorkingHoursForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final WorkingHourRepository workingHourRepository;
    private final TimeOffRepository timeOffRepository;
    private final PasswordEncoder passwordEncoder;

    public ProviderService(ProviderRepository providerRepository,
                           WorkingHourRepository workingHourRepository,
                           TimeOffRepository timeOffRepository,
                           PasswordEncoder passwordEncoder) {
        this.providerRepository = providerRepository;
        this.workingHourRepository = workingHourRepository;
        this.timeOffRepository = timeOffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Provider register(RegistrationRequest request) {
        if (providerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BookingException("An account already exists with this e-mail.");
        }
        if (providerRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new BookingException("This booking link is already taken.");
        }
        Provider provider = new Provider();
        provider.setDisplayName(request.getDisplayName());
        provider.setBusinessName(request.getBusinessName());
        provider.setEmail(request.getEmail().toLowerCase());
        provider.setSlug(request.getSlug().toLowerCase());
        provider.setZoneId(request.getZoneId());
        provider.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Provider saved = providerRepository.save(provider);

        // Sensible default: Monday to Friday, 09:00 - 17:00.
        List<WorkingHour> defaults = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY).stream()
                .map(day -> new WorkingHour(saved, day, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(17, 0)))
                .toList();
        workingHourRepository.saveAll(defaults);
        return saved;
    }

    @Transactional(readOnly = true)
    public Provider requireBySlug(String slug) {
        return providerRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new BookingException("Booking page not found."));
    }

    @Transactional(readOnly = true)
    public Provider requireByEmail(String email) {
        return providerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BookingException("Account not found."));
    }

    @Transactional
    public Provider updateProfile(Provider provider, ProfileForm form) {
        provider.setDisplayName(form.getDisplayName());
        provider.setBusinessName(form.getBusinessName());
        provider.setWhatsappNumber(form.getWhatsappNumber());
        provider.setZoneId(form.getZoneId());
        provider.setSlotMinutes(form.getSlotMinutes());
        provider.setBufferMinutes(form.getBufferMinutes());
        provider.setBookingHorizonDays(form.getBookingHorizonDays());
        provider.setDescription(form.getDescription());
        return providerRepository.save(provider);
    }

    @Transactional(readOnly = true)
    public WorkingHoursForm workingHoursForm(Provider provider) {
        Map<DayOfWeek, WorkingHour> existing = workingHourRepository
                .findByProviderOrderByDayOfWeekAscStartTimeAsc(provider).stream()
                .collect(Collectors.toMap(WorkingHour::getDayOfWeek, Function.identity(), (a, b) -> a));

        WorkingHoursForm form = WorkingHoursForm.empty();
        form.getRows().forEach(row -> {
            WorkingHour hour = existing.get(row.getDayOfWeek());
            if (hour != null) {
                row.setEnabled(true);
                row.setStartTime(hour.getStartTime());
                row.setEndTime(hour.getEndTime());
            }
        });
        return form;
    }

    @Transactional
    public void saveWorkingHours(Provider provider, WorkingHoursForm form) {
        workingHourRepository.deleteByProvider(provider);
        workingHourRepository.flush();
        List<WorkingHour> hours = form.getRows().stream()
                .filter(WorkingHoursForm.Row::isEnabled)
                .filter(row -> row.getStartTime() != null && row.getEndTime() != null)
                .filter(row -> row.getStartTime().isBefore(row.getEndTime()))
                .map(row -> new WorkingHour(provider, row.getDayOfWeek(), row.getStartTime(), row.getEndTime()))
                .toList();
        workingHourRepository.saveAll(hours);
    }

    @Transactional(readOnly = true)
    public List<TimeOff> timeOffs(Provider provider) {
        return timeOffRepository.findByProviderOrderByStartAtAsc(provider);
    }

    @Transactional
    public TimeOff addTimeOff(Provider provider, Instant start, Instant end, String reason) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BookingException("The end of the block must be after its start.");
        }
        return timeOffRepository.save(new TimeOff(provider, start, end, reason));
    }

    @Transactional
    public void deleteTimeOff(Provider provider, Long id) {
        timeOffRepository.findById(id)
                .filter(t -> t.getProvider().getId().equals(provider.getId()))
                .ifPresent(timeOffRepository::delete);
    }

    @Transactional
    public Provider save(Provider provider) {
        return providerRepository.save(provider);
    }
}
