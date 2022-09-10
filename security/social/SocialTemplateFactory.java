package com.nals.rw360.security.social;

import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.enums.AuthProvider;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.security.social.google.GoogleTemplate;
import lombok.Builder;

import static com.nals.rw360.errors.ErrorCodes.INVALID_AUTH_PROVIDER;

@Builder
public class SocialTemplateFactory {
    private final ApplicationProperties.Social socialConfig;
    private final String accessToken;

    public SocialTemplate getSocialTemplate(final AuthProvider provider) {
        switch (provider) {
            case GOOGLE:
                return new GoogleTemplate(accessToken);
            default:
                throw new ValidatorException("Auth provider not support", "provider", INVALID_AUTH_PROVIDER);
        }
    }
}
