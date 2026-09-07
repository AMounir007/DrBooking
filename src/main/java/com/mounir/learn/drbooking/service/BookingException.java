package com.mounir.learn.drbooking.service;

/**
 * Raised when a booking cannot be created (slot taken, plan limit reached...).
 */
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}
