package com.nals.rw360.blueprints;

import com.github.javafaker.Faker;
import com.nals.rw360.domain.GroupType;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import com.tobedevoured.modelcitizen.callback.FieldCallback;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(GroupType.class)
public class GroupTypeBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private FieldCallback<String> name = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.lorem().characters(10);
        }
    };
}
