package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.group.GroupUpdateReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(GroupUpdateReq.class)
public class GroupUpdateReqBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private String name = faker.name().name();

    @Default
    private String description = faker.lorem().characters(20, 254);

    @Default
    private String imageName = "image.png";

    @Default
    private Long managerId = 1L;
}
