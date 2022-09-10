package com.nals.rw360.security.custom;

import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.security.DomainUserDetails;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Base on SecurityExpressionRoot
 * {@link org.springframework.security.access.expression.SecurityExpressionRoot}
 */
public class CustomSecurityExpressionRoot
    implements SecurityExpressionOperations {

    private final Authentication authentication;
    private PermissionEvaluator permissionEvaluator;
    private AuthenticationTrustResolver trustResolver;
    private RoleHierarchy roleHierarchy;
    private Set<String> roles;
    private Set<String> authorities;
    private String defaultRolePrefix = "ROLE_";

    private final boolean permitAll = true;
    private final boolean denyAll = false;

    private final String read = "read";
    private final String write = "write";
    private final String create = "create";
    private final String delete = "delete";
    private final String admin = "administration";

    /**
     * Creates a new instance
     *
     * @param authentication the {@link Authentication} to use. Cannot be null.
     */
    CustomSecurityExpressionRoot(final Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication object cannot be null");
        }
        this.authentication = authentication;
    }

    public final boolean hasAuthority(final String authority) {
        return hasAnyAuthority(authority);
    }

    public final boolean hasAnyAuthority(final String... authorities) {
        return hasAnyAuthorityName(authorities);
    }

    public final boolean hasRole(final String role) {
        return hasAnyRole(role);
    }

    public final boolean hasAnyRole(final String... roles) {
        return hasAnyRoleName(roles);
    }

    public final Authentication getAuthentication() {
        return authentication;
    }

    public final boolean permitAll() {
        return true;
    }

    public final boolean denyAll() {
        return false;
    }

    public final boolean isAnonymous() {
        return trustResolver.isAnonymous(authentication);
    }

    public final boolean isAuthenticated() {
        return !isAnonymous();
    }

    public final boolean isRememberMe() {
        return trustResolver.isRememberMe(authentication);
    }

    public final boolean isFullyAuthenticated() {
        return !trustResolver.isAnonymous(authentication)
            && !trustResolver.isRememberMe(authentication);
    }

    /**
     * Convenience method to access {@link Authentication#getPrincipal()} from
     * {@link #getAuthentication()}
     *
     * @return Object
     */
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    public void setTrustResolver(final AuthenticationTrustResolver trustResolver) {
        this.trustResolver = trustResolver;
    }

    public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    /**
     * <p>
     * Sets the default prefix to be added to {@link #hasAnyRole(String...)} or
     * {@link #hasRole(String)}. For example, if hasRole("ADMIN") or hasRole("ROLE_ADMIN")
     * is passed in, then the role ROLE_ADMIN will be used when the defaultRolePrefix is
     * "ROLE_" (default).
     * </p>
     *
     * <p>
     * If null or empty, then no default role prefix is used.
     * </p>
     *
     * @param defaultRolePrefix the default prefix to add to roles. Default "ROLE_".
     */
    public void setDefaultRolePrefix(final String defaultRolePrefix) {
        this.defaultRolePrefix = defaultRolePrefix;
    }

    private Set<String> getAuthorities() {
        if (Objects.isNull(authorities)) {
            Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();

            if (Objects.nonNull(roleHierarchy)) {
                userAuthorities = roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
            }

            authorities = AuthorityUtils.authorityListToSet(userAuthorities);
        }

        return authorities;
    }

    private Set<String> getRoles() {
        if (Objects.isNull(roles)) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                roles = new HashSet<>();
            } else {
                Collection<? extends GrantedAuthority> roleGrantedAuthorities =
                    ((DomainUserDetails) authentication.getPrincipal()).getRoles();
                roles = AuthorityUtils.authorityListToSet(roleGrantedAuthorities);
            }
        }

        return roles;
    }

    public boolean hasPermission(final Object target, final Object permission) {
        return permissionEvaluator.hasPermission(authentication, target, permission);
    }

    public boolean hasPermission(final Object targetId, final String targetType, final Object permission) {
        return permissionEvaluator.hasPermission(authentication, (Serializable) targetId,
                                                 targetType, permission);
    }

    public void setPermissionEvaluator(final PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    private boolean hasAnyRoleName(final String... roles) {
        Set<String> roleSet = getRoles();

        for (String role : roles) {
            if (roleSet.contains(getRoleWithDefaultPrefix(this.defaultRolePrefix, role))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasAnyAuthorityName(final String... authorities) {
        Set<String> authoritySet = getAuthorities();

        for (String authority : authorities) {
            if (authoritySet.contains(authority)) {
                return true;
            }
        }

        return false;
    }

    private static String getRoleWithDefaultPrefix(final String defaultRolePrefix, final String role) {
        if (Objects.isNull(role)) {
            return null;
        }

        if (StringHelper.isBlank(defaultRolePrefix)) {
            return role;
        }

        if (role.startsWith(defaultRolePrefix)) {
            return role;
        }

        return defaultRolePrefix + role;
    }
}
