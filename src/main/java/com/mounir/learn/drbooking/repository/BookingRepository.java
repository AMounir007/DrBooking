package com.mounir.learn.drbooking.repository;

import com.mounir.learn.drbooking.domain.Booking;
import com.mounir.learn.drbooking.domain.BookingStatus;
import com.mounir.learn.drbooking.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReference(String reference);

    List<Booking> findByProviderAndStartAtBetweenOrderByStartAtAsc(Provider provider, Instant from, Instant to);

    List<Booking> findByProviderAndStatusInAndStartAtBetweenOrderByStartAtAsc(
            Provider provider, Collection<BookingStatus> statuses, Instant from, Instant to);

    List<Booking> findByProviderAndStartAtGreaterThanEqualOrderByStartAtAsc(Provider provider, Instant from);

    long countByProviderAndCreatedAtAfter(Provider provider, Instant since);

    @Query("""
            select b from Booking b
            where b.status in :statuses
              and b.startAt between :from and :to
            """)
    List<Booking> findUpcoming(@Param("statuses") Collection<BookingStatus> statuses,
                               @Param("from") Instant from,
                               @Param("to") Instant to);

    boolean existsByProviderAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Provider provider, Collection<BookingStatus> statuses, Instant end, Instant start);
}
