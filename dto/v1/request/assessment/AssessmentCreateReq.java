package com.nals.rw360.dto.v1.request.assessment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.deserializer.TrimDeserializer;
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
import static com.nals.rw360.errors.ErrorCodes.NAME_NOT_BLANK;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssessmentCreateReq {
    @NotBlank
    @Size(max = 55)
    @ErrorMappings({
        @ErrorMapping(value = NotBlank.class, code = NAME_NOT_BLANK),
        @ErrorMapping(value = Size.class, code = INVALID_NAME)
    })
    @JsonDeserialize(using = TrimDeserializer.class)
    private String name;

    @NotBlank
    @Size(max = 1000)
    @ErrorMappings({
        @ErrorMapping(value = NotBlank.class, code = DESCRIPTION_NOT_BLANK),
        @ErrorMapping(value = Size.class, code = INVALID_DESCRIPTION)
    })
    @JsonDeserialize(using = TrimDeserializer.class)
    private String description;

    private String imageName;

    @NotNull
    private Long startDate;

    @NotNull
    private Long endDate;
}
