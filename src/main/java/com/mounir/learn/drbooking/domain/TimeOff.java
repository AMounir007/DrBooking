package com.mounir.learn.drbooking.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A blocked period (holiday, personal event, external calendar event).
 */
@Entity
@Table(name = "time_offs")
public class TimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant endAt;

    @Column(length = 200)
    private String reason;

    /** Identifier of the event when imported from Google/Outlook calendars. */
    @Column(length = 200)
    private String externalEventId;

    public TimeOff() {
    }

    public TimeOff(Provider provider, Instant startAt, Instant endAt, String reason) {
        this.provider = provider;
        this.startAt = startAt;
        this.endAt = endAt;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getExternalEventId() { return externalEventId; }
    public void setExternalEventId(String externalEventId) { this.externalEventId = externalEventId; }
}
