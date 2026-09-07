package com.mounir.learn.drbooking.config;

import com.mounir.learn.drbooking.domain.Plan;
import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.repository.ProviderRepository;
import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds a demo provider so the app is usable right after start-up.
 * Login: dr.ahmed@example.com / password123
 */
@Configuration
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = false)
public class DemoDataLoader {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    @Bean
    ApplicationRunner seedDemoProvider(ProviderRepository providerRepository, ProviderService providerService) {
        return args -> {
            if (providerRepository.existsBySlugIgnoreCase("dr-ahmed")) {
                return;
            }
            RegistrationRequest request = new RegistrationRequest();
            request.setDisplayName("Dr. Ahmed");
            request.setBusinessName("Ahmed Family Clinic");
            request.setEmail("dr.ahmed@example.com");
            request.setPassword("password123");
            request.setSlug("dr-ahmed");
            request.setZoneId("Africa/Cairo");

            Provider provider = providerService.register(request);
            provider.setPlan(Plan.PRO);
            provider.setSlotMinutes(30);
            provider.setWhatsappNumber("201234567890");
            provider.setDescription("General practitioner - consultations, follow-ups and check-ups.");
            providerService.save(provider);

            log.info("Demo provider ready -> http://localhost:8080/dr-ahmed (login dr.ahmed@example.com / password123)");
        };
    }
}
