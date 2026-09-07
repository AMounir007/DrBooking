package com.mounir.learn.drbooking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * A service provider (doctor, coach, salon owner...) who owns a public booking page.
 */
@Entity
@Table(name = "providers", uniqueConstraints = {
        @UniqueConstraint(columnNames = "slug"),
        @UniqueConstraint(columnNames = "email")
})
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public URL identifier, e.g. "dr-ahmed" -> /dr-ahmed */
    @NotBlank
    @Column(nullable = false, length = 80)
    private String slug;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 160)
    private String businessName;

    @Email
    @NotBlank
    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 200)
    private String passwordHash;

    /** WhatsApp-capable phone in international format, e.g. 201234567890 */
    @Column(length = 30)
    private String whatsappNumber;

    @Column(length = 60)
    private String zoneId = "UTC";

    /** Length of a single bookable slot, in minutes. */
    @Column(nullable = false)
    private int slotMinutes = 30;

    /** Buffer applied after each booking, in minutes. */
    @Column(nullable = false)
    private int bufferMinutes = 0;

    /** How many days ahead customers may book. */
    @Column(nullable = false)
    private int bookingHorizonDays = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan = Plan.FREE;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingHour> workingHours = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeOff> timeOffs = new ArrayList<>();

    public ZoneId zone() {
        try {
            return ZoneId.of(zoneId);
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

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

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<WorkingHour> getWorkingHours() { return workingHours; }
    public void setWorkingHours(List<WorkingHour> workingHours) { this.workingHours = workingHours; }

    public List<TimeOff> getTimeOffs() { return timeOffs; }
    public void setTimeOffs(List<TimeOff> timeOffs) { this.timeOffs = timeOffs; }
}
