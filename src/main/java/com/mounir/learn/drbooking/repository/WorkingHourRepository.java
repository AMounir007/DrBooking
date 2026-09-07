package com.mounir.learn.drbooking.repository;

import com.mounir.learn.drbooking.domain.Provider;
import com.mounir.learn.drbooking.domain.WorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, Long> {

    List<WorkingHour> findByProviderOrderByDayOfWeekAscStartTimeAsc(Provider provider);

    List<WorkingHour> findByProviderAndDayOfWeek(Provider provider, DayOfWeek dayOfWeek);

    void deleteByProvider(Provider provider);
}
