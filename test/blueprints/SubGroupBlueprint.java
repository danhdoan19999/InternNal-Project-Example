package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.enums.Status;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import com.tobedevoured.modelcitizen.callback.FieldCallback;
import lombok.Getter;
import lombok.Setter;

import static com.nals.rw360.enums.Status.OPEN;
import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(SubGroup.class)
public class SubGroupBlueprint {

    private final Faker faker = new Faker(ENGLISH);

    @Default
    private FieldCallback<String> name = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.lorem().characters(10) + "Sub Group";
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
    private Status status = OPEN;

    @Default
    private Long groupTypeId = 1L;

    @Default
    private Long groupId = 1L;

    @Default
    private Long managerId = 1L;
}
