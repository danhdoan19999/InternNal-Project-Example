package com.nals.rw360.dto.v1.request.group;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.errors.annotation.ErrorMapping;
import com.nals.rw360.errors.annotation.ErrorMappings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.nals.rw360.errors.ErrorCodes.DESCRIPTION_NOT_BLANK;
import static com.nals.rw360.errors.ErrorCodes.INVALID_DESCRIPTION;
import static com.nals.rw360.errors.ErrorCodes.INVALID_NAME;
import static com.nals.rw360.errors.ErrorCodes.MANAGER_ID_NOT_NULL;
import static com.nals.rw360.errors.ErrorCodes.MEDIA_NOT_NULL;
import static com.nals.rw360.errors.ErrorCodes.NAME_NOT_BLANK;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GroupUpdateReq {

    @NotBlank
    @Size(max = 55)
    @ErrorMappings({
        @ErrorMapping(value = NotBlank.class, code = NAME_NOT_BLANK),
        @ErrorMapping(value = Size.class, code = INVALID_NAME)
    })
    private String name;

    @NotBlank
    @Size(max = 5000)
    @ErrorMappings({
        @ErrorMapping(value = NotBlank.class, code = DESCRIPTION_NOT_BLANK),
        @ErrorMapping(value = Size.class, code = INVALID_DESCRIPTION)
    })
    private String description;

    @NotNull
    @ErrorMappings({
        @ErrorMapping(value = NotNull.class, code = MEDIA_NOT_NULL)
    })
    private String imageName;

    @NotNull
    @ErrorMappings({
        @ErrorMapping(value = NotNull.class, code = MANAGER_ID_NOT_NULL)
    })
    private Long managerId;
}
