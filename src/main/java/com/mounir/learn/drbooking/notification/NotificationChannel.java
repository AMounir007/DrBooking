package com.mounir.learn.drbooking.notification;

/**
 * Outbound channel used to deliver a notification to a customer or a provider.
 */
public interface NotificationChannel {

    String name();

    /**
     * @param destination e-mail address or phone number depending on the channel
     */
    void send(String destination, String subject, String body);

    boolean supports(String destination);
}
