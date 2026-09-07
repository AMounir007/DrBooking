package com.mounir.learn.drbooking.repository;

import com.mounir.learn.drbooking.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findBySlugIgnoreCase(String slug);

    Optional<Provider> findByEmailIgnoreCase(String email);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByEmailIgnoreCase(String email);
}
