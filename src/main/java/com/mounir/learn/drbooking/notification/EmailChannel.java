package com.mounir.learn.drbooking.notification;

import com.mounir.learn.drbooking.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public EmailChannel(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "EMAIL";
    }

    @Override
    public boolean supports(String destination) {
        return destination != null && destination.contains("@");
    }

    @Override
    public void send(String destination, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(destination);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            log.info("Email sent to {} - {}", destination, subject);
        } catch (MailException ex) {
            // In the MVP a mail failure must never break the booking flow.
            log.warn("Email to {} could not be delivered ({}). Message content:\n{}",
                    destination, ex.getMessage(), body);
        }
    }
}
