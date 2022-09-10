package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.Assessment;
import com.nals.rw360.helpers.DateHelper;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import com.tobedevoured.modelcitizen.callback.FieldCallback;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import static com.nals.rw360.helpers.DateHelper.ASIA_HCM_ZONE_ID;
import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(Assessment.class)
public class AssessmentBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private FieldCallback<String> name = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.lorem().characters(10) + "assessment";
        }
    };

    @Default
    private FieldCallback<String> description = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.lorem().characters(20, 254);
        }
    };

    @Default
    private String imageName = "image.png";

    @Default
    private Instant startDate = DateHelper.getCurrentDateTimeAtStartOfDay(ASIA_HCM_ZONE_ID);

    @Default
    private Instant endDate = DateHelper.getCurrentDateTimeAtEndOfDay(ASIA_HCM_ZONE_ID);
}
