package com.mounir.learn.drbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/**
 * Internationalisation: Arabic is the default language and Egypt the default time zone.
 * Visitors can switch with ?lang=ar / ?lang=en, the choice is stored in a cookie.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Default time zone of the platform. */
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Africa/Cairo");

    public static final Locale ARABIC = Locale.forLanguageTag("ar-EG");
    public static final Locale ENGLISH = Locale.ENGLISH;

    public static final List<Locale> SUPPORTED = List.of(ARABIC, ENGLISH);

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setDefaultLocale(ARABIC);
        resolver.setDefaultTimeZone(java.util.TimeZone.getTimeZone(DEFAULT_ZONE));
        resolver.setCookieMaxAge(java.time.Duration.ofDays(365));
        resolver.setCookiePath("/");
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
