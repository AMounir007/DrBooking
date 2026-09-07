package com.mounir.learn.drbooking.web;

import com.mounir.learn.drbooking.service.BookingException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBookingException(BookingException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/booking-error";
    }
}
