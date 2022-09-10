package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.group.GroupCreateReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.JAPANESE;

@Getter
@Setter
@Blueprint(GroupCreateReq.class)
public class GroupCreateReqBlueprint {

    private final Faker faker = new Faker(JAPANESE);

    @Default
    private String name = faker.lorem().characters(10);

    @Default
    private String description = faker.lorem().characters(100);

    @Default
    private Long managerId = 1L;
}
