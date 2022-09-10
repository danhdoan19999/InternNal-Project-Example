package com.nals.rw360.security.custom;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * Base on WebSecurityExpressionRoot
 * {@link org.springframework.security.web.access.expression.WebSecurityExpressionRoot}
 */
public class CustomWebSecurityExpressionRoot
    extends CustomSecurityExpressionRoot {

    /**
     * Allows direct access to the request object
     */
    private final HttpServletRequest request;

    public CustomWebSecurityExpressionRoot(final Authentication a, final FilterInvocation fi) {
        super(a);
        this.request = fi.getRequest();
    }

    /**
     * Takes a specific IP address or a range using the IP/Netmask (e.g. 192.168.1.0/24 or
     * 202.24.0.0/14).
     *
     * @param ipAddress the address or range of addresses from which the request must
     *                  come.
     * @return true if the IP address of the current request is in the required range.
     */
    public boolean hasIpAddress(final String ipAddress) {
        IpAddressMatcher matcher = new IpAddressMatcher(ipAddress);
        return matcher.matches(this.request);
    }
}
