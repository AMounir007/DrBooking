package com.mounir.learn.drbooking.security;

import com.mounir.learn.drbooking.repository.ProviderRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderUserDetailsService implements UserDetailsService {

    private final ProviderRepository providerRepository;

    public ProviderUserDetailsService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return providerRepository.findByEmailIgnoreCase(username)
                .map(ProviderPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + username));
    }
}
