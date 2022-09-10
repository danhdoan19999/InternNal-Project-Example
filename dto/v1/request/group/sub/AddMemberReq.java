package com.nals.rw360.dto.v1.request.group.sub;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.errors.annotation.ErrorMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Set;

import static com.nals.rw360.errors.ErrorCodes.LIST_MEMBER_ID_NOT_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddMemberReq {
    @NotEmpty
    @ErrorMapping(value = NotNull.class, code = LIST_MEMBER_ID_NOT_NULL)
    private Set<@NotNull Long> ids;
}
