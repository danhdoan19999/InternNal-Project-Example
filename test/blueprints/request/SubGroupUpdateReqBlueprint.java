package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupUpdateReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(SubGroupUpdateReq.class)
public class SubGroupUpdateReqBlueprint {

    private final Faker faker = new Faker(ENGLISH);

    @Default
    private String name = faker.lorem().characters(10);

    @Default
    private String description = faker.lorem().characters(100);

    @Default
    private String imageName = "image.png";

    @Default
    private Long groupTypeId = faker.random().nextLong(3) + 1;

    @Default
    private Long managerId = faker.random().nextLong(10);
}
