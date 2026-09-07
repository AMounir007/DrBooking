package com.mounir.learn.drbooking.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Exposes the current URL (without the "lang" parameter) so every page can render
 * a language switcher that keeps the visitor exactly where they are.
 */
@ControllerAdvice
public class LocaleModelAdvice {

    @ModelAttribute("switchUrl")
    public String switchUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return uri;
        }
        String kept = Arrays.stream(query.split("&"))
                .filter(p -> !p.startsWith("lang="))
                .collect(Collectors.joining("&"));
        return kept.isBlank() ? uri : uri + "?" + kept;
    }
}
