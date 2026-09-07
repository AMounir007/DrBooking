package com.mounir.learn.drbooking.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_provider_start", columnList = "provider_id,startAt"),
        @Index(name = "idx_booking_reference", columnList = "reference")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Always loaded together with the booking: rendering the manage page and sending
     * notifications (including from the async reminder scheduler) both need it, and a
     * lazy proxy would throw LazyInitializationException once the loading transaction closes.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    /** Public reference used in manage/cancel links. */
    @Column(nullable = false, unique = true, length = 40)
    private String reference = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(length = 160)
    private String customerEmail;

    @Column(nullable = false, length = 30)
    private String customerPhone;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    /** Who last changed {@link #status} - lets the provider answer "who confirmed / cancelled this?". */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingActor statusChangedBy;

    /** Display name of the actor above, e.g. the provider's name or the customer's name. */
    @Column(length = 120)
    private String statusChangedByName;

    /** When {@link #status} was last changed. */
    private Instant statusChangedAt;

    /** Optional free-text reason captured when a provider cancels or marks a no-show. */
    @Column(length = 300)
    private String statusChangeReason;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean reminder24hSent = false;

    @Column(nullable = false)
    private boolean reminder2hSent = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BookingActor getStatusChangedBy() { return statusChangedBy; }
    public void setStatusChangedBy(BookingActor statusChangedBy) { this.statusChangedBy = statusChangedBy; }

    public String getStatusChangedByName() { return statusChangedByName; }
    public void setStatusChangedByName(String statusChangedByName) { this.statusChangedByName = statusChangedByName; }

    public Instant getStatusChangedAt() { return statusChangedAt; }
    public void setStatusChangedAt(Instant statusChangedAt) { this.statusChangedAt = statusChangedAt; }

    public String getStatusChangeReason() { return statusChangeReason; }
    public void setStatusChangeReason(String statusChangeReason) { this.statusChangeReason = statusChangeReason; }

    /**
     * Moves this booking to a new status and records who did it and when, in one place,
     * so the audit trail can never be forgotten by a caller.
     */
    public void changeStatus(BookingStatus newStatus, BookingActor actor, String actorName, String reason, Instant now) {
        this.status = newStatus;
        this.statusChangedBy = actor;
        this.statusChangedByName = actorName;
        this.statusChangeReason = reason;
        this.statusChangedAt = now;
    }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isReminder24hSent() { return reminder24hSent; }
    public void setReminder24hSent(boolean reminder24hSent) { this.reminder24hSent = reminder24hSent; }

    public boolean isReminder2hSent() { return reminder2hSent; }
    public void setReminder2hSent(boolean reminder2hSent) { this.reminder2hSent = reminder2hSent; }

    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }
}
