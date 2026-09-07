package com.mounir.learn.drbooking;

import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LocalizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messages;

    @Autowired
    private ProviderService providerService;

    @Test
    void homePageIsArabicByDefault() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dir=\"rtl\"")))
                .andExpect(content().string(containsString("توقف عن مطاردة المواعيد.")));
    }

    @Test
    void languageCanBeSwitchedToEnglish() throws Exception {
        mockMvc.perform(get("/").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dir=\"ltr\"")))
                .andExpect(content().string(containsString("Stop chasing appointments.")));
    }

    @Test
    void bothBundlesResolveTheSameKeys() {
        Locale ar = Locale.forLanguageTag("ar-EG");
        assertThat(messages.getMessage("nav.pricing", null, ar)).isEqualTo("الأسعار");
        assertThat(messages.getMessage("nav.pricing", null, Locale.ENGLISH)).isEqualTo("Pricing");
        assertThat(messages.getMessage("day.FRIDAY", null, ar)).isEqualTo("الجمعة");
        assertThat(messages.getMessage("status.CONFIRMED", null, ar)).isEqualTo("مؤكد");
    }

    @Test
    void notificationTemplatesAreTranslated() {
        Locale ar = Locale.forLanguageTag("ar-EG");
        String subject = messages.getMessage("notify.confirm.subject", new Object[]{"د. أحمد"}, ar);
        assertThat(subject).contains("د. أحمد").contains("تم تأكيد");
    }

    @Test
    void newProviderDefaultsToEgyptAndArabic() {
        RegistrationRequest request = new RegistrationRequest();
        request.setDisplayName("د. سارة");
        request.setEmail("sara@example.com");
        request.setPassword("password123");
        request.setSlug("dr-sara");

        var provider = providerService.register(request);

        assertThat(provider.getZoneId()).isEqualTo("Africa/Cairo");
        assertThat(provider.getLanguage()).isEqualTo("ar");
        assertThat(provider.zone().getId()).isEqualTo("Africa/Cairo");
        assertThat(provider.locale().getLanguage()).isEqualTo("ar");
    }
}
