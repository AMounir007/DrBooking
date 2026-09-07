package com.mounir.learn.drbooking.security;

import com.mounir.learn.drbooking.domain.Provider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ProviderPrincipal implements UserDetails {

    private final Long providerId;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final String slug;

    public ProviderPrincipal(Provider provider) {
        this.providerId = provider.getId();
        this.email = provider.getEmail();
        this.passwordHash = provider.getPasswordHash();
        this.displayName = provider.getDisplayName();
        this.slug = provider.getSlug();
    }

    public Long getProviderId() { return providerId; }

    public String getDisplayName() { return displayName; }

    public String getSlug() { return slug; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PROVIDER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
