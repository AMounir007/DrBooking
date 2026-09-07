package com.mounir.learn.drbooking.repository;

import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.domain.TimeOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {

    List<TimeOff> findByProviderOrderByStartAtAsc(Provider provider);

    List<TimeOff> findByProviderAndEndAtAfterAndStartAtBefore(Provider provider, Instant from, Instant to);
}
