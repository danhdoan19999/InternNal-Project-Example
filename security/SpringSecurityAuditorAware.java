package com.nals.rw360.security;

import com.nals.rw360.helpers.SecurityHelper;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.nals.rw360.config.Constants.SYSTEM;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware
    implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String username = SecurityHelper.getCurrentUserLogin().orElse(SYSTEM);
        return Optional.of(username);
    }
}
