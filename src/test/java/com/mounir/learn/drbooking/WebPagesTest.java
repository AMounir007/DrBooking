package com.mounir.learn.drbooking;

import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebPagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProviderService providerService;

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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dr. Public")));
    }
}
