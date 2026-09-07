package com.mounir.learn.drbooking.notification;

import com.mounir.learn.drbooking.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * WhatsApp Cloud API channel. When disabled the message is logged instead of sent,
 * so the whole flow can be demonstrated without credentials.
 */
@Component
public class WhatsAppChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppChannel.class);

    private final AppProperties properties;
    private final RestClient restClient;

    public WhatsAppChannel(AppProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public String name() {
        return "WHATSAPP";
    }

    @Override
    public boolean supports(String destination) {
        return destination != null && destination.replaceAll("[^0-9]", "").length() >= 8;
    }

    @Override
    public void send(String destination, String subject, String body) {
        String phone = destination.replaceAll("[^0-9]", "");
        String text = subject + "\n\n" + body;

        AppProperties.Whatsapp cfg = properties.getWhatsapp();
        if (!cfg.isEnabled() || cfg.getApiUrl().isBlank()) {
            log.info("[WhatsApp simulated] to +{} :\n{}", phone, text);
            return;
        }
        try {
            restClient.post()
                    .uri(cfg.getApiUrl())
                    .header("Authorization", "Bearer " + cfg.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "to", phone,
                            "type", "text",
                            "text", Map.of("preview_url", false, "body", text)))
                    .retrieve()
                    .toBodilessEntity();
            log.info("WhatsApp message sent to +{}", phone);
        } catch (Exception ex) {
            log.warn("WhatsApp message to +{} failed: {}", phone, ex.getMessage());
        }
    }
}
