package com.nals.rw360.dto.v1.response.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProfileRes {
    private Long id;
    private String name;
    private Gender gender;
    private Long dob;
    private String email;
    private String phone;
    private String address;
    private String imageName;
    private String imageUrl;
    private Boolean isFirstLogin;
}
