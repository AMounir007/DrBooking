package com.mounir.learn.drbooking.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ProfileForm {

    @NotBlank
    private String displayName;

    private String businessName;

    private String whatsappNumber;

    @NotBlank
    private String zoneId;

    @Min(5)
    @Max(480)
    private int slotMinutes = 30;

    @Min(0)
    @Max(240)
    private int bufferMinutes = 0;

    @Min(1)
    @Max(365)
    private int bookingHorizonDays = 30;

    private String description;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int slotMinutes) { this.slotMinutes = slotMinutes; }

    public int getBufferMinutes() { return bufferMinutes; }
    public void setBufferMinutes(int bufferMinutes) { this.bufferMinutes = bufferMinutes; }

    public int getBookingHorizonDays() { return bookingHorizonDays; }
    public void setBookingHorizonDays(int bookingHorizonDays) { this.bookingHorizonDays = bookingHorizonDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
