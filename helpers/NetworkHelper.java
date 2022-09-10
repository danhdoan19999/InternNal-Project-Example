package com.nals.rw360.helpers;

import javax.servlet.http.HttpServletRequest;

/**
 * @author thangnc
 */
public final class NetworkHelper {
    private NetworkHelper() {
    }

    public static String getClientIP(final HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }

        return remoteAddr;
    }
}
