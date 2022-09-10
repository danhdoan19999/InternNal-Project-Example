package com.nals.rw360.blueprints.request;

import com.nals.rw360.dto.v1.request.group.sub.AddMemberReq;
import com.tobedevoured.modelcitizen.annotation.Blueprint;
import com.tobedevoured.modelcitizen.annotation.Default;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Blueprint(AddMemberReq.class)
public class AddMemberReqBlueprint {

    @Default
    private Set<Long> ids = new HashSet<>(Arrays.asList(1L, 2L));
}
