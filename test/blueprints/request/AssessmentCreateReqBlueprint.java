package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.assessment.AssessmentCreateReq;
import com.nals.rw360.helpers.DateHelper;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import static com.nals.rw360.helpers.DateHelper.ASIA_HCM_ZONE_ID;
import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(AssessmentCreateReq.class)
public class AssessmentCreateReqBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private String name = faker.lorem().characters(10);

    @Default
    private String description = faker.lorem().characters(100);

    @Default
    private Long startDate = DateHelper.toMillis(DateHelper.getCurrentDateTimeAtEndOfDay(ASIA_HCM_ZONE_ID));

    @Default
    private Long endDate = DateHelper.toMillis(DateHelper.getCurrentDateTimeAtEndOfDay(ASIA_HCM_ZONE_ID));
}
