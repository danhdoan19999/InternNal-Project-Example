package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupCreateReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(SubGroupCreateReq.class)
public class SubGroupCreateReqBlueprint {

    private final Faker faker = new Faker(ENGLISH);

    @Default
    private String name = faker.lorem().characters(10);

    @Default
    private String description = faker.lorem().characters(100);

    @Default
    private Long groupTypeId = 1L;

    @Default
    private Long managerId = 1L;
}
