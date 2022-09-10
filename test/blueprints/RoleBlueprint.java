package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.Role;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static com.nals.rw360.enums.RoleType.ROLE_PMO;
import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(Role.class)
public class RoleBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private String name = ROLE_PMO.name();
}
