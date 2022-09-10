package com.nals.rw360.security.social.google;

import com.nals.rw360.security.social.SocialOAuth2Template;
import com.nals.rw360.security.social.SocialUser;

public class GoogleTemplate
    extends SocialOAuth2Template {

    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public GoogleTemplate(final String accessToken) {
        super(accessToken);
    }

    @Override
    public SocialUser getUserInfo() {
        return getRestTemplate().getForObject(USER_INFO_URL, SocialUser.class);
    }
}
