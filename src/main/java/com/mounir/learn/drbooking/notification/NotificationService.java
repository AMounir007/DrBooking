package com.mounir.learn.drbooking.notification;

import com.mounir.learn.drbooking.config.AppProperties;
import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builds and dispatches confirmation / reminder / cancellation messages.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("EEEE dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);

    private final EmailChannel emailChannel;
    private final WhatsAppChannel whatsAppChannel;
    private final AppProperties properties;

    public NotificationService(EmailChannel emailChannel, WhatsAppChannel whatsAppChannel, AppProperties properties) {
        this.emailChannel = emailChannel;
        this.whatsAppChannel = whatsAppChannel;
        this.properties = properties;
    }

    @Async
    public void sendConfirmation(Booking booking) {
        Provider provider = booking.getProvider();
        String subject = "Appointment confirmed with " + provider.getDisplayName();
        String body = """
                Hello %s,

                Your appointment with %s is confirmed.

                When: %s (%s)
                Reference: %s

                Manage or cancel your booking: %s

                See you soon!
                """.formatted(
                booking.getCustomerName(),
                provider.getDisplayName(),
                formatFor(booking),
                provider.getZoneId(),
                booking.getReference(),
                manageLink(booking));

        dispatchToCustomer(booking, subject, body);

        // Notify the provider as well.
        emailChannel.send(provider.getEmail(),
                "New booking: " + booking.getCustomerName(),
                "%s booked %s (%s). Phone: %s".formatted(
                        booking.getCustomerName(), formatFor(booking),
                        provider.getZoneId(), booking.getCustomerPhone()));
    }

    @Async
    public void sendReminder(Booking booking, int hoursBefore) {
        String subject = "Reminder: appointment in %d hours".formatted(hoursBefore);
        String body = """
                Hello %s,

                This is a reminder for your appointment with %s.

                When: %s (%s)

                Please confirm or cancel here: %s
                """.formatted(
                booking.getCustomerName(),
                booking.getProvider().getDisplayName(),
                formatFor(booking),
                booking.getProvider().getZoneId(),
                manageLink(booking));

        dispatchToCustomer(booking, subject, body);
    }

    @Async
    public void sendCancellation(Booking booking) {
        String subject = "Appointment cancelled";
        String body = """
                Hello %s,

                Your appointment with %s on %s has been cancelled.

                You can book a new time here: %s/%s
                """.formatted(
                booking.getCustomerName(),
                booking.getProvider().getDisplayName(),
                formatFor(booking),
                properties.getBaseUrl(),
                booking.getProvider().getSlug());

        dispatchToCustomer(booking, subject, body);
        emailChannel.send(booking.getProvider().getEmail(), "Booking cancelled",
                "%s cancelled the appointment of %s.".formatted(booking.getCustomerName(), formatFor(booking)));
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

    private String manageLink(Booking booking) {
        return "%s/booking/%s".formatted(properties.getBaseUrl(), booking.getReference());
    }

    private String formatFor(Booking booking) {
        return FORMAT.format(booking.getStartAt().atZone(booking.getProvider().zone()));
    }
}
