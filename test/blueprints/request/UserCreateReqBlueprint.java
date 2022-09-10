package com.nals.rw360.blueprints.request;

import com.github.javafaker.Faker;
import com.nals.rw360.dto.v1.request.user.UserCreateReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import com.tobedevoured.modelcitizen.callback.FieldCallback;
import lombok.Getter;
import lombok.Setter;

import static java.util.Locale.ENGLISH;

@Getter
@Setter
@Blueprint(UserCreateReq.class)
public class UserCreateReqBlueprint {
    private final Faker faker = new Faker(ENGLISH);

    @Default
    private FieldCallback<String> name = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.name().fullName();
        }
    };

    @Default
    private FieldCallback<String> email = new FieldCallback<>() {
        @Override
        public String get(final Object referenceModel) {
            return faker.internet().emailAddress();
        }
    };

    @Default
    private String password = "admin!@#123";

    @Default
    private Long roleId = 1L;
}
