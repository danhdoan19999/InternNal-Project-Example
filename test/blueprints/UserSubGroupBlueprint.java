package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.UserSubGroup;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(UserSubGroup.class)
public class UserSubGroupBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private Long userId = faker.random().nextLong(100);

    @Default
    private Long subGroupId = faker.random().nextLong(100);

    @Default
    private Long groupId = faker.random().nextLong(100);
}
