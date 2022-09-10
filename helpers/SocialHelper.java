package com.nals.rw360.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;

public final class SocialHelper {
    public static final String USERNAME_SEPARATOR = ";";

    private SocialHelper() {
    }

    public static String getProviderUserId(final String socialId, final String socialEmail) {
        Assert.notNull(socialId, "Social ID must not be null");
        Assert.notNull(socialEmail, "Social Email must not be null");
        return String.format("%s%s%s", socialId, USERNAME_SEPARATOR, socialEmail);
    }

    public static SocialUserInfo getSocialUserInfo(final String providerUserId) {
        Assert.notNull(providerUserId, "Provider User Id must not be null");
        String[] infos = providerUserId.split(USERNAME_SEPARATOR);
        return new SocialUserInfo(infos[0], infos[1]);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialUserInfo {
        private String id;
        private String email;
    }
}
