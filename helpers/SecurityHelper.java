package com.nals.rw360.helpers;

import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.security.AuthoritiesConstants;
import com.nals.rw360.security.DomainUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;

/**
 * Utility class for Spring Security.
 */
public final class SecurityHelper {

    private SecurityHelper() {
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(final Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the User ID of the current user.
     *
     * @return the User ID of the current user.
     */
    public static Long getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                       .filter(authentication -> authentication.getPrincipal() instanceof DomainUserDetails)
                       .map(authentication -> ((DomainUserDetails) authentication.getPrincipal()).getId())
                       .orElseThrow(() -> new ObjectNotFoundException("current_user_id"));
    }

    public static Set<String> getCurrentUserRoles() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                       .filter(authentication -> authentication.getPrincipal() instanceof DomainUserDetails)
                       .map(authentication -> getRoles(authentication).collect(Collectors.toSet()))
                       .orElse(Collections.emptySet());
    }

    public static Set<String> getCurrentUserPermissions() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                       .filter(authentication -> authentication.getPrincipal() instanceof DomainUserDetails)
                       .map(authentication -> getAuthorities(authentication).collect(Collectors.toSet()))
                       .orElse(Collections.emptySet());
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && getAuthorities(authentication)
            .noneMatch(AuthoritiesConstants.ROLE_ANONYMOUS::equals);
    }

    public static boolean hasRole(final String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.nonNull(authentication) && getRoles(authentication).anyMatch(role::equals);
    }

    public static boolean hasAnyRole(final String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(authentication)) {
            return false;
        }

        for (String role : roles) {
            if (getRoles(authentication).anyMatch(role::equals)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasPermission(final String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.nonNull(authentication) && getAuthorities(authentication).anyMatch(authority::equals);
    }

    public static boolean hasAnyPermission(final String... authorities) {
        for (String authority : authorities) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Objects.nonNull(authentication) && getAuthorities(authentication).anyMatch(authority::equals);
        }
        return false;
    }

    public static boolean isAdmin() {
        return hasRole(ROLE_ADMIN.name());
    }

    public static boolean isSuperUser() {
        return hasAnyRole(ROLE_ADMIN.name(), ROLE_MANAGER.name());
    }

    public static boolean isSuperUser(final Collection<? extends GrantedAuthority> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return false;
        }

        return roles.stream().map(GrantedAuthority::getAuthority).anyMatch(SecurityHelper::isSuperUser);
    }

    public static boolean isSuperUser(final String[] roles) {
        if (Objects.isNull(roles)) {
            return false;
        }

        return Arrays.stream(roles).anyMatch(SecurityHelper::isSuperUser);
    }

    private static Stream<String> getAuthorities(final Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    }

    private static Stream<String> getRoles(final Authentication authentication) {
        return ((DomainUserDetails) authentication.getPrincipal()).getRoles()
                                                                  .stream()
                                                                  .map(GrantedAuthority::getAuthority);
    }

    private static boolean isSuperUser(final String role) {
        return role.equalsIgnoreCase(ROLE_ADMIN.name()) || role.equalsIgnoreCase(ROLE_MANAGER.name());
    }
}
