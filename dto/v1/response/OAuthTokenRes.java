package com.nals.rw360.dto.v1.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.dto.v1.response.user.ProfileRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OAuthTokenRes {

    private String accessToken;

    private String refreshToken;

    private Set<String> roles;

    private Set<String> permissions;

    private long expiredIn;

    private ProfileRes userInfo;
}
