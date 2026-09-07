package com.mounir.learn.drbooking.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class BookingRequest {

    @NotBlank(message = "Please enter your name")
    @Size(max = 120)
    private String customerName;

    @Email(message = "Please enter a valid e-mail")
    @Size(max = 160)
    private String customerEmail;

    @NotBlank(message = "Please enter your phone number")
    @Size(max = 30)
    private String customerPhone;

    @Size(max = 500)
    private String notes;

    /** ISO-8601 instant of the selected slot. */
    private Instant start;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getStart() { return start; }
    public void setStart(Instant start) { this.start = start; }
}
