package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.Group;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import com.tobedevoured.modelcitizen.callback.FieldCallback;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(Group.class)
public class GroupBlueprint {

    private final Faker faker = new Faker(ENGLISH);

    @Default
    private FieldCallback<String> name = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.lorem().characters(10) + "Group";
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
    private Long managerId = 1L;
}
