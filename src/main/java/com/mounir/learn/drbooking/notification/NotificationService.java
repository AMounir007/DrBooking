package com.mounir.learn.drbooking.notification;

import com.mounir.learn.drbooking.config.AppProperties;
import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builds and dispatches confirmation / reminder / cancellation messages
 * in the provider's chosen language (Arabic by default).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailChannel emailChannel;
    private final WhatsAppChannel whatsAppChannel;
    private final AppProperties properties;
    private final MessageSource messages;

    public NotificationService(EmailChannel emailChannel,
                               WhatsAppChannel whatsAppChannel,
                               AppProperties properties,
                               MessageSource messages) {
        this.emailChannel = emailChannel;
        this.whatsAppChannel = whatsAppChannel;
        this.properties = properties;
        this.messages = messages;
    }

    @Async
    public void sendConfirmation(Booking booking) {
        Provider provider = booking.getProvider();
        Locale locale = provider.locale();

        String subject = msg("notify.confirm.subject", locale, provider.getDisplayName());
        String body = msg("notify.confirm.body", locale,
                booking.getCustomerName(),
                provider.getDisplayName(),
                formatFor(booking),
                provider.getZoneId(),
                booking.getReference(),
                manageLink(booking));

        dispatchToCustomer(booking, subject, body);

        emailChannel.send(provider.getEmail(),
                msg("notify.provider.new.subject", locale, booking.getCustomerName()),
                msg("notify.provider.new.body", locale,
                        booking.getCustomerName(), formatFor(booking),
                        provider.getZoneId(), booking.getCustomerPhone()));
    }

    @Async
    public void sendReminder(Booking booking, int hoursBefore) {
        Provider provider = booking.getProvider();
        Locale locale = provider.locale();

        String subject = msg("notify.reminder.subject", locale, hoursBefore);
        String body = msg("notify.reminder.body", locale,
                booking.getCustomerName(),
                provider.getDisplayName(),
                formatFor(booking),
                provider.getZoneId(),
                manageLink(booking));

        dispatchToCustomer(booking, subject, body);
    }

    @Async
    public void sendCancellation(Booking booking) {
        Provider provider = booking.getProvider();
        Locale locale = provider.locale();

        String subject = msg("notify.cancel.subject", locale);
        String body = msg("notify.cancel.body", locale,
                booking.getCustomerName(),
                provider.getDisplayName(),
                formatFor(booking),
                properties.getBaseUrl() + "/" + provider.getSlug());

        dispatchToCustomer(booking, subject, body);
        emailChannel.send(provider.getEmail(),
                msg("notify.provider.cancel.subject", locale),
                msg("notify.provider.cancel.body", locale, booking.getCustomerName(), formatFor(booking)));
    }

    private void dispatchToCustomer(Booking booking, String subject, String body) {
        boolean delivered = false;
        if (booking.getProvider().getPlan().isWhatsappReminders()
                && whatsAppChannel.supports(booking.getCustomerPhone())) {
            whatsAppChannel.send(booking.getCustomerPhone(), subject, body);
            delivered = true;
        }
        if (emailChannel.supports(booking.getCustomerEmail())) {
            emailChannel.send(booking.getCustomerEmail(), subject, body);
            delivered = true;
        }
        if (!delivered) {
            log.info("No reachable channel for booking {} - message skipped:\n{}", booking.getReference(), body);
        }
    }

    private String msg(String key, Locale locale, Object... args) {
        return messages.getMessage(key, args, key, locale);
    }

    private String manageLink(Booking booking) {
        return "%s/booking/%s".formatted(properties.getBaseUrl(), booking.getReference());
    }

    /** Formats the appointment date in the provider's zone and language. */
    private String formatFor(Booking booking) {
        Provider provider = booking.getProvider();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE dd MMMM yyyy - HH:mm", provider.locale());
        return formatter.format(booking.getStartAt().atZone(provider.zone()));
    }
}
