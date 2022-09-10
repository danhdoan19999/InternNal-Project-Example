package com.nals.rw360.dto.v1.response.assessment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssessmentRes {
    private Long id;
    private String name;
    private String description;
    private Long formsNumber;
    private Long formsUsedNumber;
    private String imageName;
    private String imageUrl;
    private Long startDate;
    private Long endDate;
}
