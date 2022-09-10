package com.nals.rw360.interceptor.logging;

import com.nals.rw360.helpers.SecurityHelper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Objects;

import static com.nals.rw360.config.Constants.USER_ID_HEADER_NAME;
import static com.nals.rw360.config.Constants.USER_ID_LOG_NAME;
import static com.nals.rw360.security.AuthoritiesConstants.ANONYMOUS;

@Slf4j
public class RequestLoggingInterceptor
    implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull final HttpServletRequest request,
                             @Nonnull final HttpServletResponse response,
                             @Nonnull final Object handler) {
        if (!log.isTraceEnabled()) {
            MDC.put(USER_ID_LOG_NAME, Objects.isNull(request.getHeader(USER_ID_HEADER_NAME))
                ? SecurityHelper.getCurrentUserLogin().orElse(ANONYMOUS)
                : request.getHeader(USER_ID_HEADER_NAME));
        }

        return true;
    }

    @Override
    public void afterCompletion(@Nonnull final HttpServletRequest request,
                                @Nonnull final HttpServletResponse response,
                                @Nonnull final Object handler,
                                final Exception ex)
        throws Exception {
        if (!log.isTraceEnabled()) {
            MDC.clear();
        }
    }
}
