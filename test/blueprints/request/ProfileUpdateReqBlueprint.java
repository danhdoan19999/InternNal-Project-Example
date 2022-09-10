package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.user.ProfileUpdateReq;
import com.nals.rw360.enums.Gender;
import com.nals.rw360.helpers.DateHelper;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static com.nals.rw360.enums.Gender.MALE;
import static java.util.Locale.JAPANESE;

@Getter
@Setter
@Blueprint(ProfileUpdateReq.class)
public class ProfileUpdateReqBlueprint {
    private final Faker faker = new Faker(JAPANESE);

    @Default
    private String name = faker.name().fullName();

    @Default
    private Gender gender = MALE;

    @Default
    private Long dob = DateHelper.toMillis(DateHelper.truncatedNowToDay());

    @Default
    private String phone = "0987654321";

    @Default
    private String address = faker.address().fullAddress();

    @Default
    private String imageName = "image.png";
}
