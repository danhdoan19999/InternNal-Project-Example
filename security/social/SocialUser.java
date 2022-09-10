package com.nals.rw360.security.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialUser {
    private String sub;
    private String name;
    private String email;
    private String picture;
}
